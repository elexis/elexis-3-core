package ch.elexis.core.mail.internal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.internal.account.MailboxAccountService;
import ch.elexis.core.mail.internal.legacy.MailLegacyAccountStore;
import ch.elexis.core.mail.internal.legacy.MailLegacyMigration;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;

/** Isolates compatibility and migration tests from the installation configuration. */
public abstract class MailLegacyTestSupport {
	protected final Map<String, String> global = new ConcurrentHashMap<>();
	protected final Map<String, String> local = new ConcurrentHashMap<>();
	protected final AtomicInteger claims = new AtomicInteger();
	protected final AtomicInteger migratedWrites = new AtomicInteger();
	protected CyclicBarrier claimBarrier;
	protected IConfigService config;
	protected IModelService model;
	protected MailLegacyAccountStore store;
	protected MailboxAccountService accounts;
	protected MailClient client;

	/** Wires the real facade, conversion and storage to isolated config and model services. */
	@Before
	public void prepareLegacyServices() throws Exception {
		config = (IConfigService) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { IConfigService.class }, (proxy, method, args) -> {
					Map<String, String> values = method.getName().endsWith("Local") ? local : global;
					String key = (String) args[0];
					if (method.getName().startsWith("get")) {
						return values.getOrDefault(key, (String) args[1]);
					}
					if (method.getName().startsWith("set")) {
						if (args[1] == null) {
							values.remove(key);
						} else {
							values.put(key, (String) args[1]);
						}
						if (key.startsWith("mail/client/account/") && !key.contains("/legacy") && !key.contains("/mandators")) {
							migratedWrites.incrementAndGet();
						}
						return method.getReturnType() == boolean.class ? true : null;
					}
					throw new UnsupportedOperationException(method.getName());
				});
		model = (IModelService) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { IModelService.class }, (proxy, method, args) -> {
					if (method.getName().equals("load")) {
						String key = (String) args[0];
						if (claimBarrier != null && args.length == 2 && key.equals("mail/client/legacy/migration")) {
							claimBarrier.await(5, TimeUnit.SECONDS);
							return Optional.empty();
						}
						return global.containsKey(key) ? Optional.of(entry(key)) : Optional.empty();
					}
					if (method.getName().equals("executeNativeUpdate")) {
						Matcher insert = Pattern.compile("VALUES \\('([^']*)', '([^']*)'\\)").matcher((String) args[0]);
						if (!insert.find()) {
							throw new AssertionError("Expected a literal config insert");
						}
						if (global.putIfAbsent(insert.group(1), insert.group(2)) != null) {
							throw new IllegalStateException("Duplicate config key");
						}
						if (insert.group(1).equals("mail/client/legacy/migration")) {
							claims.incrementAndGet();
						}
						return 1;
					}
					if (method.getName().equals("save")) {
						IConfig entry = (IConfig) args[0];
						global.put(entry.getKey(), entry.getValue());
						return null;
					}
					throw new UnsupportedOperationException(method.getName());
				});
		store = new MailLegacyAccountStore();
		inject(store, "configService", config);
		accounts = new MailboxAccountService();
		inject(accounts, "configService", config);
		client = new MailClient();
		inject(client, "configService", config);
		inject(client, "accountService", accounts);
		inject(client, "legacyAccounts", store);
		inject(client, "modelService", model);
		client.activate();
	}

	/** Stops facade workers after each isolated test. */
	@After
	public void stopLegacyServices() {
		if (client != null) {
			client.deactivate();
		}
	}

	protected MailLegacyMigration migration() throws Exception {
		MailLegacyMigration migration = new MailLegacyMigration();
		inject(migration, "configService", config);
		inject(migration, "modelService", model);
		inject(migration, "accounts", store);
		return migration;
	}

	protected void migrate(MailLegacyMigration migration) throws Exception {
		Method activate = MailLegacyMigration.class.getDeclaredMethod("activate");
		activate.setAccessible(true);
		try {
			activate.invoke(migration);
		} catch (InvocationTargetException e) {
			throw new AssertionError(e.getCause());
		}
	}

	protected static void inject(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	protected MailAccount legacy(String id, MailAccount.TYPE type) {
		MailAccount account = new MailAccount();
		account.setId(id);
		account.setType(type);
		account.setHost("relay.example.test");
		account.setPort(type == MailAccount.TYPE.SMTP ? "25" : "143");
		account.setUsername("sender@example.test");
		account.setPassword("secret-with-padding");
		return account;
	}

	private IConfig entry(String key) {
		return (IConfig) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { IConfig.class },
				(proxy, method, args) -> switch (method.getName()) {
				case "getKey", "getId" -> key;
				case "getValue" -> global.get(key);
				case "setValue" -> global.put(key, (String) args[0]);
				default -> throw new UnsupportedOperationException(method.getName());
				});
	}
}
