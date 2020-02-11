package ch.elexis.core.logback.rocketchat.internal;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONObject;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

public class IntegrationPostHandler {
	
	private final boolean attachment;
	private final ILoggingEvent eventObject;
	private final String identification;
	
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");
	
	public IntegrationPostHandler(ILoggingEvent eventObject, String identification,
		boolean attachment){
		this.eventObject = eventObject;
		this.identification = identification;
		this.attachment = attachment;
	}
	
	public IStatus post(URL url) throws MalformedURLException, IOException{
		Level logLevel = eventObject.getLevel();
		if (logLevel == null) {
			logLevel = Level.INFO;
		}
		
		JSONObject json = new JSONObject();
		
		if (identification != null) {
			json.put("username", identification);
		}
		
		ZonedDateTime eventTimeStamp =
			Instant.ofEpochMilli(eventObject.getTimeStamp()).atZone(ZoneId.of("GMT+1"));
		StringBuilder sbHeader = new StringBuilder();
		sbHeader
			.append(levelToEmoji(logLevel) + " " + eventTimeStamp.toLocalDateTime().format(dtf));
		sbHeader.append(" _" + eventObject.getLoggerName() + "_\n");
		String sbHeaderString = sbHeader.toString();
		
		String exception = parseException(eventObject);
		
		if (attachment) {
			json.put("text", sbHeaderString);
			
			Map<String, Object> params = new HashMap<>();
			params.put("color", levelToColor(logLevel));
			params.put("text", eventObject.getFormattedMessage());
			if (exception != null) {
				params.put("text", eventObject.getFormattedMessage() + exception);
			}
			
			json.put("attachments", Collections.singletonList(params));
		} else {
			StringBuilder sbBody = new StringBuilder();
			sbBody.append(levelToEmoji(logLevel));
			sbBody.append(eventObject.getFormattedMessage());
			if (exception != null) {
				sbBody.append("\n" + exception);
			}
			json.put("text", sbHeader + sbBody.toString());
		}
		
		return send(json.toString().getBytes(), url);
	}
	
	private String parseException(ILoggingEvent eventObject2){
		if (eventObject.getThrowableProxy() != null) {
			StringBuilder sbException = new StringBuilder();
			IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
			
			sbException.append("```\n");
			StackTraceElementProxy[] st = throwableProxy.getStackTraceElementProxyArray();
			for (int j = 0; j < st.length; j++) {
				StackTraceElementProxy stackTraceElementProxy = st[j];
				StackTraceElement stackTraceElement = stackTraceElementProxy.getStackTraceElement();
				sbException.append(stackTraceElement + "\n");
				if (j == 5) {
					break;
				}
			}
			sbException.append("```");
			return sbException.toString();
		}
		return null;
	}
	
	private Object levelToColor(Level logLevel){
		switch (logLevel.levelInt) {
		case Level.ERROR_INT:
			return "#FF0000";
		case Level.WARN_INT:
			return "#FFDB00";
		case Level.INFO_INT:
			return "#0000FF";
		default:
			return "#00FF00";
		}
	}
	
	private String levelToEmoji(Level logLevel){
		switch (logLevel.levelInt) {
		case Level.ERROR_INT:
			return ":stop_sign:";
		case Level.WARN_INT:
			return ":warning:";
		default:
			return "";
		}
	}
	
	private IStatus send(byte[] postDataBytes, URL url) throws MalformedURLException, IOException{
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
		return new Status(Status.ERROR, "ch.elexis.core.logback.rocketchat", "Error sending, with response code: " + responseCode);
	}
	
}
