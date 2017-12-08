package ch.elexis.hl7.model;

import java.text.ParseException;

import org.apache.commons.codec.binary.Base64;

public class EncapsulatedData extends AbstractData {
	byte[] data;
	
	public EncapsulatedData(String name, String encoding, String text, String dateStr,
		String comment, String group, String sequence) throws ParseException{
		super(name, dateStr, comment, group, sequence);
		if (encoding != null && "base64".equals(encoding.trim().toLowerCase())) { //$NON-NLS-1$
			data = Base64.decodeBase64(text.getBytes());
		} else {
			data = text.getBytes();
		}
	}
	
	public byte[] getData(){
		return data;
	}
}
