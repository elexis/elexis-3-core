package ch.elexis.hl7;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.v26.HL7Constants;
import ch.elexis.hl7.v26.Messages;
import ch.rgw.tools.StringTool;

public abstract class HL7Reader {
	static Logger logger = LoggerFactory.getLogger(HL7Reader.class);
	
	protected Message message;
	protected ObservationMessage observation;
	protected Patient pat;
	protected HL7PatientResolver patientResolver;
	
	public HL7Reader(Message message){
		this.message = message;
	}
	
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
		"-", "+", "<", ">", "L", "H"
	};
	
	protected void resolvePatient(String firstName, String lastName, String birthDate){
		pat = patientResolver.resolvePatient(firstName, lastName, birthDate);
		if (pat == null) {
			logger.warn(Messages.getString("HL7_PatientNotInDatabase"));
		}
	}
	
	protected void checkConflict(String firstName, String lastName, String birthDate, String sex){
		if (!patientResolver.matchPatient(pat, firstName, lastName, birthDate)) {
			StringBuilder sb = new StringBuilder();
			sb.append(Messages.getString("HL7_NameConflictWithID")).append(":\n")
				.append(Messages.getString("HL7_Lab")).append(lastName).append(StringTool.space)
				.append(firstName).append("(").append(sex).append("),").append(birthDate)
				.append("\n").append(Messages.getString("HL7_Database")).append(pat.getLabel());
			pat = null;
			logger.warn(sb.toString());
			
			resolvePatient(firstName, lastName, birthDate);
		}
	}
	
	public boolean isPathologic(String abnormalValue){
		if (!StringTool.isNothing(abnormalValue)) {
			for (String startChar : abnormalFlagStartCharacters) {
				if (abnormalValue.startsWith(startChar)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Patient getPatient(){
		return pat;
	}
	
	public Message getACK() throws HL7Exception, IOException{
		return message.generateACK();
	}
	
	public String getVersion(){
		return message.getVersion();
	}
}
