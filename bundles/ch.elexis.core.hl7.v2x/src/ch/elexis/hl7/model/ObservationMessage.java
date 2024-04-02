package ch.elexis.hl7.model;

import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import ch.elexis.hl7.util.HL7Helper;

public class ObservationMessage {

	// MSH
	String sendingApplication; // MSH-3
	String sendingFacility; // MSH-4
	Date dateTimeOfMessage = null; // MSH-7
	String messageControlID = StringUtils.EMPTY; // MSH-10

	// PID
	String patientId; // PID-3
	String alternatePatientId; // PID-4
	String patientName; // PID-5
	String patientLastName = StringUtils.EMPTY; // PID-5-2
	String patientFirstName = StringUtils.EMPTY; // PID-5-3
	String patientBirthdate = StringUtils.EMPTY; // PID-7
	String patientSex = StringUtils.EMPTY; // PID-8
	// optional NTE following PID
	// see
	// http://hl7-definition.caristix.com:9010/Default.aspx?version=HL7+v2.5&segment=OBX
	String patientNotesAndComments = StringUtils.EMPTY;

	// ORC
	String orderNumber; // ORC-2
	String orderNumberPlacer = StringUtils.EMPTY; // ORC-2
	String orderNumberFiller = StringUtils.EMPTY; // ORC-3
	Date dateTimeOfTransaction = null; // ORC-9

	// OBX
	List<IValueType> observations = new Vector<>();

	public ObservationMessage(String _sendingApplication, String _sendingFacility, String _dateTimeOfMessage,
			String _patientId, String _patientName, String _patientNotesAndComments, String _alternatePatientId,
			String _orderNumber) throws ParseException {
		super();
		this.sendingApplication = _sendingApplication;
		this.sendingFacility = _sendingFacility;
		this.patientId = _patientId;
		this.patientName = _patientName;
		this.patientNotesAndComments = _patientNotesAndComments;
		this.alternatePatientId = _alternatePatientId;
		this.orderNumber = _orderNumber;
		this.dateTimeOfMessage = HL7Helper.stringToDate(_dateTimeOfMessage);
	}

	public ObservationMessage(String _sendingApplication, String _sendingFacility, String _dateTimeOfMessage,
			String _messageControlID, String _dateTimeOfTransaction, String _patientId, String _patientLastName,
			String _patientFirstName, String _patientNotesAndComments, String _patientBirthDate, String _patientSex,
			String _alternatePatientId, String _orderNumberPlacer, String _orderNumberFiller) throws ParseException {
		super();
		this.sendingApplication = _sendingApplication;
		this.sendingFacility = _sendingFacility;
		this.dateTimeOfMessage = HL7Helper.stringToDate(_dateTimeOfMessage);
		this.messageControlID = _messageControlID;
		this.patientId = _patientId;
		this.patientLastName = _patientLastName;
		this.patientFirstName = _patientFirstName;
		this.patientNotesAndComments = _patientNotesAndComments;
		this.patientName = _patientLastName + StringUtils.SPACE + _patientFirstName;
		this.patientBirthdate = _patientBirthDate;
		this.patientSex = _patientSex;
		this.alternatePatientId = _alternatePatientId;
		this.orderNumberPlacer = _orderNumberPlacer;
		this.orderNumberFiller = _orderNumberFiller;
		this.dateTimeOfTransaction = HL7Helper.stringToDate(_dateTimeOfTransaction);
	}

	public void add(IValueType type) {
		this.observations.add(type);
	}

	public String getSendingApplication() {
		return sendingApplication;
	}

	public String getSendingFacility() {
		return sendingFacility;
	}

	public Date getDateTimeOfMessage() {
		return dateTimeOfMessage;
	}

	public String getMessageControlID() {
		return messageControlID;
	}

	public String getPatientId() {
		if (this.patientId == null || this.patientId.trim().length() == 0) {
			return this.alternatePatientId;
		}
		return patientId;
	}

	public String getAlternatePatientId() {
		return alternatePatientId;
	}

	public String getPatientName() {
		return patientName;
	}

	public String getPatientLastName() {
		return patientLastName;
	}

	public String getPatientFirstName() {
		return patientFirstName;
	}

	public String getPatientBirthdate() {
		return patientBirthdate;
	}

	public String getPatientSex() {
		return patientSex;
	}

	public String getPatientNotesAndComments() {
		return patientNotesAndComments;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public String getOrderNumberPlacer() {
		return orderNumberPlacer;
	}

	public String getOrderNumberFiller() {
		return orderNumberFiller;
	}

	public Date getDateTimeOfTransaction() {
		return dateTimeOfTransaction;
	}

	public List<IValueType> getObservations() {
		return observations;
	}
}
