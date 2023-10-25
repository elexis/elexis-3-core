package ch.elexis.core.mail.ui.handlers;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailAccount.TYPE;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.TaskUtil;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.data.Mandant;

/**
 * Handler for sending an Email without UI. The command execution returns a
 * String with the result of sending the mail, which is empty on success. The
 * mail can be specified via the command parameters.
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.accountid</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.mandant</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.attachments</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.to</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.subject</li>
 * <li>ch.elexis.core.mail.ui.sendMailNoUi.text</li> <br />
 * Attachments are sent, and not deleted afterwards.
 *
 * @author thomas
 *
 */
public class SendMailNoUiHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String doSend = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.doSend");
		if (Boolean.valueOf(doSend)) {
	        Optional<?> descriptor = ContextServiceHolder.get().getNamed("sendMailDialog.taskDescriptor");
	        if (descriptor != null && descriptor.isPresent()) {
	            OutboxUtil.getOrCreateElement((ITaskDescriptor) descriptor.get(), true);
	            EncounterUtil.addMailToEncounter((ITaskDescriptor) descriptor.get());
				ContextServiceHolder.get().getRootContext().setNamed("sendMailDialog.taskDescriptor", null);
	        }
			return null;
	    }
		MailAccount mailAccount = null;
		String accountId = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.accountid");
		if (StringUtils.isNoneBlank(accountId)) {
			mailAccount = MailClientComponent.getMailClient().getAccount(accountId).orElse(null);
		}
		if (mailAccount == null) {
			mailAccount = getMailAccount(event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.mandant"));
			if (mailAccount == null) {
				Mandant mandant = Mandant.load(event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.mandant"));
				return "No account for mandant [" + mandant.getLabel(false) + "]";
			}
		}

		String attachments = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.attachments");
		String to = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.to");
		if (StringUtils.isBlank(to)) {
			return "No to address";
		}
		String subject = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.subject");
		String text = event.getParameter("ch.elexis.core.mail.ui.sendMailNoUi.text");

		MailMessage message = new MailMessage().to(to).subject(StringUtils.defaultString(subject))
				.text(StringUtils.defaultString(text));
		if (attachments != null && !attachments.isEmpty()) {
			message.setAttachments(attachments);
		}
		Optional<ITaskDescriptor> taskDescriptor = TaskUtil.createSendMailTaskDescriptor(mailAccount.getId(), message);
		if (taskDescriptor.isPresent()) {
			try {
				ITask task = TaskUtil.executeTaskSync(taskDescriptor.get(), new NullProgressMonitor());
				if (task.isSucceeded()) {
					OutboxUtil.getOrCreateElement(taskDescriptor.get(), true);
					EncounterUtil.addMailToEncounter(taskDescriptor.get());
					return null;
				} else {
					return MailClientComponent.getLastErrorMessage();
				}
			} catch (TaskException e) {
				LoggerFactory.getLogger(TaskUtil.class).error("Error executing mail task", e);
			}
		}
		return "Error executing mail task";
	}

	private MailAccount getMailAccount(String mandantId) {
		List<String> accounts = MailClientComponent.getMailClient().getAccounts();
		for (String string : accounts) {
			Optional<MailAccount> account = MailClientComponent.getMailClient().getAccount(string);
			if (account.isPresent() && account.get().getType() == TYPE.SMTP && account.get().isForMandant(mandantId)) {
				return account.get();
			}
		}
		return null;
	}
}
