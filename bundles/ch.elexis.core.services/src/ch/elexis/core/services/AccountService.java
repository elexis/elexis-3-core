package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IAccount;
import ch.elexis.core.services.internal.Account;

@Component
public class AccountService implements IAccountService {
	private static final String ACCOUNTS_CONFIG = "ch.elexis.core.data/accounttransaction/accounts"; //$NON-NLS-1$
	private static final String ACCOUNTS_SEPARATOR = "||"; //$NON-NLS-1$

	public Account UNKNOWN = new Account(-1, StringUtils.EMPTY);
	private HashMap<Integer, IAccount> localCache;

	@Reference
	private IConfigService configService;

	private List<IAccount> loadAccounts() {
		List<IAccount> ret = new ArrayList<>();
		ret.add(UNKNOWN);
		String accountsString = configService.get(ACCOUNTS_CONFIG, StringUtils.EMPTY);
		if (accountsString != null && !accountsString.isEmpty()) {
			String[] accounts = accountsString.split("\\|\\|"); //$NON-NLS-1$
			for (String string : accounts) {
				String[] parts = string.split("\\|"); //$NON-NLS-1$
				if (parts.length == 2) {
					ret.add(new Account(Integer.parseInt(parts[0]), parts[1]));
				}
			}
		}
		return ret;
	}

	/**
	 * Get the current map of accounts. The map is reloaded after
	 * {@link Account#setAccounts(List)} is called.
	 *
	 * @return
	 */
	@Override
	public HashMap<Integer, IAccount> getAccounts() {
		if (localCache == null) {
			loadCache();
		}
		return localCache;
	}

	private void loadCache() {
		localCache = new HashMap<>();
		List<IAccount> accounts = loadAccounts();
		for (IAccount account : accounts) {
			localCache.put(account.getNumeric(), account);
		}
	}

	@Override
	public void removeAccount(IAccount account) {
		List<IAccount> accounts = loadAccounts();
		for (Iterator<IAccount> iterator = accounts.iterator(); iterator.hasNext();) {
			Account existingAccount = (Account) iterator.next();
			if (existingAccount.getNumeric() == account.getNumeric()) {
				iterator.remove();
			}
		}
		setAccounts(accounts);
	}

	@Override
	public void addAccount(IAccount newAccount) {
		String existingString = configService.get(ACCOUNTS_CONFIG, StringUtils.EMPTY);
		StringBuilder sb = new StringBuilder();
		sb.append(existingString);
		if (sb.length() > 0) {
			sb.append(ACCOUNTS_SEPARATOR);
		}
		sb.append(newAccount.getNumeric()).append("|").append(newAccount.getName());
		configService.set(ACCOUNTS_CONFIG, sb.toString());
		// reset local cache
		loadCache();
	}

	@Override
	public void setAccounts(List<IAccount> accounts) {
		StringBuilder sb = new StringBuilder();
		for (IAccount account : accounts) {
			if (account.getNumeric() == -1) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(ACCOUNTS_SEPARATOR);
			}
			sb.append(account.getNumeric()).append("|").append(account.getName());
		}
		configService.set(ACCOUNTS_CONFIG, sb.toString());
		// reset local cache
		loadCache();
	}

	@Override
	public void initDefaults() {
		HashMap<Integer, IAccount> existingAccounts = getAccounts();
		if (!existingAccounts.containsKey(Integer.valueOf(1000))) {
			addAccount(new Account(1000, "Kasse"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(1100))) {
			addAccount(new Account(1100, "Post"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(1200))) {
			addAccount(new Account(1200, "BESR"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(1201))) {
			addAccount(new Account(1201, "Manuelle Bankeingänge"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(1209))) {
			addAccount(new Account(1209, "EFT-Zahlungen"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(4590))) {
			addAccount(new Account(4590, "Differenz"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(4595))) {
			addAccount(new Account(4595, "Debitorenverlust"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(6060))) {
			addAccount(new Account(6060, "Mahngebühren (Ertrag)"));
		}
		if (!existingAccounts.containsKey(Integer.valueOf(9999))) {
			addAccount(new Account(9999, "Diverses"));
		}
	}

	@Override
	public IAccount getUnknown() {
		return UNKNOWN;
	}

}
