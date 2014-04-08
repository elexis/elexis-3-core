package ch.elexis.hl7.model;

import java.text.ParseException;
import java.util.Date;

import ch.elexis.hl7.util.HL7Helper;

public abstract class AbstractData implements IValueType {
	private String name;
	private Date date;
	private String comment;
	private String group;
	private String sequence;
	
	public AbstractData(String name, String dateStr, String comment, String group, String sequence)
		throws ParseException{
		super();
		this.name = name;
		this.comment = comment;
		if (dateStr != null && dateStr.length() > 0) {
			this.date = HL7Helper.stringToDate(dateStr);
		}
		this.group = group;
		this.sequence = sequence;
	}
	
	public String getName(){
		return name;
	}
	
	public Date getDate(){
		return date;
	}
	
	public void setDate(Date date){
		this.date = date;
	}
	
	public String getComment(){
		return comment;
	}
	
	public String getGroup(){
		return group;
	}
	
	public String getSequence(){
		return sequence;
	}
}
