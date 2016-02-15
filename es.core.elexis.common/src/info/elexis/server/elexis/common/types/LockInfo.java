package info.elexis.server.elexis.common.types;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ch.elexis.core.constants.StringConstants;

@XmlRootElement
public class LockInfo {

	@XmlElement
	private String elementId;
	@XmlElement
	private String elementType;
	@XmlElement
	private String user;
	@XmlElement
	private Date creationDate;

	public LockInfo() {
	}

	public LockInfo(String storeToString, String userId) {
		String[] split = storeToString.split(StringConstants.DOUBLECOLON);
		if (split.length == 2) {
			this.elementId = split[1];
			this.elementType = split[0];
			this.user = userId;
			this.creationDate = new Date();
		} else {
			throw new IllegalArgumentException(storeToString);
		}
	}

	public String getElementType() {
		return elementType;
	}

	public String getElementId() {
		return elementId;
	}

	public String getUser() {
		return user;
	}

	public Date getCreationDate() {
		return creationDate;
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
		return elementType+StringConstants.DOUBLECOLON+elementId;
	}
}
