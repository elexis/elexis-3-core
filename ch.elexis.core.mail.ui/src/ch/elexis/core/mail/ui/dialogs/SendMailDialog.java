package ch.elexis.core.mail.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.ui.client.MailClientComponent;

public class SendMailDialog extends TitleAreaDialog {

	private ComboViewer accountsViewer;
	private MailAccount account;
	private Text toText;
	private String toString = "";
	private Text subjectText;
	private String subjectString = "";
	private Text textText;
	private String textString = "";
	private Label attachmentsLabel;
	private String attachmentsString = "";
	
	public SendMailDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
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
			lbl.setText("Konto");
			accountsViewer = new ComboViewer(container);
			accountsViewer.setContentProvider(ArrayContentProvider.getInstance());
			accountsViewer.setLabelProvider(new LabelProvider());
			accountsViewer.setInput(getSendMailAccounts());
			accountsViewer.getControl()
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			lbl = new Label(container, SWT.NONE);
			lbl.setText("An");
			toText = new Text(container, SWT.BORDER);
			toText.setText(toString);
			toText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			lbl = new Label(container, SWT.NONE);
			lbl.setText("Betreff");
			subjectText = new Text(container, SWT.BORDER);
			subjectText.setText(subjectString);
			subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			lbl = new Label(container, SWT.NONE);
			lbl.setText("Anhang");
			attachmentsLabel = new Label(container, SWT.NONE);
			attachmentsLabel.setText(attachmentsString);
			attachmentsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			lbl = new Label(container, SWT.NONE);
			lbl.setText("Text");
			textText = new Text(container, SWT.BORDER | SWT.MULTI);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 128;
			textText.setLayoutData(gd);
		}
		
		return area;
	}
	
	public void setAttachments(String attachments){
		if (attachments != null && !attachments.isEmpty()) {
			attachmentsString = attachments;
		}
	}
	
	public void setTo(String to){
		if (to != null && !to.isEmpty()) {
			toString = to;
		}
	}
	
	public void setSubject(String subject){
		if (subject != null && !subject.isEmpty()) {
			subjectString = subject;
		}
	}
	
	public void setText(String text){
		if (text != null && !text.isEmpty()) {
			textString = text;
		}
	}
	
	private List<String> getSendMailAccounts(){
		List<String> ret = new ArrayList<String>();
		List<String> accounts = MailClientComponent.getMailClient().getAccounts();
		for (String accountId : accounts) {
			Optional<MailAccount> accountOptional =
				MailClientComponent.getMailClient().getAccount(accountId);
			if (accountOptional.isPresent()) {
				if (accountOptional.get().getType() == TYPE.SMTP) {
					ret.add(accountId);
				}
			}
		}
		return ret;
	}
	
	@Override
	protected void okPressed(){
		String validation = getValidation();
		if (validation != null) {
			setErrorMessage(validation);
			return;
		}
		super.okPressed();
	}
	
	private String getValidation(){
		StructuredSelection accountSelection = (StructuredSelection) accountsViewer.getSelection();
		if (accountSelection == null || accountSelection.isEmpty()) {
			return "Kein Konto ausgewählt.";
		}
		String accountId = (String) accountSelection.getFirstElement();
		Optional<MailAccount> optionalAccount =
			MailClientComponent.getMailClient().getAccount(accountId);
		if (!optionalAccount.isPresent()) {
			return "Kein Konto ausgewählt.";
		} else {
			account = optionalAccount.get();
		}
		
		String to = toText.getText();
		if(to == null || to.isEmpty()) {
			return "Keine an E-Mail Adresse.";
		}
		toString = to;
		
		subjectString = subjectText.getText();
		
		textString = textText.getText();
		
		return null;
	}
	
	public String getTo(){
		return toString;
	}
	
	public String getSubject(){
		return subjectString;
	}
	
	public String getText(){
		return textString;
	}
	
	public MailAccount getAccount(){
		return account;
	}
	
}
