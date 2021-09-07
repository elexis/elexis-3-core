package ch.elexis.core.mail.ui.dialogs;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.mail.AttachmentsUtil;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.ui.icons.Images;

public class AttachmentsComposite extends Composite {
	
	private String attachments;
	private String documents;
	private Command createRocheLaborCommand;
	private String postfix;
	
	private Composite attachmentsParent;
	
	public AttachmentsComposite(Composite parent, int style){
		super(parent, style);
		this.setData("org.eclipse.e4.ui.css.CssClassName", "CustomComposite");
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
		attachmentsParent = new Composite(this, SWT.NONE);
		attachmentsParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		attachmentsParent.setLayout(new GridLayout(2, false));
		
		ToolBarManager mgr = new ToolBarManager();
		mgr.add(new AddAttachmentAction(this));
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
		updateAttachments();
	}
	
	private void updateAttachments(){
		attachmentsParent.setRedraw(false);
		// clear all labels and rebuild
		for (Control control : attachmentsParent.getChildren()) {
			control.dispose();
		}
		if (StringUtils.isNotBlank(attachments)) {
			String[] attachmentsParts = attachments.split(":::");
			for (String string : attachmentsParts) {
				Label label = new Label(attachmentsParent, SWT.NONE);
				label.setText(FilenameUtils.getName(string));
				label.setData(string);
				label.setToolTipText("Mit Doppelklick öffnen (keine Änderungen)");
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDoubleClick(MouseEvent e){
						File file = new File((String) label.getData());
						file.setReadOnly();
						Program.launch((String) label.getData());
					}
				});
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				Label remove = new Label(attachmentsParent, SWT.NONE);
				remove.setImage(Images.IMG_DELETE.getImage());
				remove.setData(string);
				remove.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseUp(MouseEvent e){
						List<String> removeParts = Arrays.asList(getAttachments().split(":::"));
						String removedString =
							removeParts.stream().filter(part -> !part.equals(remove.getData()))
							.collect(Collectors.joining(":::"));
						setAttachments(removedString);
					}
				});
				remove.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			}
		}
		if (StringUtils.isNotBlank(documents)) {
			String[] documentsParts = documents.split(":::");
			for (String string : documentsParts) {
				Label label = new Label(attachmentsParent, SWT.NONE);
				String tmpFile = AttachmentsUtil.toAttachment(string);
				if (!tmpFile.endsWith(".pdf")) {
					MessageDialog.openWarning(getShell(), "Warnung",
						"Dokument " + FilenameUtils.getName(tmpFile)
							+ " konnte nicht konvertiert werden, bzw. ist kein pdf.\nBitte prüfen ob ein editierbares Dokument versendet werden soll.");
				}
				label.setText(FilenameUtils.getName(tmpFile));
				label.setData(string);
				label.setToolTipText("Mit Doppelklick öffnen (keine Änderungen)");
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDoubleClick(MouseEvent e){
						File file = new File(tmpFile);
						file.setReadOnly();
						Program.launch(tmpFile);
					}
				});
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				Label remove = new Label(attachmentsParent, SWT.NONE);
				remove.setImage(Images.IMG_DELETE.getImage());
				remove.setData(string);
				remove.addMouseListener(new MouseAdapter() {
					public void mouseUp(MouseEvent e){
						List<String> removeParts = Arrays.asList(getDocuments().split(":::"));
						String removedString =
							removeParts.stream().filter(part -> !part.equals(remove.getData()))
								.collect(Collectors.joining(":::"));
						setDocuments(removedString);
					};
				});
				remove.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			}
		}
		attachmentsParent.setRedraw(true);
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
		updateAttachments();
	}
	
	/**
	 * String containing references to {@link IDocument}s.
	 * 
	 * @param documents
	 */
	public void setDocuments(String documents){
		this.documents = documents;
		updateAttachments();
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
