package ch.elexis.core.mail.ui.dialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailConstants;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.MailTextTemplate;
import ch.elexis.core.mail.PreferenceConstants;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.mail.ui.handlers.OutboxUtil;
import ch.elexis.core.mail.ui.preference.SerializableFile;
import ch.elexis.core.mail.ui.preference.SerializableFileUtil;
import ch.elexis.core.mail.ui.preference.TextTemplates;
import ch.elexis.core.model.IBlobSecondary;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.documents.composites.DocumentsSelectionComposite;
import ch.elexis.core.ui.e4.fieldassist.IdentifiableContentProposal;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.data.Kontakt;
import jakarta.inject.Inject;

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
	private DocumentsSelectionComposite attachmentsSelection;
	private Button confidentialCheckbox;
	private String accountId;
	private String attachmentsString;
	private String documentsString;
	private boolean disableOutbox;
	private ComboViewer templatesViewer;
	private boolean doSend = true;
	private LocalDateTime sentTime;
	private String patID;

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
			if (accountId != null) {
				accountsViewer.setSelection(new StructuredSelection(accountId));
			}

			lbl = new Label(container, SWT.NONE);
			lbl.setText("An");
			toText = new Text(container, SWT.BORDER);
			toText.setText(toString);
			toText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			ContentProposalAdapter toAddressProposalAdapter = new ContentProposalAdapter(toText,
					new TextContentAdapter(), new MailAddressContentProposalProvider(),
					null, null);
			toText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.ARROW_DOWN) {
						toAddressProposalAdapter.openProposalPopup();
					}
					super.keyPressed(e);
				}
			});
			toAddressProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
			toAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
				@Override
				public void proposalAccepted(IContentProposal proposal) {
					if (proposal instanceof IdentifiableContentProposal) {
						@SuppressWarnings("unchecked")
						IdentifiableContentProposal<IContact> identifiableContentProposal = (IdentifiableContentProposal<IContact>) proposal;
						IContact contact = identifiableContentProposal.getIdentifiable();
						int index = MailAddressContentProposalProvider.getLastAddressIndex(toText.getText());
						StringBuilder sb = new StringBuilder();
						if (index != 0) {
							sb.append(toText.getText().substring(0, index)).append(", ").append(contact.getEmail());
						} else {
							sb.append(contact.getEmail());
						}
						toText.setText(sb.toString());
						toText.setSelection(toText.getText().length());
						attachments.setPostfix(toText.getText());
						Display.getDefault().asyncExec(() -> {
							toAddressProposalAdapter.closeProposalPopup();
						});
					}
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
			ccText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.ARROW_DOWN) {
						ccAddressProposalAdapter.openProposalPopup();
					}
					super.keyPressed(e);
				}
			});
			ccAddressProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
			ccAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
				@Override
				public void proposalAccepted(IContentProposal proposal) {
					@SuppressWarnings("unchecked")
					IdentifiableContentProposal<IContact> identifiableContentProposal = (IdentifiableContentProposal<IContact>) proposal;
					IContact contact = identifiableContentProposal.getIdentifiable();
					int index = MailAddressContentProposalProvider.getLastAddressIndex(ccText.getText());
					StringBuilder sb = new StringBuilder();
					if (index != 0) {
						sb.append(ccText.getText().substring(0, index)).append(", ").append(contact.getEmail());
					} else {
						sb.append(contact.getEmail());
					}
					ccText.setText(sb.toString());
					ccText.setSelection(ccText.getText().length());
					Display.getDefault().asyncExec(() -> {
						ccAddressProposalAdapter.closeProposalPopup();
					});
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

			lbl = new Label(container, SWT.NONE);
			lbl.setText("Vertraulich");
			confidentialCheckbox = new Button(container, SWT.CHECK); // Checkbox initialisieren
			getConfidentialCheckbox().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

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
			List<Object> templatesInput = new ArrayList<>();
			templatesInput.add("Keine Vorlage");
			templatesInput.addAll(MailTextTemplate.load());
			templatesViewer.setInput(templatesInput);
			templatesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					if (event.getStructuredSelection() != null
							&& event.getStructuredSelection().getFirstElement() instanceof ITextTemplate) {
						ITextTemplate selectedTemplate = (ITextTemplate) event.getStructuredSelection()
								.getFirstElement();

						setTemplateAttachments(selectedTemplate);

						textText.setText(textReplacement.performReplacement(ContextServiceHolder.get().getRootContext(),
								selectedTemplate.getTemplate()));
						if (selectedTemplate.getExtInfo(MailConstants.TEXTTEMPLATE_SUBJECT) != null) {
							subjectText.setText((String) selectedTemplate.getExtInfo(MailConstants.TEXTTEMPLATE_SUBJECT)
									+ StringUtils.SPACE + subjectString);
						}
					} else {
						textText.setText(StringUtils.EMPTY);
					}
					updateLayout();
				}
			});
			lbl = new Label(container, SWT.NONE);
			lbl.setText("Text");
			textText = new Text(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			textText.setLayoutData(gd);
			textText.setText(textString);

			attachments = new AttachmentsComposite(container, SWT.NONE);
			attachments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			attachments.setAttachments(attachmentsString);
			attachments.setDocuments(documentsString);
			attachments.setPostfix(toString);

			lbl = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			lbl = new Label(container, SWT.NONE);
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			lbl.setText("Dokument zum anhängen doppelklicken");

			Text searchField = new Text(container, SWT.BORDER);
			searchField.setMessage("Suche...");
			searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			searchField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String searchText = searchField.getText();
					attachmentsSelection.setFilter(searchText);
				}
			});

			attachmentsSelection = new DocumentsSelectionComposite(container, SWT.NONE);
			attachmentsSelection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			if (patID != null) {
				attachmentsSelection.setPatient(CoreModelServiceHolder.get().load(patID, IPatient.class).orElse(null));
			} else {
				attachmentsSelection.setPatient(ContextServiceHolder.get().getActivePatient().orElse(null));
			}

			attachmentsSelection.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					if (event.getSelection() instanceof IStructuredSelection && !event.getSelection().isEmpty()) {
						attachments.addDocument(
								(IDocument) ((IStructuredSelection) event.getSelection()).getFirstElement());
					}
				}
			});
	
			if (!doSend) {
				lbl.setVisible(false);
				searchField.setVisible(false);
				templatesViewer.getCombo().setVisible(false);
				attachments.setVisible(false);
				attachmentsSelection.setVisible(false);
			}

			if (accountId == null) {
				// set selected account for mandant
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

	public void doSend(boolean doSend) {
		this.doSend = doSend;
	}

	public void patId(String patID) {
		this.patID = patID;
	}
	private List<String> getSendMailAccounts() {
		List<String> ret = new ArrayList<>();
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
		Button outboxBtn = createButton(parent, -1, "Outbox", false);
		super.createButtonsForButtonBar(parent);
		if (getButton(IDialogConstants.OK_ID) != null) {
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (doSend) {
				okButton.setText("Senden");
			} else {
				okButton.setText(IDialogConstants.OK_LABEL);
			}
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

	private void setTemplateAttachments(ITextTemplate selectedTemplate) {
		IBlobSecondary textTemplate = CoreModelServiceHolder.get()
				.load(TextTemplates.NAMED_BLOB_PREFIX + selectedTemplate.getId(), IBlobSecondary.class).orElse(null);// $NON-NLS-1$
		if (textTemplate != null) {
			byte[] DBArrayList = textTemplate.getContent();

			Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
			List<String> attachmentPaths = new ArrayList<>();
			try {
				List<SerializableFile> deserializedContent = SerializableFileUtil.deserializeData(DBArrayList);
				for (SerializableFile serializableFile : deserializedContent) {
					Path tempFile = tempDir.resolve(serializableFile.getName());
					if (!Files.exists(tempFile)) {
						Files.write(tempFile, serializableFile.getData());
						tempFile.toFile().deleteOnExit();
					}
					attachmentPaths.add(tempFile.toString());
				}
				attachments.setAttachments(String.join(AttachmentsUtil.ATTACHMENT_DELIMITER, attachmentPaths));// $NON-NLS-1$
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getValidation() {
		if (getConfidentialCheckbox() != null && getConfidentialCheckbox().getSelection()) {
			subjectString = subjectText.getText() + " (Vertraulich)";
		} else {
			subjectString = subjectText.getText();
		}

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

	public Boolean doSend() {
		return doSend;
	}

	public Button getConfidentialCheckbox() {
		return confidentialCheckbox;
	}
}
