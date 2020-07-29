package ch.elexis.core.mail.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.gson.Gson;

import ch.elexis.core.mail.IMailClient;
import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;

public class SendMailRunnable implements IIdentifiedRunnable {
	
	public static final String RUNNABLE_ID = "sendMailFromContext";
	
	private Gson gson = new Gson();
	
	private IMailClient mailClient;
	
	public SendMailRunnable(IMailClient mailClient){
		this.mailClient = mailClient;
		this.gson = new Gson();
	}
	
	@Override
	public String getId(){
		return RUNNABLE_ID;
	}
	
	@Override
	public String getLocalizedDescription(){
		return "Send an email using the information from the task context";
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put("accountId", RunContextParameter.VALUE_MISSING_REQUIRED);
		defaultRunContext.put("message",
			RunContextParameter.VALUE_MISSING_REQUIRED);
		return defaultRunContext;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		String accountId = (String) runContext.get("accountId");
		MailMessage message =
			getRunContextEntryTyped(runContext, "message", MailMessage.class);
		Optional<MailAccount> account = mailClient.getAccount(accountId);
		if (message != null && account.isPresent()) {
			mailClient.sendMail(account.get(), message);
		}
		return null;
	}
	
	private <T> T getRunContextEntryTyped(Map<String, Serializable> map, String key,
		Class<T> clazz){
		try {
			String valueToString = JSONObject.valueToString(map.get(key));
			return gson.fromJson(valueToString, clazz);
		} catch (JSONException e) {
			// do nothing
		}
		return null;
	}
}
