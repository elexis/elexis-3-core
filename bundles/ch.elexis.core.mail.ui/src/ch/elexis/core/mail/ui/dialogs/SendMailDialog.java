package ch.elexis.core.mail.ui.dialogs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.mail.ui.handlers.OutboxUtil;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.Kontakt;
import ch.medelexis.pea.PeaService;

public class SendMailDialog extends TitleAreaDialog {

	@Inject
	private ITextReplacementService textReplacement;

	private ComboViewer accountsViewer;
	private MailAccount account;
	private Text toText;
	private String toString = StringUtils.EMPTY;
	private Text ccText;
	private String ccString = StringUtils.EMPTY;
	private Text subjectText;
	private String subjectString = StringUtils.EMPTY;
	private Text textText;
	private String textString = StringUtils.EMPTY;
	private AttachmentsComposite attachments;
	private String accountId;
	private String attachmentsString;
	private String documentsString;
	private boolean disableOutbox;
	private ComboViewer templatesViewer;
	private boolean hideLabel = false;
	private String preselectedAccount = null;
	private LocalDateTime sentTime;
	private String emailTemplate = null;
	private List<Object> templatesInput = new ArrayList<>();
	private boolean autoSend;
	private String peaUrl;

	private String time = null;

	private String bereich;

	public SendMailDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		CoreUiUtil.injectServices(this);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (MailClientComponent.getMailClient() != null) {
			setTitle("E-Mail versenden");
		} else {
			setTitle("E-Mail versand nicht möglich");
		}
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (MailClientComponent.getMailClient() != null) {
			Label lbl = new Label(container, SWT.NONE);

			lbl.setText("Von");
			accountsViewer = new ComboViewer(container);
			accountsViewer.setContentProvider(ArrayContentProvider.getInstance());
			accountsViewer.setLabelProvider(new LabelProvider());
			accountsViewer.setInput(getSendMailAccounts());
			accountsViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// Überprüfen, ob ein preselectedAccount vorhanden ist und versuchen, ihn
			// auszuwählen
			if (preselectedAccount != null && !preselectedAccount.isEmpty()) {
				List<String> availableAccounts = getSendMailAccounts();
				if (availableAccounts.contains(preselectedAccount)) {
					accountsViewer.setSelection(new StructuredSelection(preselectedAccount));
				}
			} else if (accountId != null) {
				accountsViewer.setSelection(new StructuredSelection(accountId));
			}

			lbl = new Label(container, SWT.NONE);
			lbl.setText("An");
			toText = new Text(container, SWT.BORDER);
			toText.setText(toString);
			toText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			ContentProposalAdapter toAddressProposalAdapter = new ContentProposalAdapter(toText,
					new TextContentAdapter(), new MailAddressContentProposalProvider(), null, null);
			toAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
				@Override
				public void proposalAccepted(IContentProposal proposal) {
					int index = MailAddressContentProposalProvider.getLastAddressIndex(toText.getText());
					StringBuilder sb = new StringBuilder();
					if (index != 0) {
						sb.append(toText.getText().substring(0, index)).append(", ").append(proposal.getContent());
					} else {
						sb.append(proposal.getContent());
					}
					toText.setText(sb.toString());
					toText.setSelection(toText.getText().length());
					attachments.setPostfix(toText.getText());
				}
			});
			MenuManager menuManager = new MenuManager();
			menuManager.add(new Action("email zu Kontakt") {
				@Override
				public void run() {
					KontaktSelektor selector = new KontaktSelektor(getShell(), Kontakt.class, "Kontakt auswahl",
							"Kontakt für die E-Mail Adresse auswählen", Kontakt.DEFAULT_SORT);
					if (selector.open() == Dialog.OK) {
						Kontakt selected = (Kontakt) selector.getSelection();
						selected.set(Kontakt.FLD_E_MAIL, toText.getSelectionText());
					}
				}

				@Override
				public boolean isEnabled() {
					String text = toText.getSelectionText();
					return text != null && !text.isEmpty() && text.contains("@");
				}
			});
			menuManager.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					IContributionItem[] items = manager.getItems();
					for (IContributionItem iContributionItem : items) {
						iContributionItem.update();
					}
				}
			});
			toText.setMenu(menuManager.createContextMenu(toText));

			lbl = new Label(container, SWT.NONE);
			lbl.setText("Cc");
			ccText = new Text(container, SWT.BORDER);
			ccText.setText(ccString);
			ccText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			ContentProposalAdapter ccAddressProposalAdapter = new ContentProposalAdapter(ccText,
					new TextContentAdapter(), new MailAddressContentProposalProvider(), null, null);
			ccAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
				@Override
				public void proposalAccepted(IContentProposal proposal) {
					int index = MailAddressContentProposalProvider.getLastAddressIndex(ccText.getText());
					StringBuilder sb = new StringBuilder();
					if (index != 0) {
						sb.append(ccText.getText().substring(0, index)).append(", ").append(proposal.getContent());
					} else {
						sb.append(proposal.getContent());
					}
					ccText.setText(sb.toString());
					ccText.setSelection(ccText.getText().length());
				}
			});
			menuManager = new MenuManager();
			menuManager.add(new Action("email zu Kontakt") {
				@Override
				public void run() {
					KontaktSelektor selector = new KontaktSelektor(getShell(), Kontakt.class, "Kontakt auswahl",
							"Kontakt für die E-Mail Adresse auswählen", Kontakt.DEFAULT_SORT);
					if (selector.open() == Dialog.OK) {
						Kontakt selected = (Kontakt) selector.getSelection();
						selected.set(Kontakt.FLD_E_MAIL, ccText.getSelectionText());
					}
				}

				@Override
				public boolean isEnabled() {
					String text = ccText.getSelectionText();
					return text != null && !text.isEmpty() && text.contains("@");
				}
			});
			menuManager.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					IContributionItem[] items = manager.getItems();
					for (IContributionItem iContributionItem : items) {
						iContributionItem.update();
					}
				}
			});
			ccText.setMenu(menuManager.createContextMenu(ccText));

			lbl = new Label(container, SWT.NONE);
			lbl.setText("Betreff");
			subjectText = new Text(container, SWT.BORDER);
			subjectText.setText(subjectString);
			subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			attachments = new AttachmentsComposite(container, SWT.NONE);
			attachments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			attachments.setAttachments(attachmentsString);
			attachments.setDocuments(documentsString);
			attachments.setPostfix(toString);

			lbl = new Label(container, SWT.NONE);
			lbl.setText("Vorlage");
			templatesViewer = new ComboViewer(container);
			templatesViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			templatesViewer.setContentProvider(new ArrayContentProvider());
			templatesViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof ITextTemplate) {
						return ((ITextTemplate) element).getName() + (((ITextTemplate) element).getMandator() != null
								? " (" + ((ITextTemplate) element).getMandator().getLabel() + ")"
								: StringUtils.EMPTY);
					}
					return super.getText(element);
				}
			});

			templatesInput.add("Keine Vorlage");
			templatesInput.addAll(MailTextTemplate.load());
			templatesViewer.setInput(templatesInput);

			if (hideLabel) {
				lbl.setVisible(false);
				templatesViewer.getControl().setVisible(false);
			} else {
				templatesViewer.getControl().setVisible(true);
			}
			lbl = new Label(container, SWT.NONE);
			lbl.setText("Text");
			textText = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			textText.setLayoutData(gd);
			textText.setText(textString);

			if (emailTemplate != null && !emailTemplate.isEmpty()) {
				List<Object> availableTemplates = templatesInput;
				Optional<Object> matchingTemplate = availableTemplates.stream()
						.filter(temp -> temp instanceof ITextTemplate
								&& ((ITextTemplate) temp).getName().trim().equals(emailTemplate.trim()))
						.findFirst();

				if (matchingTemplate.isPresent()) {
					templatesViewer.setSelection(new StructuredSelection(matchingTemplate.get()));
					applySelectedTemplate(matchingTemplate.get());
				}
			}

			templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					if (event.getSelection() instanceof StructuredSelection) {
						StructuredSelection selection = (StructuredSelection) event.getSelection();
						applySelectedTemplate(selection.getFirstElement());
					}
				}
			});

			if (accountId == null) {
				IMandator selectedMandant = ContextServiceHolder.get().getActiveMandator().orElse(null);
				if (selectedMandant != null) {
					List<String> accounts = MailClientComponent.getMailClient().getAccountsLocal();
					Optional<String> mandantAccount = accounts.stream()
							.filter(aid -> MailClientComponent.getMailClient().getAccount(aid).isPresent())
							.filter(aid -> MailClientComponent.getMailClient().getAccount(aid).get()
									.isForMandant(selectedMandant.getId()))
							.findFirst();
					if (!mandantAccount.isPresent()) {
						accounts = MailClientComponent.getMailClient().getAccounts();
						mandantAccount = accounts.stream()
								.filter(aid -> MailClientComponent.getMailClient().getAccount(aid).isPresent())
								.filter(aid -> MailClientComponent.getMailClient().getAccount(aid).get()
										.isForMandant(selectedMandant.getId()))
								.findFirst();
					}
					if (mandantAccount.isPresent()) {
						accountsViewer.setSelection(new StructuredSelection(mandantAccount.get()));
					}
				}
			}
			// set template if there is no text, and a default configured
			String defaultTemplateId = ConfigServiceHolder.get().get(PreferenceConstants.PREF_DEFAULT_TEMPLATE, null);
			if (defaultTemplateId != null && StringUtils.isEmpty(textText.getText())) {
				for (Object object : templatesInput) {
					if (object instanceof ITextTemplate && ((ITextTemplate) object).getId().equals(defaultTemplateId)) {
						Display.getDefault().asyncExec(() -> {
							templatesViewer.setSelection(new StructuredSelection(object));
						});
					}
				}
			}
			if (sentTime != null) {
				accountsViewer.getCombo().setEnabled(false);
				toText.setEditable(false);
				ccText.setEditable(false);
				subjectText.setEditable(false);
				templatesViewer.getCombo().setEnabled(false);
				textText.setEditable(false);
				attachments.setEnabled(false);
			}
		}
		updateLayout();
		return area;
	}

	public void setAttachments(String attachments) {
		this.attachments.setAttachments(attachments);
		getShell().layout(true, true);
	}

	public void setDocuments(String documents) {
		this.attachments.setDocuments(documents);
		getShell().layout(true, true);
	}

	public void setTo(String to) {
		if (to != null && !to.isEmpty()) {
			toString = to;
		}
	}

	public void setSubject(String subject) {
		if (subject != null && !subject.isEmpty()) {
			subjectString = subject;
		}
	}

	public void setText(String text) {
		if (text != null && !text.isEmpty()) {
			textString = text;
		}
		updateLayout();
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

	@Override
	protected void okPressed() {
		String validation = getValidation();
		if (validation != null) {
			setErrorMessage(validation);
			return;
		}
		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button outboxBtn = createButton(parent, -1, "in Outbox ablegen", false);
		super.createButtonsForButtonBar(parent);
		if (getButton(IDialogConstants.OK_ID) != null) {
			Button okButton = getButton(IDialogConstants.OK_ID);
			okButton.setText("Senden");
			if (sentTime != null) {
				setTitle("E-Mail Anzeige");
				setMessage("Diese E-Mail wurde versendet am "
						+ sentTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
				// hide all buttons if already sent
				for (Control control : parent.getChildren()) {
					if (control instanceof Button) {
						Button button = (Button) control;
						((GridData) button.getLayoutData()).exclude = true;
						button.setVisible(false);
					}
				}
			}

			if (autoSend) {
				Display.getCurrent().timerExec(0, () -> {
					if (okButton != null && !okButton.isDisposed()) {
						okButton.notifyListeners(SWT.Selection, new Event());
					}
				});
			}
		}

		outboxBtn.setEnabled(!disableOutbox && OutboxUtil.isOutboxAvailable());
		outboxBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String validation = getValidation();
				if (validation != null) {
					setErrorMessage(validation);
					return;
				} else {
					setErrorMessage(null);
				}
				createOutboxElement();
			}

			private void createOutboxElement() {
				MailMessage message = new MailMessage().to(getTo()).cc(getCc()).subject(getSubject()).text(getText());
				message.setAttachments(attachments.getAttachments());
				message.setDocuments(attachments.getDocuments());
				Optional<ITaskDescriptor> descriptor = TaskUtil.createSendMailTaskDescriptor(account.getId(), message);
				if (descriptor.isPresent()) {
					OutboxUtil.getOrCreateElement(descriptor.get(), false);
				}
				// close dialog with cancel status, do not send mail
				cancelPressed();
			}
		});

		parent.layout();
	}

	private String getValidation() {
		StructuredSelection accountSelection = (StructuredSelection) accountsViewer.getSelection();
		if (accountSelection == null || accountSelection.isEmpty()) {
			return "Kein Konto ausgewählt.";
		}
		String accountId = (String) accountSelection.getFirstElement();
		Optional<MailAccount> optionalAccount = MailClientComponent.getMailClient().getAccount(accountId);
		if (!optionalAccount.isPresent()) {
			return "Kein Konto ausgewählt.";
		} else {
			account = optionalAccount.get();
		}

		String to = toText.getText();
		if (to == null || to.isEmpty()) {
			return "Keine an E-Mail Adresse.";
		}
		toString = to;

		ccString = ccText.getText();

		subjectString = subjectText.getText();

		textString = textText.getText();

		return null;
	}

	public String getTo() {
		return toString;
	}

	public String getCc() {
		return ccString;
	}

	public void setCc(String cc) {
		this.ccString = cc;
	}

	public String getSubject() {
		return subjectString;
	}

	public String getText() {
		return textString;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public MailAccount getAccount() {
		return account;
	}

	public String getAttachmentsString() {
		return attachments.getAttachments();
	}

	public String getDocumentsString() {
		return attachments.getDocuments();
	}

	public void setHideLabel(boolean hide) {
		this.hideLabel = hide;
	}

	public void setAccount(String account) {
		this.preselectedAccount = account;
	}

	public void setMailMessage(MailMessage message) {
		setTo(StringUtils.defaultString(message.getTo()));
		setCc(StringUtils.defaultString(message.getCc()));
		setSubject(StringUtils.defaultString(message.getSubject()));
		setText(StringUtils.defaultString(message.getText()));
		attachmentsString = message.getAttachmentsString();
		documentsString = message.getDocumentsString();

		updateLayout();
	}

	private void updateLayout() {
		if (textText != null && !textText.isDisposed()) {
			GridData gd = (GridData) textText.getLayoutData();
			String text = textText.getText();
			boolean defaultSet = false;
			if (StringUtils.isNotBlank(text)) {
				String[] lines = text.split(StringUtils.LF);
				if (lines.length > 12) {
					defaultSet = true;
					gd.heightHint = SWT.DEFAULT;
				}
			}
			if (!defaultSet) {
				gd.heightHint = 250;
			}
			if (getShell() != null && !getShell().isDisposed()) {
				getShell().layout();
			}
		}
	}

	public void disableOutbox() {
		this.disableOutbox = true;
	}

	public void setDocumentsString(String documents) {
		this.documentsString = documents;
	}

	public void setAttachmentsString(String attachments) {
		this.attachmentsString = attachments;
	}

	public void sent(LocalDateTime sentTime) {
		this.sentTime = sentTime;
	}

	public void setTemplate(String template) {
		this.emailTemplate = template;

	}

	public void setTime(String time) {
		this.time = time;

	}

	public void setBereich(String bereich) {
		this.bereich = bereich;

	}

	public void setAutoSend(boolean autoSend) {
		this.autoSend = autoSend;
	}

	private void applySelectedTemplate(Object selectedTemplate) {
		if (selectedTemplate instanceof ITextTemplate) {
			ITextTemplate template = (ITextTemplate) selectedTemplate;
			String templateText = template.getTemplate();
			Optional<PeaService> peaService = OsgiServiceUtil.getService(PeaService.class);

			if (peaService.isPresent()) {
				peaUrl = peaService.get().getPeaUrl();
			}

			String[] dateAndTime = time.split(" ");
			String[] dateParts = dateAndTime[0].split("\\.");
			String[] timeParts = dateAndTime[1].split(":");

			int day = Integer.parseInt(dateParts[0]);
			int month = Integer.parseInt(dateParts[1]);
			int year = Integer.parseInt(dateParts[2]);
			int hour = Integer.parseInt(timeParts[0]);
			int minute = Integer.parseInt(timeParts[1]);
			int second = Integer.parseInt(timeParts[2]);

			LocalDateTime dateTimeFrom = LocalDateTime.of(year, month, day, hour, minute, second);// Holen Sie sich das

			templateText = replacePlaceholders(templateText, dateTimeFrom, peaUrl, bereich);

			// Führen Sie die restlichen Ersetzungen durch
			textText.setText(
					textReplacement.performReplacement(ContextServiceHolder.get().getRootContext(), templateText));
		} else {
			textText.setText(StringUtils.EMPTY);
		}
		updateLayout();
	}

	private String replacePlaceholders(String text, LocalDateTime dateTime, String peaUrl, String bereich) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		text = text.replace("[Termin.Tag]", dateTime.format(dateFormatter));
		text = text.replace("[Termin.Zeit]", dateTime.format(timeFormatter));
		text = text.replace("[Termin.Bereich]", bereich);
		if (peaUrl != null) {
			text = text.replace("[Termin.PEAUrl]", peaUrl);
		} else {
			text = text.replace("[Termin.PEAUrl]", "");
		}

		return text;
	}

}