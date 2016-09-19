package ch.elexis.core.common;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InstanceStatus {

	public enum STATE {
		STARTING_UP, RUNNING, SHUTTING_DOWN
	};

	private String uuid;
	private String activeUser;
	private String identifier;
	private String version;
	private STATE state;
	private String operatingSystem;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getActiveUser() {
		return activeUser;
	}

	public void setActiveUser(String activeUser) {
		this.activeUser = activeUser;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public STATE getState() {
		return state;
	}

	@Override
	public String toString() {
		String ret = "[" + getUuid() + "] " + getActiveUser() + " (Version " + getVersion() + " @ "
				+ getOperatingSystem() + ") ";
		if (getState() != STATE.RUNNING) {
			ret += getState();
		}
		return ret;
	}

}
