package ch.elexis.hl7;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Primitive;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IPatient;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.OrcMessage;
import ch.elexis.hl7.v26.HL7Constants;
import ch.elexis.hl7.v26.Messages;
import ch.rgw.tools.StringTool;

public abstract class HL7Reader {
	static Logger logger = LoggerFactory.getLogger(HL7Reader.class);
	
	protected Message message;
	protected ObservationMessage observation;
	protected IPatient pat;
	protected HL7PatientResolver patientResolver;
	
	public HL7Reader(Message message){
		this.message = message;
	}
	
	public abstract OrcMessage getOrcMessage();
	
	public abstract String getSender() throws ElexisException;
	
	public abstract ObservationMessage readObservation(HL7PatientResolver patientResolver,
		boolean createIfNotFound) throws ElexisException;
	
	protected boolean isTextOrNumeric(String valueType){
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_ST))
			return true;
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_TX))
			return true;
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_FT))
			return true;
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_NM))
			return true;
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_SN))
			return true;
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_CE))
			return true;
		
		return false;
	}
	
	private String[] abnormalFlagStartCharacters = {
		"-", "+", "<", ">", "L", "H", "A"
	};
	
	protected void resolvePatient(String firstName, String lastName, String birthDate){
		pat = patientResolver.resolvePatient(firstName, lastName, birthDate);
		if (pat == null) {
			logger.warn(Messages.HL7_PatientNotInDatabase);
		}
	}
	
	protected void checkConflict(String firstName, String lastName, String birthDate, String sex){
		if (!patientResolver.matchPatient(pat, firstName, lastName, birthDate)) {
			StringBuilder sb = new StringBuilder();
			sb.append(Messages.HL7_NameConflictWithID).append(":\n")
				.append(Messages.HL7_Lab).append(lastName).append(StringTool.space)
				.append(firstName).append("(").append(sex).append("),").append(birthDate)
				.append("\n").append(Messages.HL7_Database).append(pat.getLabel());
			pat = null;
			logger.warn(sb.toString());
			
			resolvePatient(firstName, lastName, birthDate);
		}
	}
	
	public Boolean isPathologic(String abnormalValue){
		if (!StringTool.isNothing(abnormalValue)) {
			if (abnormalValue.startsWith("N")) {
				return false;
			}
			for (String startChar : abnormalFlagStartCharacters) {
				if (abnormalValue.startsWith(startChar)) {
					return true;
				}
			}
		}
		return null;
	}
	
	public IPatient getPatient(){
		return pat;
	}
	
	public Message getACK() throws HL7Exception, IOException{
		return message.generateACK();
	}
	
	public String getVersion(){
		return message.getVersion();
	}
	
	public String parseTextValue(String value){
		String text = value;
		text = text.replaceAll("\\\\.br\\\\", "\n");
		text = text.replaceAll("\\\\.BR\\\\", "\n");
		
		// only return parsed value if it contains reasonable input
		if (text != null && !text.isEmpty()) {
			return text;
		}
		return value;
	}
	
	/**
	 * Parse an FT value
	 * @param ftValue
	 * @return
	 * @see http://www.healthintersections.com.au/?page_id=441
	 */
	public String parseFormattedTextValue(String ftValue) {
		// currently we use the default, please augment
		// on specific requirements
		return parseTextValue(ftValue);
	}
	
	/**
	 * Extracts and trims the String value from a {@link Primitive}
	 * 
	 * @param nameObj
	 * @return
	 */
	public String extractName(Primitive nameObj){
		if (nameObj != null) {
			String val = nameObj.getValue();
			if (val != null) {
				return val.trim();
			}
		}
		return null;
	}
	
	/**
	 * Collects all potential not null name values and adds them to the OrcMessage. All not null
	 * values like firstName, secondName and combination of firstName and secondName will also be
	 * added to the OrcMessage.
	 * 
	 * Example firstName is Max and secondName is Muster: Method collects: Max, Muster, Max Muster
	 * in OrcMessage
	 * 
	 * @param firstName
	 * @param secondName
	 * @param orcMessage
	 */
	public void addNameValuesToOrcMessage(Primitive firstName, Primitive secondName,
		OrcMessage orcMessage){
		if (orcMessage != null) {
			String name = extractName(firstName);
			if (name != null) {
				orcMessage.getNames().add(name);
			}
			
			String name2 = extractName(secondName);
			if (name2 != null) {
				orcMessage.getNames().add(name2);
				if (name != null) {
					orcMessage.getNames().add(name + " " + name2);
				}
			}
		}
	}
}
