package ch.elexis.hl7.model;

import java.text.ParseException;

import ch.rgw.tools.StringTool;
public class TextData extends AbstractData {
	private String text;
	
	public TextData(String name, String text, String dateStr, String group, String sequence)
		throws ParseException{
		super(name, dateStr, StringTool.leer, group, sequence); //$NON-NLS-1$
		this.text = text;
	}
	
	public String getText(){
		return text;
	}
}
