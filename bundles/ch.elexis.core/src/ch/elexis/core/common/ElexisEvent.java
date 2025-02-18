package ch.elexis.core.common;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ElexisEvent {

	private String topic;
	private Map<String, String> properties;

	public ElexisEvent() {
		properties = new HashMap<>();
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

	public void putProperty(String key, String value) {
		properties.put(key, value);
	}

	/**
	 * Create an ElexisEvent
	 *
	 * @param topic       the topic of this event
	 * @param objectId    the referenced object id
	 * @param objectClass the referenced object class, in store-to-string format
	 * @return
	 * @see ElexisEventTopics
	 */
	@Transient
	public static ElexisEvent of(String topic, String objectId, String objectClass) {
		ElexisEvent ee = new ElexisEvent();
		ee.setTopic(topic);
		ee.getProperties().put(ElexisEventTopics.PROPKEY_ID, objectId);
		ee.getProperties().put(ElexisEventTopics.PROPKEY_CLASS, objectClass);
		return ee;
	}

}
