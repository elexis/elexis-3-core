package ch.elexis.hl7.model;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import ch.elexis.hl7.util.HL7Helper;

public class ObservationMessage {
	
	// MSH
	String sendingApplication; // MSH-3
	String sendingFacility; // MSH-4
	Date dateTimeOfMessage; // MSH-4
	
	// PID
	String patientId; // PID-3
	String alternatePatientId; // PID-4
	String patientName; // PID-5
	
	// ORC
	String orderNumber; // ORC-2
	
	// OBX
	List<IValueType> observations = new Vector<IValueType>();
	
	public ObservationMessage(String _sendingApplication, String _sendingFacility,
		String _dateTimeOfMessage, String _patientId, String _patientName,
		String _alternatePatientId, String _orderNumber) throws ParseException{
		super();
		this.sendingApplication = _sendingApplication;
		this.sendingFacility = _sendingFacility;
		this.patientId = _patientId;
		this.patientName = _patientName;
		this.alternatePatientId = _alternatePatientId;
		this.orderNumber = _orderNumber;
		this.dateTimeOfMessage = HL7Helper.stringToDate(_dateTimeOfMessage);
	}
	
	public void add(IValueType type){
		this.observations.add(type);
	}
	
	public String getSendingApplication(){
		return sendingApplication;
	}
	
	public String getSendingFacility(){
		return sendingFacility;
	}
	
	public Date getDateTimeOfMessage(){
		return dateTimeOfMessage;
	}
	
	public String getPatientId(){
		if (this.patientId == null || this.patientId.trim().length() == 0) {
			return this.alternatePatientId;
		}
		return patientId;
	}
	
	public String getPatientName(){
		return patientName;
	}
	
	public String getOrderNumber(){
		return orderNumber;
	}
	
	public List<IValueType> getObservations(){
		return observations;
	}
}
