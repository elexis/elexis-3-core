package ch.elexis.core.mail.ui.dialogs;

import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.model.IDocument;

public class AttachmentsComposite extends Composite {
	
	private Label attachmentsLabel;
	private String attachmentsString = "";
	private String attachments;
	private String documents;
	private Command createRocheLaborCommand;
	private String postfix;
	
	public AttachmentsComposite(Composite parent, int style){
		super(parent, style);
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		createRocheLaborCommand =
			commandService.getCommand("at.medevit.elexis.roche.labor.CreatePdfSelection");
		
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(3, false));
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText("Anhang");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		attachmentsLabel = new Label(this, SWT.NONE);
		attachmentsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		ToolBarManager mgr = new ToolBarManager();
		mgr.add(new AddAttachmentAction(this));
		mgr.add(new RemoveAttachmentAction(this));
		if (createRocheLaborCommand != null && createRocheLaborCommand.isEnabled()) {
			mgr.add(new LaborAttachmentAction(this, createRocheLaborCommand));
		}
		ToolBar toolbar = mgr.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
	}
	
	public String getAttachmentNames(String attachmentAsString){
		StringBuilder build = new StringBuilder();
		if (attachmentAsString != null) {
			String[] attachments = attachmentAsString.split(",\n");
			for (String f : attachments) {
				if (build.length() > 0) {
					build.append(",\n");
				}
				build.append(Paths.get(f).getFileName());
			}
		}
		return build.toString();
	}
	
	/**
	 * String containing references to files.
	 * 
	 * @param attachments
	 */
	public void setAttachments(String attachments){
		this.attachments = attachments;
		updateAttachmentsLabel();
	}
	
	private void updateAttachmentsLabel(){
		if (StringUtils.isNotBlank(attachments)) {
			attachmentsString = attachments.replaceAll(":::", ",\n");
		}
		if (StringUtils.isNotBlank(documents)) {
			// replace if no attachments, else append
			if (StringUtils.isNotBlank(attachments)) {
				attachmentsString +=
					AttachmentsUtil.toAttachments(documents).replaceAll(":::", ",\n");
			} else {
				attachmentsString =
					AttachmentsUtil.toAttachments(documents).replaceAll(":::", ",\n");
			}
		}
		attachmentsLabel.setText(getAttachmentNames(attachmentsString));
		attachmentsLabel.setToolTipText(attachmentsString);
		getParent().layout(true, true);
	}
	
	public void addDocument(IDocument document){
		if (document != null) {
			if (StringUtils.isBlank(documents)) {
				documents =
					AttachmentsUtil.getDocumentsString(Collections.singletonList(document));
			} else {
				documents +=
					":::" + AttachmentsUtil.getDocumentsString(Collections.singletonList(document));
			}
		}
		updateAttachmentsLabel();
	}
	
	/**
	 * String containing references to {@link IDocument}s.
	 * 
	 * @param documents
	 */
	public void setDocuments(String documents){
		this.documents = documents;
		updateAttachmentsLabel();
	}
	
	public String getAttachments(){
		return attachments;
	}
	
	public String getDocuments(){
		return documents;
	}
	
	public void setPostfix(String text){
		this.postfix = text;
	}
	
	public String getPostfix(){
		return postfix;
	}
}
