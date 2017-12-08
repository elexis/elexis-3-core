package ch.elexis.hl7.model;

import java.text.ParseException;

public class StringData extends AbstractData {
	private String unit;
	private String value;
	private String range;
	
	public StringData(String name, String unit, String value, String range, String dateStr,
		String comment, String group, String sequence) throws ParseException{
		super(name, dateStr, comment, group, sequence);
		this.unit = unit;
		this.value = value;
		this.range = range;
	}
	
	public String getUnit(){
		return unit;
	}
	
	public String getValue(){
		return value;
	}
	
	public String getRange(){
		return range;
	}
}
