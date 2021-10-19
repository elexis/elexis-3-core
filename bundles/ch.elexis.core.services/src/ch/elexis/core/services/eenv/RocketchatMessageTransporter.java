package ch.elexis.core.services.eenv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IMessageTransporter;
import ch.elexis.core.services.internal.Bundle;

@Component
public class RocketchatMessageTransporter implements IMessageTransporter {
	
	/**
	 * The webhook integration token to use to send messages as station to rocketchat.
	 */
	public static final String CFG_ROCKETCHAT_STATION_INTEGRATION_TOKEN =
		"rocketchat-station-integration-token";
	
	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;
	
	@Reference
	private IConfigService configService;
	
	@Override
	public String getUriScheme(){
		return "rocketchat";
	}
	
	@Override
	public boolean isExternal(){
		return false;
	}
	
	@Override
	public IStatus send(TransientMessage message){
		return sendFromStationSender(message);
	}
	
	private IStatus sendFromStationSender(TransientMessage message){
		String integrationToken =
			configService.getLocal(CFG_ROCKETCHAT_STATION_INTEGRATION_TOKEN, null);
		if (integrationToken != null) {
			try {
				URL integrationUrl = new URL(
					elexisEnvironmentService.getRocketchatIntegrationBaseUrl() + integrationToken);
				
				String jsonMessage = prepareRocketchatMessage(message);
				return send(integrationUrl, jsonMessage.getBytes());
				
			} catch (IOException e) {
				return new Status(IStatus.ERROR, Bundle.ID, e.getMessage());
			}
		}
		
		return new Status(IStatus.ERROR, Bundle.ID,
			"No webhook integration token [" + CFG_ROCKETCHAT_STATION_INTEGRATION_TOKEN
				+ "] found in local config or malformed url.");
	}
	
	protected String prepareRocketchatMessage(TransientMessage message){
		
		String severity = message.getMessageCodes().get(MessageCode.Key.Severity);
		if (severity == null) {
			severity = MessageCode.Value.Severity_INFO;
		}
		
		StringBuilder header = new StringBuilder();
		header.append(severityToEmoji(severity) + " @"
			+ message.getReceiver().substring(message.getReceiver().indexOf(':') + 1));
		
		Set<Entry<String, String>> entrySet = message.getMessageCodes().entrySet();
		if (!entrySet.isEmpty()) {
			header.append(" | ");
			message.getMessageCodes().entrySet()
				.forEach(c -> header.append(c.getKey() + ":" + c.getValue() + " "));
		}
		
		Map<String, Object> params = new HashMap<>();
		params.put("color", severityToColor(severity));
		params.put("text", message.getMessageText());
		
		RocketchatMessage rocketchatMessage = new RocketchatMessage();
		rocketchatMessage.setSender(message.getSender());
		rocketchatMessage.setText(header.toString());
		rocketchatMessage.setAttachments(params);
		return new Gson().toJson(rocketchatMessage);
	}
	
	private IStatus send(URL url, byte[] postDataBytes) throws IOException{
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		con.getOutputStream().write(postDataBytes);
		
		int responseCode = con.getResponseCode();
		if (responseCode == 200) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, Bundle.ID,
			"Error sending, with response code: " + responseCode);
	}
	
	private String severityToColor(String severity){
		switch (severity) {
		case MessageCode.Value.Severity_WARN:
			return "#FFDB00";
		case MessageCode.Value.Severity_ERROR:
			return "#FF0000";
		default:
			return "#0000FF";
		}
	}
	
	private String severityToEmoji(String severity){
		switch (severity) {
		case MessageCode.Value.Severity_ERROR:
			return ":stop_sign:";
		case MessageCode.Value.Severity_WARN:
			return ":warning:";
		default:
			return "";
		}
	}
	
}
