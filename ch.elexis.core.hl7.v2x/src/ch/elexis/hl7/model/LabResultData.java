package ch.elexis.hl7.model;

import java.text.ParseException;
import java.util.Date;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.hl7.util.HL7Helper;

public class LabResultData extends AbstractData {
	private String code;
	private String unit;
	private String value;
	private String range;
	private Date obrDateTime;
	private boolean flag;
	private boolean isNumeric = false;
	private boolean isFormatedText = false;
	private boolean isPlainText = false;
	
	public LabResultData(String code, String name, String unit, String value, String range,
		boolean flag, String obrDateTime, String dateStr, String comment, String group,
		String sequence) throws ParseException{
		super(name, dateStr, comment, group, sequence);
		
		this.setCode(code);
		this.setUnit(unit);
		this.setValue(value);
		this.setRange(range);
		this.setFlagged(flag);
		if (obrDateTime != null && obrDateTime.length() > 0) {
			this.obrDateTime = HL7Helper.stringToDate(obrDateTime);
		}
	}
	
	public String getCode(){
		return code;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getUnit(){
		return unit;
	}
	
	public void setUnit(String unit){
		this.unit = unit;
	}
	
	public String getValue(){
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getRange(){
		return range;
	}
	
	public void setRange(String range){
		this.range = range;
	}
	
	public Date getOBRDateTime(){
		return obrDateTime;
	}
	
	public void setOBRDateTime(String obrDateTime) throws ElexisException{
		try {
			if (obrDateTime != null && obrDateTime.length() > 0) {
				this.obrDateTime = HL7Helper.stringToDate(obrDateTime);
			}
		} catch (ParseException e) {
			throw new ElexisException(e.getMessage(), e);
		}
	}
	
	public boolean isFlagged(){
		return flag;
	}
	
	public void setFlagged(boolean flag){
		this.flag = flag;
	}
	
	public boolean isNumeric(){
		return isNumeric;
	}
	
	public void setIsNumeric(boolean isNumeric){
		this.isNumeric = isNumeric;
	}
	
	public boolean isFormatedText(){
		return isFormatedText;
	}
	
	public void setIsFormatedText(boolean isFormatedText){
		this.isFormatedText = isFormatedText;
	}
	
	public boolean isPlainText(){
		return isPlainText;
	}
	
	public void setIsPlainText(boolean isPlainText){
		this.isPlainText = isPlainText;
	}
	
}