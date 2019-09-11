package ch.elexis.core.logback.rocketchat;

import java.io.IOException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class RocketchatAppender extends AppenderBase<ILoggingEvent> {
	
	private String event;
	
	private String integrationUrl;
	private String identification;
	private String attachmentBased;
	
	@Override
	public void start(){
		if (identification == null) {
			identification = "logback";
			addInfo("No <identification> parameter defined, defaulting to logback.");
		}
		super.start();
	}
	
	@Override
	protected void append(ILoggingEvent eventObject){
		try {
			new IntegrationPostHandler(eventObject, identification,
				Boolean.valueOf(attachmentBased)).post(integrationUrl);
		} catch (IOException ex) {
			addError("Error posting to integrationUrl [" + integrationUrl + "]", ex);
		}
	}
	
	public String getEvent(){
		return event;
	}
	
	public void setEvent(String event){
		this.event = event;
	}
	
	public String getIntegrationUrl(){
		return integrationUrl;
	}
	
	public void setIntegrationUrl(String integrationUrl){
		this.integrationUrl = integrationUrl;
	}
	
	public String getIdentification(){
		return identification;
	}
	
	public void setIdentification(String identification){
		this.identification = identification;
	}
	
	public String getAttachmentBased(){
		return attachmentBased;
	}
	
	public void setAttachmentBased(String attachmentBased){
		this.attachmentBased = attachmentBased;
	}
}
