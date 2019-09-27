package ch.elexis.core.logback.rocketchat;

import java.io.IOException;
import java.net.URL;

import ch.elexis.core.logback.rocketchat.internal.IntegrationPostHandler;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class RocketchatAppender extends AppenderBase<ILoggingEvent> {

	@Override
	public void start() {
		if (getContext().getProperty("identification") == null) {
			getContext().putProperty("identification", "logback");
			addInfo("No <identification> parameter defined, defaulting to logback.");
		}
		super.start();
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		try {
			String integrationUrl = getContext().getProperty("integrationUrl");
			if (integrationUrl == null || integrationUrl.length() == 0) {
				return;
			}
			
			URL realIntegrationUrl = new URL(integrationUrl);
			String identification = getContext().getProperty("identification");
			boolean attachmentBased = (getContext().getProperty("attachmentBased") == null) ? true
					: Boolean.valueOf(getContext().getProperty("attachmentBased"));
			new IntegrationPostHandler(eventObject, identification, attachmentBased).post(realIntegrationUrl);
		} catch (IOException ex) {
			addError("Error posting to integrationUrl [" + getContext().getProperty("integrationUrl") + "]", ex);
		}
	}

	public String getEvent() {
		return getContext().getProperty("event");
	}

	public void setEvent(String event) {
		getContext().putProperty("event", event);
	}

	public String getIntegrationUrl() {
		return getContext().getProperty("integrationUrl");
	}

	public void setIntegrationUrl(String integrationUrl) {
		addInfo("Changing integration url ...");
		getContext().putProperty("integrationUrl", integrationUrl);
	}

	public String getIdentification() {
		return getContext().getProperty("identification");
	}

	public void setIdentification(String identification) {
		getContext().putProperty("identification", identification);
	}

	public String getAttachmentBased() {
		return getContext().getProperty("attachmentBased");
	}

	public void setAttachmentBased(String attachmentBased) {
		getContext().putProperty("attachmentBased", attachmentBased);
	}
}
