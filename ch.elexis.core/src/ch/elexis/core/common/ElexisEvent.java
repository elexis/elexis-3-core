package ch.elexis.core.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ElexisEvent {

	public static final String EVENT_BASE = "info/elexis/";
	
	private String topic;
	private Map<String, String> properties;

	public ElexisEvent() {
		properties = new HashMap<String, String>();
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
