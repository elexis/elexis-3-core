package ch.elexis.core.mail.ui.preference;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.views.controls.RichTextEditorComposite;
import ch.elexis.core.mail.ui.preference.MailSignInHandler;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.SentCopyMode;
import ch.elexis.core.mail.service.IMailAccountTester;
import ch.elexis.core.mail.service.IMailOAuthService;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.MailAccessException;
import ch.elexis.core.mail.ui.messages.Messages;
import ch.elexis.core.mail.ui.preference.MailAccountForm.Field;

/**
 * Edits personal and shared mail accounts, using background connection tests
 * and browser sign-in.
 */
public final class MailAccountDialog extends TitleAreaDialog {
	private static final Logger log = LoggerFactory.getLogger(MailAccountDialog.class);
	private final IUser user;
	private final IMailboxAccountService accounts;
	private final IMailOAuthService oauth;
	private final IMailAccountTester tester;
	private final Display display;
	private final boolean existing;
	private MailAccountForm form;
	private final Map<Field, Text> fields = new EnumMap<>(Field.class);
	private final Map<Field, Combo> choices = new EnumMap<>(Field.class);
	private Combo provider;
	private List<MailProvider> providers;
	private Table mandators;
	private Combo owner;
	private Label ownerLabel;
	private List<IUser> ownerUsers;
	private Composite content;
	private Label accessHint;
	private Button personal;
	private Button shared;
	private Button googleOAuth;
	private Button googlePassword;
	private Button sharedMailbox;
	private Button signIn;
	private Button signOut;
	private Button test;
	private Text password;
	private Label passwordLabel;
	private Label authHint;
	private Label state;
	private Table delegates;
	private Table visibleUsers;
	private Table visibleGroups;
	private RichTextEditorComposite signature;
	private CompletableFuture<?> pending;
	private boolean updating;
	private boolean testing;

	/**
	 * Creates a resizable dialog; construction never connects to a server.
	 * 
	 * @param parent   parent shell
	 * @param user     active user
	 * @param account  existing account or null
	 * @param accounts account storage
	 * @param oauth    browser sign-in service
	 * @param tester   asynchronous connection tester
	 */
	public MailAccountDialog(Shell parent, IUser user, MailboxAccount account, IMailboxAccountService accounts,
			IMailOAuthService oauth, IMailAccountTester tester) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.user = user;
		this.accounts = accounts;
		this.oauth = oauth;
		this.tester = tester;
		display = parent.getDisplay();
		existing = account != null;
		form = new MailAccountForm(account, user.getId(), accounts.canManageShared(user));
	}

	/** {@inheritDoc} */
	@Override
	protected Point getInitialSize() {
		return new Point(780, 680);
	}

	/** {@inheritDoc} */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		setTitle(Messages.MailSettings_AccountTitle);
		setMessage(Messages.MailSettings_AccountHint);
		TabFolder tabs = new TabFolder(area, SWT.NONE);
		tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content = tabs;
		Composite body = tab(tabs, Messages.MailSettings_General);
		new Label(body, SWT.NONE).setText(Messages.MailSettings_Provider);
		provider = new Combo(body, SWT.READ_ONLY);
		providers = Arrays.stream(MailProvider.values())
				.filter(value -> value == MailProvider.IMAP || value == MailProvider.HIN || oauth.isAvailable(value))
				.toList();
		provider.setItems(providers.stream().map(MailAccountDialog::providerText).toArray(String[]::new));
		provider.addListener(SWT.Selection, event -> {
			form.provider(providers.get(provider.getSelectionIndex()), oauth.isAvailable(MailProvider.GOOGLE));
			populate();
		});
		field(body, Field.ADDRESS, Messages.MailSettings_Address);
		field(body, Field.NAME, Messages.MailSettings_Name);
		Group access = group(body, Messages.MailSettings_Scope);
		personal = radio(access, Messages.MailSettings_Personal, this::updateAccess);
		shared = radio(access, Messages.MailSettings_Shared, this::updateAccess);
		personal.setEnabled(form.canChangeScope(accounts.canManageShared(user)));
		shared.setEnabled(accounts.canManageShared(user));
		ownerLabel = new Label(access, SWT.NONE);
		ownerLabel.setText(Messages.MailSettings_Owner);
		owner = new Combo(access, SWT.READ_ONLY);
		owner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ownerUsers = CoreModelServiceHolder.get().findAll(IUser.class);
		owner.setItems(ownerUsers.stream().map(value -> Objects.toString(value.getUsername(), value.getId()))
				.toArray(String[]::new));
		owner.addListener(SWT.Selection, event -> {
			form.owner(ownerUsers.get(owner.getSelectionIndex()).getId());
			check(delegates, form.delegates());
			updateAuth();
		});
		delegates = userList(access, Messages.MailSettings_Delegates);
		visibleUsers = userList(access, Messages.MailSettings_VisibleUsers);
		Label groupsLabel = new Label(access, SWT.NONE);
		groupsLabel.setText(Messages.MailSettings_VisibleGroups);
		groupsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		visibleGroups = checkTable(access);
		for (IUserGroup value : CoreModelServiceHolder.get().findAll(IUserGroup.class)) {
			TableItem item = new TableItem(visibleGroups, SWT.NONE);
			item.setData(value.getId());
			item.setText(Objects.toString(value.getGroupname(), value.getId()));
		}
		new Label(access, SWT.NONE).setText(Messages.MailSettings_Mandators);
		mandators = checkTable(access);
		for (IMandator value : CoreModelServiceHolder.get().findAll(IMandator.class)) {
			TableItem item = new TableItem(mandators, SWT.NONE);
			item.setData(value.getId());
			item.setText(value.getLabel());
		}
		accessHint = new Label(access, SWT.WRAP);
		accessHint.setText(Messages.MailSettings_VisibilityHint);
		accessHint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Composite authentication = tab(tabs, Messages.MailSettings_Authentication);
		googleOAuth = radio(authentication, Messages.MailSettings_GoogleOAuth, this::updateAuth);
		googlePassword = radio(authentication, Messages.MailSettings_AppPassword, this::updateAuth);
		field(authentication, Field.USERNAME, Messages.MailSettings_Username);
		passwordLabel = new Label(authentication, SWT.NONE);
		password = new Text(authentication, SWT.BORDER | SWT.PASSWORD);
		password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sharedMailbox = new Button(authentication, SWT.CHECK);
		sharedMailbox.setText(Messages.MailSettings_SharedMailbox);
		sharedMailbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		sharedMailbox.addListener(SWT.Selection, event -> form.sharedMailbox(sharedMailbox.getSelection()));
		signIn = action(authentication, Messages.MailSettings_MicrosoftSignIn, this::signIn);
		signOut = action(authentication, Messages.MailSettings_SignOut, this::signOut);
		state = new Label(authentication, SWT.WRAP);
		state.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		authHint = new Label(authentication, SWT.WRAP);
		authHint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Composite server = tab(tabs, Messages.MailSettings_Advanced);
		field(server, Field.IMAP_HOST, Messages.MailSettings_ImapHost);
		field(server, Field.IMAP_PORT, Messages.MailSettings_ImapPort);
		choice(server, Field.IMAP_SECURITY, Messages.MailSettings_ImapSecurity,
				Arrays.stream(MailSecurity.values()).map(Enum::name).toArray(String[]::new));
		field(server, Field.SMTP_HOST, Messages.MailSettings_SmtpHost);
		field(server, Field.SMTP_PORT, Messages.MailSettings_SmtpPort);
		choice(server, Field.SMTP_SECURITY, Messages.MailSettings_SmtpSecurity,
				Arrays.stream(MailSecurity.values()).map(Enum::name).toArray(String[]::new));
		choice(server, Field.SENT_COPY, Messages.MailSettings_SentCopy,
				Arrays.stream(SentCopyMode.values()).map(Enum::name).toArray(String[]::new));
		field(server, Field.BUDGET, Messages.MailSettings_Budget);
		field(server, Field.POLL, Messages.MailSettings_Poll);
		Composite sign = tab(tabs, Messages.MailSettings_Signature);
		sign.setLayout(new GridLayout());
		signature = new RichTextEditorComposite(sign, SWT.NONE);
		signature.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		signature.setText(form.get(Field.SIGNATURE));
		signature.addModifyListener(event -> form.set(Field.SIGNATURE, Objects.toString(signature.getText(), ""))); //$NON-NLS-1$
		if (existing) {
			MailboxAccount account = accounts.getAccount(form.id()).orElseThrow();
			check(delegates, account.delegateUserIds());
			check(visibleUsers, account.visibleUserIds());
			check(visibleGroups, account.visibleGroupIds());
			check(mandators, accounts.getMandatorIds(account.id()));
		}
		populate();
		return area;
	}

	private Composite tab(TabFolder tabs, String title) {
		Composite page = new Composite(tabs, SWT.NONE);
		page.setLayout(new GridLayout(2, false));
		TabItem item = new TabItem(tabs, SWT.NONE);
		item.setText(title);
		item.setControl(page);
		return page;
	}

	private Group group(Composite parent, String title) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(title);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		return group;
	}

	private Button radio(Composite parent, String title, Runnable action) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(title);
		button.addListener(SWT.Selection, event -> {
			if (button.getSelection()) {
				action.run();
			}
		});
		return button;
	}

	private Button action(Composite parent, String title, Runnable action) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(title);
		button.addListener(SWT.Selection, event -> action.run());
		return button;
	}

	private void field(Composite parent, Field field, String title) {
		new Label(parent, SWT.NONE).setText(title);
		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fields.put(field, text);
		text.addModifyListener(event -> {
			if (!updating) {
				form.set(field, text.getText());
				if (field == Field.ADDRESS) {
					populate();
				}
			}
		});
	}

	private void choice(Composite parent, Field field, String title, String[] values) {
		new Label(parent, SWT.NONE).setText(title);
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setItems(Arrays.stream(values)
				.map(value -> field == Field.SENT_COPY ? text("MailSettings_Sent" + value) //$NON-NLS-1$
						: "NONE".equals(value) //$NON-NLS-1$
								? Messages.MailSettings_SecurityNONE
								: value)
				.toArray(String[]::new));
		combo.setData(values);
		choices.put(field, combo);
		combo.addListener(SWT.Selection,
				event -> form.set(field, ((String[]) combo.getData())[combo.getSelectionIndex()]));
	}

	private Table checkTable(Composite parent) {
		Table table = new Table(parent, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 70;
		table.setLayoutData(data);
		return table;
	}

	private Table userList(Composite parent, String title) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(title);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		Table table = checkTable(parent);
		for (IUser value : CoreModelServiceHolder.get().findAll(IUser.class)) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(value.getId());
			item.setText(Objects.toString(value.getUsername(), value.getId()));
		}
		return table;
	}

	private void check(Table table, List<String> ids) {
		for (TableItem item : table.getItems()) {
			item.setChecked(ids.contains(item.getData()));
		}
	}

	private List<String> checked(Table table) {
		return Arrays.stream(table.getItems()).filter(TableItem::getChecked).map(item -> (String) item.getData())
				.toList();
	}

	private void populate() {
		updating = true;
		fields.forEach((field, text) -> {
			if (!text.getText().equals(form.get(field))) {
				text.setText(form.get(field));
			}
		});
		choices.forEach(
				(field, combo) -> combo.select(Arrays.asList((String[]) combo.getData()).indexOf(form.get(field))));
		provider.select(providers.indexOf(form.provider()));
		personal.setSelection(form.scope() == MailboxScope.PERSONAL);
		shared.setSelection(form.scope() == MailboxScope.SHARED);
		googleOAuth.setSelection(form.auth() == AuthMode.OAUTH2);
		googlePassword.setSelection(form.auth() == AuthMode.PASSWORD);
		sharedMailbox.setSelection(form.sharedMailbox());
		updating = false;
		updateAccess();
		updateAuth();
	}

	private void updateAccess() {
		form.access(personal.getSelection() ? MailboxScope.PERSONAL : MailboxScope.SHARED, user.getId(),
				checked(delegates), checked(visibleUsers), checked(visibleGroups), accounts.canManageShared(user));
		check(delegates, form.delegates());
		check(visibleUsers, form.visibleUsers());
		check(visibleGroups, form.visibleGroups());
		accessHint.setText(form.groupsDropped() ? Messages.MailSettings_GroupsDropped
				: Messages.MailSettings_VisibilityHint);
		owner.select(ownerUsers.stream().map(IUser::getId).toList().indexOf(form.owner()));
		owner.setEnabled(form.canChangeOwner());
		visible(ownerLabel, personal.getSelection());
		visible(owner, personal.getSelection());
		showList(delegates, personal.getSelection());
		showList(visibleUsers, shared.getSelection());
		showList(visibleGroups, shared.getSelection());
		showList(mandators, shared.getSelection());
		visible(accessHint, shared.getSelection() || form.groupsDropped());
		content.layout(true, true);
		updateAuth();
	}

	private void showList(Table table, boolean show) {
		List<Control> siblings = Arrays.asList(table.getParent().getChildren());
		visible(siblings.get(siblings.indexOf(table) - 1), show);
		visible(table, show);
	}

	private void updateAuth() {
		boolean google = form.provider() == MailProvider.GOOGLE;
		visible(googleOAuth, google);
		visible(googlePassword, google);
		visible(sharedMailbox, form.provider() == MailProvider.M365);
		visible(signIn, form.auth() == AuthMode.OAUTH2);
		visible(signOut, form.auth() == AuthMode.OAUTH2);
		boolean available = oauth.isAvailable(form.provider());
		googleOAuth.setEnabled(google && available);
		googlePassword.setEnabled(google);
		if (google && !available) {
			googleOAuth.setSelection(false);
			googlePassword.setSelection(true);
		}
		if (google) {
			form.auth(googlePassword.getSelection() ? AuthMode.PASSWORD : AuthMode.OAUTH2);
		}
		visible(signIn, form.auth() == AuthMode.OAUTH2);
		visible(signOut, form.auth() == AuthMode.OAUTH2);
		password.setEnabled(form.auth() == AuthMode.PASSWORD);
		Text username = fields.get(Field.USERNAME);
		if (username != null) {
			username.setEnabled(form.auth() == AuthMode.PASSWORD);
		}
		passwordLabel.setText(
				form.provider() == MailProvider.HIN ? Messages.MailSettings_HinToken : Messages.MailSettings_Password);
		sharedMailbox.setEnabled(form.provider() == MailProvider.M365);
		signIn.setText(form.provider() == MailProvider.GOOGLE ? Messages.MailSettings_GoogleSignIn
				: Messages.MailSettings_MicrosoftSignIn);
		signIn.setEnabled(form.auth() == AuthMode.OAUTH2 && available && pending == null && !testing
				&& form.canAuthenticate());
		signOut.setEnabled(form.id() != null && form.auth() == AuthMode.OAUTH2 && pending == null && !testing
				&& form.canAuthenticate());
		authHint.setText(form.provider() == MailProvider.HIN ? Messages.MailSettings_HinHint
				: form.auth() == AuthMode.OAUTH2 && !available || google && !available
						? Messages.MailSettings_OAuthUnavailable
						: Messages.MailSettings_PasswordHint);
		if (pending == null) {
			state.setText(!form.canAuthenticate() ? "-" //$NON-NLS-1$
					: form.id() == null ? Messages.MailSettings_NotSignedIn
					: form.auth() == AuthMode.OAUTH2
							? oauth.getSignedInAs(user, form.id())
									.map(value -> NLS.bind(Messages.MailSettings_SignedInAs, value))
									.orElse(Messages.MailSettings_NotSignedIn)
							: accounts.hasCredentials(user, form.id()) ? Messages.MailSettings_Stored
									: Messages.MailSettings_NotSignedIn);
		}
		if (test != null) {
			test.setEnabled(form.canAuthenticate() && pending == null && !testing);
		}
		state.getParent().layout(true, true);
	}

	private void visible(Control control, boolean visible) {
		GridData data = control.getLayoutData() instanceof GridData existingData ? existingData : new GridData();
		data.exclude = !visible;
		control.setLayoutData(data);
		control.setVisible(visible);
	}

	private void capture() {
		form.access(personal.getSelection() ? MailboxScope.PERSONAL : MailboxScope.SHARED, user.getId(),
				checked(delegates), checked(visibleUsers), checked(visibleGroups), accounts.canManageShared(user));
		form.set(Field.SIGNATURE, Objects.toString(signature.getText(), "")); //$NON-NLS-1$
	}

	private boolean save() {
		capture();
		String invalid = form.validationKey();
		if (invalid != null) {
			setErrorMessage(text(invalid));
			return false;
		}
		boolean transferring = form.ownerChanged() && form.scope() == MailboxScope.PERSONAL;
		if (transferring && !MessageDialog.openQuestion(getShell(), Messages.MailSettings_TransferTitle,
				NLS.bind(Messages.MailSettings_TransferQuestion, owner.getText()))) {
			return false;
		}
		char[] secret = password.getText().toCharArray();
		try {
			if (transferring && form.id() != null && form.auth() == AuthMode.PASSWORD && secret.length > 0) {
				accounts.setPassword(user, form.id(), secret);
				password.setText(""); //$NON-NLS-1$
			}
			MailboxAccount saved = accounts.save(user, form.build());
			form.saved(saved.id());
			if (saved.scope() == MailboxScope.SHARED) {
				accounts.setMandatorIds(user, saved.id(), checked(mandators));
			}
			personal.setEnabled(accounts.canManageShared(user));
			shared.setEnabled(accounts.canManageShared(user));
			if (!transferring && form.auth() == AuthMode.PASSWORD && secret.length > 0) {
				accounts.setPassword(user, saved.id(), secret);
				password.setText(""); //$NON-NLS-1$
			}
			setErrorMessage(null);
			return true;
		} catch (IllegalArgumentException | MailAccessException e) {
			setErrorMessage(e instanceof MailAccessException ? Messages.MailSettings_AccessDenied
					: Messages.MailSettings_InvalidAccount);
			return false;
		} finally {
			Arrays.fill(secret, '\0');
		}
	}

	private void signIn() {
		if (!form.canAuthenticate() || pending != null || !save()) {
			return;
		}
		try {
			CompletableFuture<?> future = MailSignInHandler.signIn(user, form.id(), accounts, oauth, uri -> display.asyncExec(() -> {
				if (getShell() != null && !getShell().isDisposed()) {
					Program.launch(uri.toString());
				}
			}));
			pending = future;
			content.setEnabled(false);
			state.setText(Messages.MailSettings_SigningIn);
			updateAuth();
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			test.setEnabled(false);
			future.whenComplete((result, failure) -> ui(() -> {
				pending = null;
				content.setEnabled(true);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				test.setEnabled(form.canAuthenticate());
				if (failure != null) {
					setErrorMessage(text("MailSettings_Error" + MailSignInHandler.error(failure))); //$NON-NLS-1$
				}
				updateAuth();
			})).exceptionally(failure -> {
				log.error("Mail sign-in UI update failed", failure); //$NON-NLS-1$
				return null;
			});
		} catch (IllegalArgumentException | MailAccessException e) {
			setErrorMessage(Messages.MailSettings_AccessDenied);
		}
	}

	private void signOut() {
		if (!form.canAuthenticate()) {
			return;
		}
		try {
			oauth.signOut(user, form.id());
			updateAuth();
		} catch (IllegalArgumentException | MailAccessException e) {
			setErrorMessage(Messages.MailSettings_AccessDenied);
		}
	}

	private void test() {
		if (!form.canAuthenticate()) {
			return;
		}
		capture();
		String invalid = form.validationKey();
		if (invalid != null) {
			setErrorMessage(text(invalid));
			return;
		}
		char[] secret = form.auth() == AuthMode.PASSWORD && !password.getText().isEmpty()
				? password.getText().toCharArray()
				: null;
		try {
			testing = true;
			content.setEnabled(false);
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			test.setEnabled(false);
			updateAuth();
			setErrorMessage(null);
			setMessage(Messages.MailSettings_Testing, IMessageProvider.INFORMATION);
			tester.test(user, form.build(), secret).whenComplete((result, failure) -> {
				if (secret != null) {
					Arrays.fill(secret, '\0');
				}
				ui(() -> {
					testing = false;
					content.setEnabled(true);
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					test.setEnabled(form.canAuthenticate());
					updateAuth();
					if (failure != null || result == null) {
						setErrorMessage(Messages.MailSettings_TestFailed);
					} else {
						setMessage(
								NLS.bind(Messages.MailSettings_TestResult,
										result.imap() == null ? "" : text("MailSettings_Test" + result.imap().name()), //$NON-NLS-1$
										result.smtp() == null ? "" : text("MailSettings_Test" + result.smtp().name())), //$NON-NLS-1$
								IMessageProvider.INFORMATION);
					}
				});
			}).exceptionally(failure -> {
				log.error("Mail test UI update failed", failure); //$NON-NLS-1$
				return null;
			});
		} catch (IllegalArgumentException | MailAccessException e) {
			if (secret != null) {
				Arrays.fill(secret, '\0');
			}
			testing = false;
			content.setEnabled(true);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			test.setEnabled(form.canAuthenticate());
			updateAuth();
			setErrorMessage(Messages.MailSettings_InvalidAccount);
		}
	}

	private void ui(Runnable task) {
		if (!display.isDisposed()) {
			display.asyncExec(() -> {
				if (getShell() != null && !getShell().isDisposed()) {
					task.run();
				}
			});
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		test = createButton(parent, IDialogConstants.CLIENT_ID, Messages.MailSettings_TestConnection, false);
		test.setEnabled(form.canAuthenticate());
		test.addListener(SWT.Selection, event -> test());
		super.createButtonsForButtonBar(parent);
	}

	/**
	 * Saves validated values and keeps the dialog open after validation or access
	 * errors.
	 */
	@Override
	protected void okPressed() {
		if (save()) {
			super.okPressed();
		}
	}

	/**
	 * Cancels an outstanding browser sign-in before closing.
	 * 
	 * @return whether the dialog closed
	 */
	@Override
	public boolean close() {
		if (pending != null) {
			pending.cancel(true);
		}
		return super.close();
	}

	/**
	 * Resolves a validated settings localization key.
	 * 
	 * @param key Messages field name
	 * @return localized text or a generic safe error
	 */
	public static String text(String key) {
		try {
			return (String) Messages.class.getField(key).get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return Messages.MailSettings_InvalidAccount;
		}
	}

	/**
	 * Returns the provider's user-facing name.
	 * 
	 * @param provider provider
	 * @return localized name
	 */
	public static String providerText(MailProvider provider) {
		return switch (provider) {
		case M365 -> Messages.MailSettings_Microsoft;
		case GOOGLE -> Messages.MailSettings_Google;
		case HIN -> Messages.MailSettings_Hin;
		case IMAP -> Messages.MailSettings_Other;
		};
	}
}
