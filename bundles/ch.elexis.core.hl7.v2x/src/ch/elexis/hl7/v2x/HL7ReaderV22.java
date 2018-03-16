package ch.elexis.hl7.v2x;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v22.datatype.AD;
import ca.uhn.hl7v2.model.v22.datatype.CE;
import ca.uhn.hl7v2.model.v22.datatype.CN;
import ca.uhn.hl7v2.model.v22.datatype.FT;
import ca.uhn.hl7v2.model.v22.datatype.NM;
import ca.uhn.hl7v2.model.v22.datatype.ST;
import ca.uhn.hl7v2.model.v22.datatype.TX;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v22.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v22.message.ORU_R01;
import ca.uhn.hl7v2.model.v22.segment.MSH;
import ca.uhn.hl7v2.model.v22.segment.NTE;
import ca.uhn.hl7v2.model.v22.segment.OBR;
import ca.uhn.hl7v2.model.v22.segment.OBX;
import ca.uhn.hl7v2.model.v22.segment.ORC;
import ca.uhn.hl7v2.model.v22.segment.PID;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.elexis.hl7.HL7PatientResolver;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.OrcMessage;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.v26.HL7Constants;
import ch.elexis.hl7.v26.HL7_ORU_R01;
import ch.rgw.tools.StringTool;

public class HL7ReaderV22 extends HL7Reader {
	static Logger logger = LoggerFactory.getLogger(HL7ReaderV22.class);
	
	public HL7ReaderV22(Message message){
		super(message);
	}
	
	@Override
	public String getSender() throws ElexisException{
		String sender = "";
		try {
			MSH msh = (MSH) message.get("MSH");
			sender = msh.getMsh4_SendingFacility().getValue();
			if (sender == null) {
				sender = "";
			}
		} catch (HL7Exception e) {
			throw new ElexisException(e.getMessage(), e);
		}
		return sender;
	}
	
	@Override
	public ObservationMessage readObservation(HL7PatientResolver patientResolver,
		boolean createIfNotFound) throws ElexisException{
		observation = null;
		ORU_R01 oru = (ORU_R01) message;
		try {
			this.patientResolver = patientResolver;
			setPatient(oru, createIfNotFound);
			
			int obsCount = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
			for (int idx = 0; idx < obsCount; idx++) {
				OBR obr = oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx).getOBR();
				String obrObservationDateTime =
					obr.getObr7_ObservationDateTime().getTs1_TimeOfAnEvent().getValue();
					
				setOrderComment(oru, idx, obrObservationDateTime);
				
				for (int i = 0; i < oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx)
					.getOBSERVATIONReps(); i++) {
					ORU_R01_ORDER_OBSERVATION obs =
						oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx);
					// get notes and comments
					String commentNTE = getComments(obs, i);
					
					// groupe and sequence
					String group = "";
					String sequence = "";
					
					for (int k = 0; k < 2; k++) {
						CE ce = obr.getUniversalServiceID();
						if (ce != null) {
							String code = "";
							if (ce.getCe3_NameOfCodingSystem() != null)
								code = ce.getCe3_NameOfCodingSystem().getValue();
								
							group = getGroup(code, ce);
							sequence = getSequence(code, ce);
							
						}
					}
					
					// result
					readOBXResults(obs.getOBSERVATION(i), commentNTE, group, sequence,
						obrObservationDateTime);
				}
			}
		} catch (HL7Exception | ParseException e) {
			throw new ElexisException(e.getMessage(), e);
		}
		return observation;
	}
	
	private String getGroup(String code, CE ce){
		if (HL7_ORU_R01.CODINGSYSTEM_DORNER_GROUP_CODE.equalsIgnoreCase(code)) {
			if (ce.getCe2_Text() != null) {
				return ce.getCe2_Text().getValue();
			}
		}
		return "";
	}
	
	private String getSequence(String code, CE ce){
		if (HL7_ORU_R01.CODINGSYSTEM_DORNER_GROUP_POSITION.equalsIgnoreCase(code)) {
			if (ce.getCe1_Identifier() != null) {
				return ce.getCe1_Identifier().getValue();
			}
		}
		return "";
	}
	
	private void setPatient(ORU_R01 oru, final boolean createIfNotFound)
		throws ParseException, HL7Exception{
		List<? extends IPatient> list = new ArrayList<IPatient>();
		String lastName = ""; //$NON-NLS-1$
		String firstName = ""; //$NON-NLS-1$
		String birthDate = ""; //$NON-NLS-1$
		String sex = Gender.FEMALE.value();
		pat = null;
		
		if (pat == null) {
			String sendingApplication = oru.getMSH().getMsh3_SendingApplication().getValue();
			String sendingFacility = oru.getMSH().getMsh4_SendingFacility().getValue();
			String dateTimeOfMessage =
				oru.getMSH().getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().getValue();
				
			PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
			
			String patid = pid.getPatientIDInternalID(0).getCm_pat_id1_IDNumber().getValue();
			String patid_alternative = pid.getAlternatePatientID().getValue();
			if (StringTool.isNothing(patid)) {
				patid = pid.getPatientIDExternalID().getCk1_IDNumber().getValue();
				if (StringTool.isNothing(patid)) {
					patid = patid_alternative;
					if (patid == null) {
						patid = "";
					}
				}
			}
			
			if (patid != null) {
				list = patientResolver.getPatientById(patid);
			}
			
			// String[] pidflds = patid.split("[\\^ ]+"); //$NON-NLS-1$
			// String pid = "";
			// if (pidflds.length > 0)
			// pid = pidflds[pidflds.length - 1];
			
			// place order number
			String orderNumber = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC()
				.getOrc2_PlacerOrderNumber().getCm_placer1_UniquePlacerId().getValue();
				
			if (pid.getPatientName().getPn1_FamilyName().getValue() != null)
				lastName = pid.getPatientName().getPn1_FamilyName().getValue();
			if (pid.getPatientName().getGivenName().getValue() != null)
				firstName = pid.getPatientName().getGivenName().getValue();
			String patientName = firstName + " " + lastName;
			
			observation = new ObservationMessage(sendingApplication, sendingFacility,
				dateTimeOfMessage, patid, patientName, patid_alternative, orderNumber);
				
			birthDate = pid.getDateOfBirth().getTs1_TimeOfAnEvent().getValue();
			sex = pid.getSex().getValue();
			
			if ((patid == null) || (list.size() != 1)) {
				// We did not find the patient using the PatID, so we try the
				// name and birthdate
				list =
					patientResolver.findPatientByNameAndBirthdate(lastName, firstName, birthDate);
					
				if ((list != null) && (list.size() == 1)) {
					pat = list.get(0);
				} else {
					if (createIfNotFound) {
						String phone = StringConstants.EMPTY;
						AD adr = pid.getPatientAddress(0);
						phone = pid.getPhoneNumberHome(0).getValue();
						
						pat = patientResolver.createPatient(lastName, firstName, birthDate, sex);
						pat.setPatientNr(patid);
						
						if (adr != null) {
							if (adr.getStreetAddress().getValue() != null) {
								pat.setStreet(adr.getStreetAddress().getValue());
							}
							if (adr.getZipOrPostalCode().getValue() != null) {
								pat.setZip(adr.getZipOrPostalCode().getValue());
							}
							if (adr.getCity().getValue() != null) {
								pat.setCity(adr.getCity().getValue());
							}
							if (adr.getCountry().getValue() != null) {
								Country cc =
									Country.valueOf(adr.getAd6_Country().getValue());
								pat.setCountry(cc);
							}
						}
						
						pat.setPhone1(phone);
					} else {
						resolvePatient(firstName, lastName, birthDate);
					}
				}
			} else {
				// if the patient with the given ID was found, we verify, if it
				// is the correct name
				pat = list.get(0);
				if (lastName.length() != 0 && firstName.length() != 0) {
					checkConflict(firstName, lastName, birthDate, sex);
				}
			}
		}
	}
	
	private void setOrderComment(ORU_R01 oru, int idx, String obsDate) throws ParseException{
		String orderCommentNTE = getComments(oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx), -1);
		if (orderCommentNTE != null) {
			observation.add(new TextData(HL7Constants.COMMENT_NAME, orderCommentNTE, obsDate,
				HL7Constants.COMMENT_GROUP, null));
		}
	}
	
	private String getComments(ORU_R01_ORDER_OBSERVATION oobs, int i){
		String commentNTE = null;
		int size = oobs.getNTEReps();
		if (i > -1) {
			size = oobs.getOBSERVATION(i).getNTEReps();
		}
		
		for (int n = 0; n < size; n++) {
			NTE nte = oobs.getNTE(n);
			if (i > -1) {
				nte = oobs.getOBSERVATION(i).getNTE(n);
			}
			AbstractPrimitive comment = nte.getNte3_Comment(0);
			if (comment != null) {
				if (commentNTE != null) {
					commentNTE += "\n";
				} else {
					commentNTE = "";
				}
				if(comment.getValue() != null) {
					commentNTE += comment.getValue();			
				}
			}
		}
		return commentNTE;
	}
	
	private void readOBXResults(ORU_R01_OBSERVATION obs, String commentNTE, String group,
		String sequence, String defaultDateTime) throws ParseException{
		OBX obx = obs.getOBX();
		String valueType = obx.getObx2_ValueType().getValue();
		String name = "";
		String itemCode = "";
		String unit = "";
		String range = "";
		String observationTime = "";
		String status = "";
		Boolean flag;
		String rawAbnormalFlags;
		
		if (isTextOrNumeric(valueType)) {
			name = obx.getObx3_ObservationIdentifier().getCe2_Text().getValue();
			if (name == null) {
				name = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
			}
			String value = "";
			Object tmp = obx.getObx5_ObservationValue().getData();
			if (tmp instanceof ST) {
				value = ((ST) tmp).getValue();
			} else if (tmp instanceof TX) {
				value = ((TX) tmp).getValue();
				if (value.contains("\\.br")) {
					value = parseTextValue(value);
				}
			} else if (tmp instanceof FT) {
				value = parseFormattedTextValue(((FT) tmp).getValue());
			} else if (tmp instanceof NM) {
				value = ((NM) tmp).getValue();
			} else if (tmp instanceof CE) {
				value = ((CE) tmp).getCe2_Text().getValue();
			}
			
			itemCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
			unit = obx.getObx6_Units().getCe1_Identifier().getValue();
			range = obx.getObx7_ReferencesRange().getValue();
			rawAbnormalFlags = obx.getObx8_AbnormalFlags(0).getValue();
			flag = isPathologic(rawAbnormalFlags);
			observationTime =
				obx.getObx14_DateTimeOfTheObservation().getTs1_TimeOfAnEvent().getValue();
			status = obx.getObx11_ObservationResultStatus().getValue();
			
			LabResultData lrd = new LabResultData(itemCode, name, unit, value, range, flag, rawAbnormalFlags,
				defaultDateTime, observationTime, commentNTE, group, sequence, status,
				extractName(obx.getObx4_ObservationSubID()));
				
			if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_NM)) {
				lrd.setIsNumeric(true);
			} else if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_TX)) {
				lrd.setIsPlainText(true);
			} else if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_FT)) {
				lrd.setIsFormatedText(true);
			}
			
			observation.add(lrd);
		} else {
			logger.error(MessageFormat.format("Value type {0} is not implemented!", valueType));
		}
	}
	
	@Override
	public OrcMessage getOrcMessage(){
		try {
			ORU_R01 oru = (ORU_R01) message;
			if (oru != null) {
				ORU_R01_PATIENT_RESULT pr = oru.getPATIENT_RESULT();
				if (pr != null) {
					ORU_R01_ORDER_OBSERVATION oo = pr.getORDER_OBSERVATION();
					if (oo != null) {
						return extractOrc(oo.getORC());
					}
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(HL7Reader.class).warn("orc parsing failed", e);
		}
		return null;
	}
	
	private OrcMessage extractOrc(ORC orc) throws HL7Exception{
		if (orc != null) {
			OrcMessage orcMessage = new OrcMessage();
			CN ops = orc.getOrderingProvider();
			addNameValuesToOrcMessage(ops.getFamilyName(), ops.getGivenName(), orcMessage);
			return orcMessage;
		}
		return null;
	}
	
}
