package ch.elexis.core.mail.ui.preference;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.service.IMailAccountTester;
import ch.elexis.core.mail.service.IMailOAuthService;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.MailAccessException;
import ch.elexis.core.mail.ui.archive.ArchiveUtil;
import ch.elexis.core.mail.ui.messages.Messages;
import ch.elexis.core.ui.documents.composites.CategorySelectionEditComposite;

/**
 * Lists manageable accounts without connecting and hosts optional account contributions.
 */
public final class MailAccountsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	/** Workbench preference page identifier. */
	public static final String ID = "ch.elexis.core.mail.ui.preference"; //$NON-NLS-1$
	private MailSettingsServices services;
	private IMailboxAccountService accounts;
	private IMailOAuthService oauth;
	private IUser user;
	private Table table;
	private Composite body;
	private org.eclipse.swt.widgets.Display display;
	private boolean disposed;
	private Composite contributionActions;
	private Composite contributionSettings;
	private ServiceTracker<IMailAccountPageContribution, IMailAccountPageContribution> contributions;
	private final List<IMailAccountPageContribution> pageContributions = new ArrayList<>();
	private final List<Runnable> saveContributions = new ArrayList<>();
	private final List<Button> contributionButtons = new ArrayList<>();
	private Button defaultAccount;
	private Label legacyWarning;
	private Label localInfo;

	/** {@inheritDoc} */
	@Override
	public void init(IWorkbench workbench) {
		setTitle(Messages.MailSettings_Title);
	}

	/** {@inheritDoc} */
	@Override
	protected Control createContents(Composite parent) {
		body = new Composite(parent, SWT.NONE);
		display = body.getDisplay();
		body.setLayout(new GridLayout(2, false));
		user = ContextServiceHolder.get().getActiveUser().orElse(null);
		if (user == null) {
			setErrorMessage(Messages.MailSettings_Unavailable);
			setValid(false);
			return body;
		}
		services = new MailSettingsServices();
		try {
			accounts = services.get(IMailboxAccountService.class);
			oauth = services.get(IMailOAuthService.class);
		} catch (IllegalStateException e) {
			setErrorMessage(Messages.MailSettings_Unavailable);
			setValid(false);
			return body;
		}
		table = new Table(body, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 220;
		data.widthHint = 600;
		table.setLayoutData(data);
		for (String title : new String[] { Messages.MailSettings_Name, Messages.MailSettings_Address,
				Messages.MailSettings_Scope, Messages.MailSettings_Owner, Messages.MailSettings_Provider,
				Messages.MailSettings_State }) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(title);
			column.setWidth(145);
		}
		Composite actions = new Composite(body, SWT.NONE);
		actions.setLayout(new GridLayout());
		actions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		button(actions, Messages.MailSettings_New, () -> edit(null));
		Button edit = button(actions, Messages.MailSettings_Edit, () -> edit(selected()));
		Button remove = button(actions, Messages.MailSettings_Delete, this::remove);
		contributionActions = new Composite(actions, SWT.NONE);
		contributionActions.setLayout(new GridLayout());
		contributionActions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		defaultAccount = new Button(actions, SWT.CHECK);
		defaultAccount.setText(Messages.MailSettings_DefaultAccount);
		defaultAccount.setEnabled(false);
		defaultAccount.addListener(SWT.Selection, event -> {
			MailboxAccount account = selected();
			if (account != null && account.scope() == MailboxScope.SHARED && account.smtp() != null) {
				ConfigServiceHolder.get().set(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT,
						defaultAccount.getSelection() ? account.id() : null);
			}
		});
		edit.setEnabled(false);
		remove.setEnabled(false);
		table.addListener(SWT.Selection, event -> {
			edit.setEnabled(selected() != null);
			remove.setEnabled(selected() != null);
			updateSelection();
		});
		table.addListener(SWT.DefaultSelection, event -> {
			MailboxAccount selected = selected();
			if (selected != null) {
				edit(selected);
			}
		});
		legacyWarning = new Label(body, SWT.WRAP);
		legacyWarning.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		localInfo = new Label(body, SWT.WRAP);
		localInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		contributionSettings = new Composite(body, SWT.NONE);
		contributionSettings.setLayout(new GridLayout(2, false));
		contributionSettings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		createArchiveSettings();
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		contributions = new ServiceTracker<>(context, IMailAccountPageContribution.class, null) {
			@Override
			public IMailAccountPageContribution addingService(ServiceReference<IMailAccountPageContribution> reference) {
				IMailAccountPageContribution service = super.addingService(reference);
				rebuildLater();
				return service;
			}

			@Override
			public void removedService(ServiceReference<IMailAccountPageContribution> reference,
					IMailAccountPageContribution service) {
				super.removedService(reference, service);
				rebuildLater();
			}
		};
		contributions.open();
		rebuildContributions();
		return body;
	}

	private void createArchiveSettings() {
		Composite archive = new Composite(body, SWT.NONE);
		archive.setLayout(new GridLayout(2, false));
		archive.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		new Label(archive, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Button archiveAttachments = new Button(archive, SWT.CHECK);
		archiveAttachments.setText(Messages.MailSettings_ArchiveAttachments);
		archiveAttachments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		archiveAttachments.setSelection(ConfigServiceHolder.get().get(ArchiveUtil.PREF_MAIL_ARCHIVE_ENABLED, false));
		Label archiveCategoryLabel = new Label(archive, SWT.NONE);
		archiveCategoryLabel.setText(Messages.MailSettings_ArchiveCategory);
		archiveCategoryLabel.setEnabled(archiveAttachments.getSelection());
		CategorySelectionEditComposite archiveCategory = new CategorySelectionEditComposite(archive, SWT.NONE,
				"ch.elexis.data.store.omnivore", true); //$NON-NLS-1$
		archiveCategory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		archiveCategory.setEnabled(archiveAttachments.getSelection());
		archiveCategory.setCategoryByName(ConfigServiceHolder.get().get(ArchiveUtil.PREF_MAIL_ARCHIVE_DOCUMENT_CATEGORY,
				ArchiveUtil.DEFAULT_CATEGORY));
		archiveAttachments.addListener(SWT.Selection, event -> {
			ConfigServiceHolder.get().set(ArchiveUtil.PREF_MAIL_ARCHIVE_ENABLED, archiveAttachments.getSelection());
			archiveCategoryLabel.setEnabled(archiveAttachments.getSelection());
			archiveCategory.setEnabled(archiveAttachments.getSelection());
		});
		archiveCategory.addSelectionChangeListener(event -> {
			ICategory category = archiveCategory.getSelection();
			ConfigServiceHolder.get().set(ArchiveUtil.PREF_MAIL_ARCHIVE_DOCUMENT_CATEGORY,
					category != null ? category.getName() : null);
		});
	}

	private void rebuildLater() {
		if (!display.isDisposed()) {
			display.asyncExec(() -> {
				if (!disposed && !body.isDisposed()) {
					rebuildContributions();
				}
			});
		}
	}

	private void rebuildContributions() {
		for (Control control : contributionActions.getChildren()) {
			control.dispose();
		}
		for (Control control : contributionSettings.getChildren()) {
			control.dispose();
		}
		while (table.getColumnCount() > 6) {
			table.getColumn(6).dispose();
		}
		pageContributions.clear();
		saveContributions.clear();
		contributionButtons.clear();
		ServiceReference<IMailAccountPageContribution>[] references = contributions.getServiceReferences();
		if (references != null) {
			for (ServiceReference<IMailAccountPageContribution> reference : references) {
				IMailAccountPageContribution contribution = contributions.getService(reference);
				if (contribution == null) {
					continue;
				}
				pageContributions.add(contribution);
				TableColumn column = new TableColumn(table, SWT.NONE);
				column.setText(contribution.columnTitle());
				column.setWidth(145);
				for (IMailAccountPageContribution.Action action : contribution.actions(user)) {
					Button button = button(contributionActions, action.label(), () -> {
						MailboxAccount account = selected();
						if (account != null && action.enabled().test(account)) {
							action.run().accept(getShell(), account);
							refresh();
						}
					});
					button.setData(action);
					contributionButtons.add(button);
				}
				saveContributions.add(contribution.createSettings(contributionSettings, user));
			}
		}
		refresh();
		body.layout(true, true);
	}

	private void updateSelection() {
		MailboxAccount account = selected();
		defaultAccount.setEnabled(account != null && account.scope() == MailboxScope.SHARED && account.smtp() != null);
		defaultAccount.setSelection(account != null && account.id().equals(
				ConfigServiceHolder.get().get(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT, null)));
		for (Button button : contributionButtons) {
			IMailAccountPageContribution.Action action = (IMailAccountPageContribution.Action) button.getData();
			button.setEnabled(account != null && action.enabled().test(account));
		}
	}

	private Button button(Composite parent, String label, Runnable action) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button.addListener(SWT.Selection, event -> action.run());
		return button;
	}

	private MailboxAccount selected() {
		return table.getSelectionCount() == 0 ? null : (MailboxAccount) table.getSelection()[0].getData();
	}

	private void refresh() {
		table.removeAll();
		updateSelection();
		List<String> insecure = new ArrayList<>();
		for (MailboxAccount account : accounts.getManageableAccounts(user)) {
			boolean foreign = account.scope() == MailboxScope.PERSONAL && !user.getId().equals(account.ownerUserId());
			String state = foreign ? "-" //$NON-NLS-1$
					: !accounts.hasCredentials(user, account.id()) ? Messages.MailSettings_NotSignedIn
					: account.authMode() == AuthMode.PASSWORD ? Messages.MailSettings_Stored
							: oauth.getSignedInAs(user, account.id())
									.map(value -> NLS.bind(Messages.MailSettings_SignedInAs, value))
									.orElse(Messages.MailSettings_Stored);
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(account);
			List<String> values = new ArrayList<>(List.of(account.displayName(), account.emailAddress(),
					account.scope() == MailboxScope.PERSONAL ? Messages.MailSettings_PersonalShort
							: Messages.MailSettings_SharedShort,
					MailAccountForm.ownerLabel(account, id -> CoreModelServiceHolder.get().load(id, IUser.class)
							.map(IUser::getUsername).orElse(null), Messages.MailSettings_SharedShort),
					MailAccountDialog.providerText(account.provider()), state));
			for (IMailAccountPageContribution contribution : pageContributions) {
				values.add(contribution.columnText(user, account));
			}
			item.setText(values.toArray(String[]::new));
			if (accounts.isLegacy(account.id()) && (account.imap() != null && account.imap().security() == MailSecurity.NONE
					|| account.smtp() != null && account.smtp().security() == MailSecurity.NONE)) {
				insecure.add(account.id());
			}
		}
		legacyWarning.setText(insecure.isEmpty() ? "" //$NON-NLS-1$
				: NLS.bind(Messages.MailSettings_LegacyWarning, String.join(", ", insecure))); //$NON-NLS-1$
		try {
			List<String> local = services.get(IMailClient.class).getAccountsLocal();
			localInfo.setText(local.isEmpty() ? "" //$NON-NLS-1$
					: NLS.bind(Messages.MailSettings_LocalAccounts, String.join(", ", local))); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			setErrorMessage(Messages.MailSettings_Unavailable);
		}
	}

	private void edit(MailboxAccount account) {
		try {
			new MailAccountDialog(getShell(), user, account, accounts, oauth, services.get(IMailAccountTester.class)).open();
			refresh();
		} catch (IllegalStateException e) {
			setErrorMessage(Messages.MailSettings_Unavailable);
		}
	}

	private void remove() {
		MailboxAccount selected = selected();
		if (selected != null && MessageDialog.openConfirm(getShell(), Messages.MailSettings_Delete,
				NLS.bind(Messages.MailSettings_DeleteQuestion, selected.emailAddress()))) {
			try {
				accounts.remove(user, selected.id());
				refresh();
			} catch (MailAccessException e) {
				setErrorMessage(Messages.MailSettings_AccessDenied);
			}
		}
	}

	/** Saves contributed settings when Apply is pressed. */
	@Override
	protected void performApply() {
		performOk();
	}

	/**
	 * Saves contributed settings.
	 * 
	 * @return whether all requested settings were stored
	 */
	@Override
	public boolean performOk() {
		if (!isValid()) {
			return false;
		}
		try {
			saveContributions.forEach(Runnable::run);
		} catch (MailAccessException | IllegalArgumentException | IllegalStateException e) {
			setErrorMessage(Messages.MailSettings_SaveFailed);
			return false;
		}
		return true;
	}

	/** Releases OSGi references held by this page. */
	@Override
	public void dispose() {
		disposed = true;
		if (contributions != null) {
			contributions.close();
		}
		if (services != null) {
			services.close();
		}
		super.dispose();
	}
}
