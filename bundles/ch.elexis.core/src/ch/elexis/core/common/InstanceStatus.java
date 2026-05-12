package ch.elexis.core.common;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InstanceStatus {

	public enum STATE {
		UNDEF, STARTING_UP, ACTIVE, SHUTTING_DOWN
	};

	private String uuid;
	private String activeUser;
	private String identifier;
	private String version;
	private STATE state = STATE.UNDEF;
	private String operatingSystem;
	// server written
	private Date firstSeen;
	private Date lastUpdate;
	private String remoteAddress;

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

	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public Date getFirstSeen() {
		return firstSeen;
	}

	public void setFirstSeen(Date firstSeen) {
		this.firstSeen = firstSeen;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	@Override
	public String toString() {
		String identifier = getIdentifier() != null ? " @ " + getIdentifier() : StringUtils.EMPTY;
		String ret = "[" + getUuid() + "] " + getActiveUser() + identifier + " (Version " + getVersion() + " @ "
				+ getOperatingSystem() + ") ";
		if (getState() != STATE.ACTIVE) {
			ret += getState();
		}
		return ret;
	}

}
