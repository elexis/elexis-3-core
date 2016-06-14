package ch.elexis.core.mail.ui.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.mail.ui.dialogs.SendMailDialog;

/**
 * Handler for sending an Email. The mail can be specified via the command parameters.
 * <li>ch.elexis.core.mail.ui.sendMail.attachments</li>
 * <li>ch.elexis.core.mail.ui.sendMail.to</li>
 * <li>ch.elexis.core.mail.ui.sendMail.subject</li>
 * <li>ch.elexis.core.mail.ui.sendMail.text</li> <br />
 * Additionally a Dialog is displayed to configure the mail, and the account to use for sending. The
 * execution blocks until the sending is done. Attachments are sent, and not deleted afterwards.
 * 
 * @author thomas
 *
 */
public class SendMailHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{

		SendMailDialog sendMailDialog = new SendMailDialog(HandlerUtil.getActiveShell(event));
		String attachments = event.getParameter("ch.elexis.core.mail.ui.sendMail.attachments");
		if (attachments != null) {
			sendMailDialog.setAttachments(attachments);
		}
		String to = event.getParameter("ch.elexis.core.mail.ui.sendMail.to");
		if (to != null) {
			sendMailDialog.setTo(to);
		}
		String subject = event.getParameter("ch.elexis.core.mail.ui.sendMail.subject");
		if (subject != null) {
			sendMailDialog.setSubject(subject);
		}
		String text = event.getParameter("ch.elexis.core.mail.ui.sendMail.text");
		if (text != null) {
			sendMailDialog.setText(text);
		}
		
		if (sendMailDialog.open() == Dialog.OK) {
			MailMessage message = new MailMessage().to(sendMailDialog.getTo())
				.subject(sendMailDialog.getSubject()).text(sendMailDialog.getText());
			if (attachments != null && !attachments.isEmpty()) {
				List<File> attachmentList = getAttachmentsList(attachments);
				for (File file : attachmentList) {
					message.addAttachment(file);
				}
			}
			Display display = Display.getDefault();
			try {
				new ProgressMonitorDialog(HandlerUtil.getActiveShell(event)).run(true, false,
					new IRunnableWithProgress() {
						
						@Override
						public void run(IProgressMonitor monitor)
							throws InvocationTargetException, InterruptedException{
							monitor.beginTask("Send Mail ...", IProgressMonitor.UNKNOWN);
							if (MailClientComponent.getMailClient()
								.sendMail(sendMailDialog.getAccount(), message)) {
								// new OutputLog();
								display.asyncExec(new Runnable() {
									@Override
									public void run(){
										MessageDialog.openInformation(
											HandlerUtil.getActiveShell(event), "E-Mail versand",
											"E-Mail erfolgreich versendet.");
									}
								});
							} else {
								display.asyncExec(new Runnable() {
									@Override
									public void run(){
										String errorMessage =
											MailClientComponent.getLastErrorMessage();
										MessageDialog.openError(HandlerUtil.getActiveShell(event),
											"Fehler", errorMessage);
									}
								});
							}
							monitor.done();
						}
					});
			} catch (InvocationTargetException | InterruptedException e) {
				MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
					"Versenden konnte nicht gestartet werden.");
			}
		}
		
		return null;
	}
	
	private List<File> getAttachmentsList(String attachments){
		List<File> ret = new ArrayList<File>();
		if (attachments != null && !attachments.isEmpty()) {
			String[] parts = attachments.split(",");
			for (String string : parts) {
				ret.add(new File(string));
			}
		}
		return ret;
	}
}
