package ch.elexis.core.model.agenda;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.DatatypeConverter;

public class Area {

	private String id;
	private final String name;
	private final AreaType type;
	private final String contactId;

	/**
	 *
	 * @param name      the name of the area
	 * @param type
	 * @param contactId <code>null</code> if {@link AreaType#GENERIC}, else the
	 *                  contact id
	 */
	public Area(String name, AreaType type, String contactId) {
		this.name = name;
		this.type = type;
		this.contactId = contactId;
	}

	public String getName() {
		return name;
	}

	public AreaType getType() {
		return type;
	}

	public String getContactId() {
		return contactId;
	}

	public String getId() {
		if (id == null) {
			try {
				id = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(name.getBytes("UTF-8")));
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				throw new IllegalStateException(StringUtils.EMPTY, e);
			}
		}
		return id;
	}

}
