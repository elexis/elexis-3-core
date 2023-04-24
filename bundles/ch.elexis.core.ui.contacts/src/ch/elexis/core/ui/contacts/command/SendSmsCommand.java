package ch.elexis.core.ui.contacts.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IMessageService;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.medelexis.messaging.twilio.Constants;
import ch.medelexis.messaging.twilio.ui.dialog.SendSmsDialog;

public class SendSmsCommand extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPatient patient = ContextServiceHolder.get().getActivePatient().orElse(null);

		if (patient != null) {
			SendSmsDialog dialog = new SendSmsDialog(HandlerUtil.getActiveShell(event), "SMS versenden",
					"SMS an " + patient.getLastName() + " " + patient.getFirstName() + " senden");

			if (dialog.open() == Dialog.OK) {
				String contactPhoneNumber = patient.getMobile();
				IMessageService messageService = OsgiServiceUtil.getService(IMessageService.class).orElseThrow();

				TransientMessage messageReminder = messageService.prepare(Constants.CONFIG_TWILIO_SENDERNAME,
						"sms:" + contactPhoneNumber);
				messageReminder.setMessageText(dialog.getMessageText());
				messageReminder.setAlllowExternal(true);

				IStatus status = messageService.send(messageReminder);

				if (!status.isOK()) {
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", status.getMessage());
				} else {
					MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Versendet",
							"Die SMS wurde erfolgreich versendet");
				}

			}
		}
		return null;
	}

}
