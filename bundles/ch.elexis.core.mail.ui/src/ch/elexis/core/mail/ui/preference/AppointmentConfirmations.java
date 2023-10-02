package ch.elexis.core.mail.ui.preference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.medelexis.pea.PeaService;

public class AppointmentConfirmations extends PreferencePage implements IWorkbenchPreferencePage {

	private Composite parentComposite;
	private ComboViewer accountsViewer;
	private ComboViewer templatesViewer;
	private Button testButton;
	private String selectedTemplateName;
	private static final String INVALID_PEA_URL = "https://pea.myelexis.ch?sid=null";
	private ITextTemplate previousTemplateSelection;
	private String peaUrl;

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));

		new Label(parentComposite, SWT.NONE).setText("Standard E-Mail:");
		accountsViewer = new ComboViewer(parentComposite);
		accountsViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		accountsViewer.setContentProvider(new ArrayContentProvider());
		accountsViewer.setLabelProvider(new LabelProvider());
		accountsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

			}

		});
		updateAccountsCombo();

		Label emailTemplatesLabel = new Label(parentComposite, SWT.NONE);
		emailTemplatesLabel.setText("Standard E-Mail Vorlagen: ");
		emailTemplatesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		LabelProvider templateLabelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ITextTemplate) {
					return ((ITextTemplate) element).getName() + (((ITextTemplate) element).getMandator() != null
							? " (" + ((ITextTemplate) element).getMandator().getLabel() + ")"
							: StringUtils.EMPTY);
				}
				return super.getText(element);
			}
		};
		Optional<PeaService> peaService = OsgiServiceUtil.getService(PeaService.class);
		peaUrl = INVALID_PEA_URL; // Standardwert setzen
		if (peaService.isPresent()) {
			peaUrl = peaService.get().getPeaUrl();
		}
		System.out.println("Test 1234 " + peaUrl);
		templatesViewer = new ComboViewer(parentComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		templatesViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		templatesViewer.setContentProvider(new ArrayContentProvider());
		templatesViewer.setLabelProvider(templateLabelProvider);
		templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && !selection.isEmpty()) {
					Object element = selection.getFirstElement();
					selectedTemplateName = templateLabelProvider.getText(element);
				}
			}
		});
		templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selectedElement = selection.getFirstElement();

				if (selectedElement instanceof ITextTemplate) {
					ITextTemplate template = (ITextTemplate) selectedElement;

					if ("Terminbestätigung inkl. Anmeldeformular".equals(template.getName())
							&& INVALID_PEA_URL.equals(peaUrl)) { // Beachten Sie, dass Sie die peaUrl hier zur Verfügung
																	// haben müssen
						Shell shell = templatesViewer.getControl().getShell();

						MessageDialog dialog = new MessageDialog(shell, "Warnung", null,
								"Die Vorlage \"Terminbestätigung inkl. Anmeldeformular\" ist nur in Kombination mit der Online Patientenerfassung möglich.",
								MessageDialog.WARNING, new String[] { "OK" }, 0) {
							@Override
							protected Control createCustomArea(Composite parent) {
								Link link = new Link(parent, SWT.NONE);
								link.setText(
										"<a href=\"https://medelexis.ch/pea/\">Weiter Informationen finden Sie hier.</a>");
								link.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) {
										Program.launch("https://medelexis.ch/pea/");
									}
								});
								return link;
							}
						};

						dialog.open();

						// Setze die Auswahl immer zurück auf die vorherige Auswahl
						if (previousTemplateSelection != null) {
							templatesViewer.setSelection(new StructuredSelection(previousTemplateSelection), true);
						}
					} else {
						previousTemplateSelection = template;
					}
				}
			}
		});

		updateTemplatesCombo();

		testButton = new Button(parentComposite, SWT.PUSH);
		testButton.setText("Test der E-Mail");
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (MailClientComponent.getMailClient() != null && accountsViewer.getStructuredSelection() != null) {
					IStructuredSelection selection = accountsViewer.getStructuredSelection();
					String accountId = (String) selection.getFirstElement();
					Optional<MailAccount> selectedAccount = MailClientComponent.getMailClient().getAccount(accountId);
					if (selectedAccount.isPresent()
							&& MailClientComponent.getMailClient().testAccount(selectedAccount.get())) {
						MessageDialog.openInformation(getShell(), "Test", "Test erfolgreich.");
					} else {
						String message = MailClientComponent.getLastErrorMessage();
						MessageDialog.openError(getShell(), "Fehler", message);
					}
				}
			}
		});

		updateAccountsCombo();
		updateTemplatesCombo();
		loadSavedPreferences();
		return parentComposite;
	}

	protected void updateAccountsCombo() {
		if (MailClientComponent.getMailClient() != null) {
			accountsViewer.getControl().setEnabled(true);
			List<String> accountsInput = getSendMailAccounts();
			accountsInput.add(0, "");
			accountsViewer.setInput(accountsInput);
		} else {
			accountsViewer.getControl().setEnabled(false);
		}
	}

	private void updateTemplatesCombo() {
		List<Object> combined = new ArrayList<>();
		combined.add("");
		combined.addAll(MailTextTemplate.load());

		templatesViewer.setInput(combined);
		templatesViewer.refresh();
	}

	@Override
	public boolean performOk() {
		IStructuredSelection selectedAccount = accountsViewer.getStructuredSelection();
		String selectedTemplate = selectedTemplateName;
		if (selectedAccount != null) {
			ConfigServiceHolder.get().set(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT_APPOINTMENT,
					(String) selectedAccount.getFirstElement());
		}
		if (selectedTemplate != null) {
			String template = selectedTemplateName;

			ConfigServiceHolder.get().set(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT_APPOINTMENT_TEMPLATE, template);
		}
		return super.performOk();
	}

	private void loadSavedPreferences() {

		String savedAccount = ConfigServiceHolder.get().get(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT_APPOINTMENT,
				null);

		if (savedAccount != null) {
			accountsViewer.setSelection(new StructuredSelection(savedAccount));
		}

		String savedTemplate = ConfigServiceHolder.get()
				.get(PreferenceConstants.PREF_DEFAULT_MAIL_ACCOUNT_APPOINTMENT_TEMPLATE, null);
		if (savedTemplate != null) {

			List<ITextTemplate> templates = MailTextTemplate.load();
			for (ITextTemplate template : templates) {
				if (savedTemplate.equals(template.getName())) {
					templatesViewer.setSelection(new StructuredSelection(template));
					break;
				}
			}
		}

	}

	private List<String> getSendMailAccounts() {
		List<String> ret = new ArrayList<String>();
		List<String> accounts = MailClientComponent.getMailClient().getAccountsLocal();
		ret.addAll(accounts.stream().filter(aid -> MailClientComponent.getMailClient().getAccount(aid).isPresent())
				.filter(aid -> MailClientComponent.getMailClient().getAccount(aid).get().getType() == TYPE.SMTP)
				.collect(Collectors.toList()));
		accounts = MailClientComponent.getMailClient().getAccounts();
		ret.addAll(accounts.stream().filter(aid -> MailClientComponent.getMailClient().getAccount(aid).isPresent())
				.filter(aid -> MailClientComponent.getMailClient().getAccount(aid).get().getType() == TYPE.SMTP)
				.collect(Collectors.toList()));
		return ret;
	}

}
