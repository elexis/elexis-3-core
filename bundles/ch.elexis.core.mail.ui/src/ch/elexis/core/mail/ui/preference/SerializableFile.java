package ch.elexis.core.mail.ui.preference;

import java.io.Serializable;

public class SerializableFile implements Serializable {

	private static final long serialVersionUID = 1205627579938988742L;
	public String name;
	public String mimeType;
	public byte[] data;

	public SerializableFile(String name, String mimeType, byte[] data) {
		super();
		this.name = name;
		this.mimeType = mimeType;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
