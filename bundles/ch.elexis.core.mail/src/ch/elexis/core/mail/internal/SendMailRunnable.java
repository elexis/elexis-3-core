package ch.elexis.core.mail.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;

public class SendMailRunnable implements IIdentifiedRunnable {
	
	public static final String RUNNABLE_ID = "sendMailFromContext"; //$NON-NLS-1$
	
	private IMailClient mailClient;
	
	public SendMailRunnable(IMailClient mailClient){
		this.mailClient = mailClient;
	}
	
	@Override
	public String getId(){
		return RUNNABLE_ID;
	}
	
	@Override
	public String getLocalizedDescription(){
		return Messages.SendMailRunnable_1;
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put("accountId", RunContextParameter.VALUE_MISSING_REQUIRED); //$NON-NLS-1$
		defaultRunContext.put("message", //$NON-NLS-1$
			RunContextParameter.VALUE_MISSING_REQUIRED);
		return defaultRunContext;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		String accountId = (String) runContext.get("accountId"); //$NON-NLS-1$
		MailMessage message = MailMessage.fromJson(runContext.get("message"));
		Optional<MailAccount> account = mailClient.getAccount(accountId);
		if (message != null && account.isPresent()) {
			mailClient.sendMail(account.get(), message);
		}
		return null;
	}
}
