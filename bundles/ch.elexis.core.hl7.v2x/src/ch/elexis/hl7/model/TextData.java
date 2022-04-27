package ch.elexis.hl7.model;

import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;

public class TextData extends AbstractData {
	private String text;

	public TextData(String name, String text, String dateStr, String group, String sequence) throws ParseException {
		super(name, dateStr, StringUtils.EMPTY, group, sequence);
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
