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
import ca.uhn.hl7v2.model.v23.datatype.CE;
import ca.uhn.hl7v2.model.v23.datatype.ED;
import ca.uhn.hl7v2.model.v23.datatype.FT;
import ca.uhn.hl7v2.model.v23.datatype.NM;
import ca.uhn.hl7v2.model.v23.datatype.SN;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.datatype.TX;
import ca.uhn.hl7v2.model.v23.datatype.XAD;
import ca.uhn.hl7v2.model.v23.datatype.XCN;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_RESPONSE;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.NTE;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.model.v23.segment.ORC;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.elexis.hl7.HL7PatientResolver;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.OrcMessage;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.util.HL7Helper;
import ch.elexis.hl7.v26.HL7Constants;
import ch.elexis.hl7.v26.HL7_ORU_R01;
import ch.elexis.hl7.v26.Messages;
import ch.rgw.tools.StringTool;

public class HL7ReaderV23 extends HL7Reader {
	static Logger logger = LoggerFactory.getLogger(HL7ReaderV23.class);
	
	public HL7ReaderV23(Message message){
		super(message);
	}
	
	@Override
	public String getSender() throws ElexisException{
		String sender;
		try {
			MSH msh = (MSH) message.get("MSH");
			sender = msh.getMsh4_SendingFacility().getNamespaceID().getValue();
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
			
			int obsCount = oru.getRESPONSE().getORDER_OBSERVATIONReps();
			for (int idx = 0; idx < obsCount; idx++) {
				OBR obr = oru.getRESPONSE().getORDER_OBSERVATION(idx).getOBR();
				String obrObservationDateTime =
					obr.getObr7_ObservationDateTime().getTs1_TimeOfAnEvent().getValue();
				if (obrObservationDateTime == null || obrObservationDateTime.length() < 8) {
					obrObservationDateTime =
						HL7Helper.dateToString(observation.getDateTimeOfMessage());
				}
				
				setOrderComment(oru, idx, obrObservationDateTime);
				
				for (int i = 0; i < oru.getRESPONSE().getORDER_OBSERVATION(idx)
					.getOBSERVATIONReps(); i++) {
					ORU_R01_ORDER_OBSERVATION obs = oru.getRESPONSE().getORDER_OBSERVATION(idx);
					// get notes and comments
					String commentNTE = getComments(obs, i);
					
					// groupe and sequence
					String group = "";
					String sequence = "";
					for (int k = 0; k < 2; k++) {
						CE ce = obr.getObr4_UniversalServiceIdentifier();
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
			String sendingApplication =
				oru.getMSH().getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
			String sendingFacility =
				oru.getMSH().getMsh4_SendingFacility().getHd1_NamespaceID().getValue();
			String dateTimeOfMessage =
				oru.getMSH().getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().getValue();
				
			PID pid = oru.getRESPONSE().getPATIENT().getPID();
			
			String patid = pid.getPid3_PatientIDInternalID(0).getCx1_ID().getValue();
			String patid_alternative = pid.getPid4_AlternatePatientID().getCx1_ID().getValue();
			if (StringTool.isNothing(patid)) {
				patid = pid.getPid2_PatientIDExternalID().getCx1_ID().getValue();
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
			String orderNumber = oru.getRESPONSE().getORDER_OBSERVATION().getORC()
				.getOrc2_PlacerOrderNumber(0).getEi1_EntityIdentifier().getValue();
				
			if (pid.getPid5_PatientName().getFamilyName().getValue() != null)
				lastName = pid.getPid5_PatientName().getFamilyName().getValue();
			if (pid.getPid5_PatientName().getGivenName().getValue() != null)
				firstName = pid.getPid5_PatientName().getGivenName().getValue();
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
						
						XAD adr = pid.getPatientAddress(0);
						phone = pid.getPhoneNumberHome(0).getPhoneNumber().getValue();
						
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
								Country cc = Country.valueOf(adr.getCountry().getValue());
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
		String orderCommentNTE = getComments(oru.getRESPONSE().getORDER_OBSERVATION(idx), -1);
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
			if (comment != null && comment.getValue() != null) {
				if (commentNTE != null) {
					commentNTE += "\n";
				} else {
					commentNTE = "";
				}
				commentNTE += comment.getValue();
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
		
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_ED)) {
			String observationId =
				obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
				
			if (!"DOCUMENT".equals(observationId)) {
				logger.warn(MessageFormat.format(
					Messages.getString("HL7_ORU_R01.Error_WrongObsIdentifier"), observationId));
			}
			
			ED ed = (ED) obx.getObx5_ObservationValue(0).getData();
			String filename = ed.getEd3_DataSubtype().getValue();
			String encoding = ed.getEd4_Encoding().getValue();
			String data = ed.getEd5_Data().getValue();
			sequence = obx.getSetIDOBX().getValue();
			observationTime =
				obx.getObx14_DateTimeOfTheObservation().getTs1_TimeOfAnEvent().getValue();
			observation.add(new EncapsulatedData(filename, encoding, data, observationTime,
				commentNTE, group, sequence));
		} else if (isTextOrNumeric(valueType)) {
			name = obx.getObx3_ObservationIdentifier().getCe2_Text().getValue();
			if (name == null) {
				name = obx.getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().getValue();
			}
			String value = "";
			Object tmp = obx.getObx5_ObservationValue(0).getData();
			
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
			} else if (tmp instanceof SN) {
				value = ((SN) tmp).getSn2_Num1().getValue();
				if (value == null) {
					value = ((SN) tmp).getSn1_Comparator().getValue();
				}
			} else if (tmp instanceof CE) {
				value = ((CE) tmp).getCe2_Text().getValue();
			}
			
			itemCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
			unit = obx.getObx6_Units().getCe1_Identifier().getValue();
			range = obx.getObx7_ReferencesRange().getValue();
			flag = isPathologic(obx.getObx8_AbnormalFlags(0).getValue());
			observationTime =
				obx.getObx14_DateTimeOfTheObservation().getTs1_TimeOfAnEvent().getValue();
			status = obx.getObx11_ObservResultStatus().getValue();
			
			LabResultData lrd = new LabResultData(itemCode, name, unit, value, range, flag,
				defaultDateTime, observationTime, commentNTE, group, sequence, status,
				extractName(obx.getObx4_ObservationSubID()));
				
			if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_NM)
				|| valueType.equals(HL7Constants.OBX_VALUE_TYPE_SN)) {
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
				ORU_R01_RESPONSE pr = oru.getRESPONSE();
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
			XCN[] ops = orc.getOrderingProvider();
			for (XCN op : ops) {
				addNameValuesToOrcMessage(op.getFamilyName(), op.getGivenName(), orcMessage);
			}
			return orcMessage;
		}
		return null;
	}
}
