package ch.elexis.core.lock.types;

import org.apache.commons.lang3.StringUtils;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import ch.elexis.core.constants.StringConstants;

@XmlRootElement
public class LockInfo {

	@XmlTransient
	public static final long EVICTION_TIMEOUT = 30000;
	@XmlTransient
	private long refreshMillis;

	@XmlElement
	private String elementId;
	@XmlElement
	private String elementType;
	@XmlElement
	private String user;
	@XmlElement
	private Date creationDate;
	@XmlElement
	private String systemUuid;
	@XmlElement
	private String stationId;
	@XmlElement
	private String stationLabel;

	public LockInfo() {
	}

	public LockInfo(String storeToString, String userId, String systemUuid) {
		this(storeToString, userId, systemUuid, StringUtils.EMPTY, StringUtils.EMPTY);
	}

	public LockInfo(String storeToString, String userId, String systemUuid, String stationId, String stationLabel) {
		String[] split = storeToString.split(StringConstants.DOUBLECOLON);
		if (split.length == 2) {
			this.elementId = split[1];
			this.elementType = split[0];
			this.user = userId;
			this.creationDate = new Date();
			this.systemUuid = systemUuid;
			this.stationId = stationId;
			this.stationLabel = stationLabel;
		} else {
			throw new IllegalArgumentException(storeToString);
		}
	}

	/**
	 * @return class name of the element, equals storeToString without the id.
	 */
	public String getElementType() {
		return elementType;
	}

	/**
	 * @return local id of the element, equals storeToString without the class name.
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 *
	 * @return user the lock is allocated to
	 */
	public String getUser() {
		return user;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getSystemUuid() {
		return systemUuid;
	}

	public String getStationId() {
		return stationId;
	}

	public String getStationLabel() {
		return stationLabel;
	}

	public static String getElementId(String storeToString) {
		String[] split = storeToString.split(StringConstants.DOUBLECOLON);
		if (split.length == 2) {
			return split[1];
		} else {
			throw new IllegalArgumentException(storeToString);
		}
	}

	public String getElementStoreToString() {
		return elementType + StringConstants.DOUBLECOLON + elementId;
	}

	public void refresh() {
		refreshMillis = System.currentTimeMillis();
	}

	public boolean evict(long currentMillis) {
		if (refreshMillis == 0) {
			refreshMillis = currentMillis;
		} else if ((currentMillis - refreshMillis) > EVICTION_TIMEOUT) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "LockInfo [elementType=" + elementType + ", elementId=" + elementId + ", user=" + user + ", systemUuid="
				+ systemUuid + ", creationDate=" + creationDate + ", refreshMillis=" + refreshMillis + "]";
	}

}
