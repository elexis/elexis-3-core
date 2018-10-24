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
import ca.uhn.hl7v2.model.v251.datatype.CE;
import ca.uhn.hl7v2.model.v251.datatype.ED;
import ca.uhn.hl7v2.model.v251.datatype.FN;
import ca.uhn.hl7v2.model.v251.datatype.FT;
import ca.uhn.hl7v2.model.v251.datatype.NM;
import ca.uhn.hl7v2.model.v251.datatype.SN;
import ca.uhn.hl7v2.model.v251.datatype.ST;
import ca.uhn.hl7v2.model.v251.datatype.TX;
import ca.uhn.hl7v2.model.v251.datatype.XAD;
import ca.uhn.hl7v2.model.v251.datatype.XCN;
import ca.uhn.hl7v2.model.v251.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v251.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v251.group.OUL_R22_ORDER;
import ca.uhn.hl7v2.model.v251.group.OUL_R22_SPECIMEN;
import ca.uhn.hl7v2.model.v251.message.ORU_R01;
import ca.uhn.hl7v2.model.v251.message.OUL_R22;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.model.v251.segment.NTE;
import ca.uhn.hl7v2.model.v251.segment.OBR;
import ca.uhn.hl7v2.model.v251.segment.OBX;
import ca.uhn.hl7v2.model.v251.segment.ORC;
import ca.uhn.hl7v2.model.v251.segment.PID;
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
import ch.rgw.tools.TimeTool;

public class HL7ReaderV251 extends HL7Reader {
	static Logger logger = LoggerFactory.getLogger(HL7ReaderV251.class);
	
	private MSH msh;
	
	public HL7ReaderV251(Message message){
		super(message);
	}
	
	@Override
	public String getSender() throws ElexisException{
		String sender;
		try {
			MSH msh = (MSH) message.get("MSH");
			sender = msh.getMsh4_SendingFacility().getNamespaceID().getValue();
			if (sender == null) {
				sender = msh.getMsh3_SendingApplication().getNamespaceID().getValue();
				if (sender == null) {
					sender = "";
				}
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
		
		try {
			this.patientResolver = patientResolver;
			if (message.getName().contains("OUL_R22")) {
				readObservationOulR22(createIfNotFound);
			} else {
				readObservationOruR01(createIfNotFound);
			}
		} catch (HL7Exception | ParseException e) {
			throw new ElexisException(e.getMessage(), e);
		}
		
		return observation;
	}
	
	private void readObservationOruR01(boolean createIfNotFound)
		throws ParseException, HL7Exception{
		ORU_R01 oru = (ORU_R01) message;
		msh = oru.getMSH();
		
		PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
		// place order number
		String orderNumber = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC()
			.getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue();
		setPatient(pid, orderNumber, createIfNotFound);
		
		int obsCount = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
		for (int idx = 0; idx < obsCount; idx++) {
			OBR obr = oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx).getOBR();
			String obrObservationDateTime =
				obr.getObr7_ObservationDateTime().getTs1_Time().getValue();
				
			setOrderComment(oru, idx, obrObservationDateTime);
			
			for (int i = 0; i < oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx)
				.getOBSERVATIONReps(); i++) {
				ORU_R01_ORDER_OBSERVATION obs = oru.getPATIENT_RESULT().getORDER_OBSERVATION(idx);
				// get notes and comments
				String commentNTE = getComments(obs, i);
				
				// groupe and sequence
				String group = "";
				String sequence = "";
				for (int k = 0; k < 2; k++) {
					CE ce = obr.getObr47_FillerSupplementalServiceInformation(k);
					if (ce != null) {
						String code = "";
						if (ce.getCe3_NameOfCodingSystem() != null)
							code = ce.getCe3_NameOfCodingSystem().getValue();
							
						group = getGroup(code, ce);
						sequence = getSequence(code, ce);
						
					}
				}
				
				// result
				readOBXResults(obs.getOBSERVATION(i).getOBX(), commentNTE, group, sequence,
					obrObservationDateTime);
			}
		}
	}
	
	private void readObservationOulR22(boolean createIfNotFound)
		throws ParseException, HL7Exception{
		OUL_R22 oul = (OUL_R22) message;
		OUL_R22_ORDER order = oul.getSPECIMEN().getORDER();
		msh = oul.getMSH();
		
		PID pid = oul.getPATIENT().getPID();
		String orderNumber =
			order.getOBR().getObr2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue();
			
		setPatient(pid, orderNumber, createIfNotFound);
		int count = order.getRESULTReps();
		
		String lastObrObservationDateTime = null;
		for (int idx = 0; idx < count; idx++) {
			OBR obr = oul.getSPECIMEN(idx).getORDER().getOBR();
			String obrObservationDateTime =
				obr.getObr7_ObservationDateTime().getTs1_Time().getValue();
			if (obrObservationDateTime == null) {
				if (lastObrObservationDateTime != null) {
					obrObservationDateTime = lastObrObservationDateTime;
				} else {
					TimeTool time = new TimeTool();
					obrObservationDateTime = time.toString(TimeTool.TIMESTAMP);
				}
			} else {
				lastObrObservationDateTime = obrObservationDateTime;
			}
			oul.getSPECIMEN().getORDER(idx).getNTE();
			
			String commentNTE = null;
			for (int i = 0; i < oul.getSPECIMEN(idx).getORDERReps(); i++) {
				// get notes and comments
				for (int j = 0; j < oul.getSPECIMEN().getORDER(idx).getNTEReps(); j++) {
					AbstractPrimitive comment =
						oul.getSPECIMEN().getORDER(idx).getNTE(j).getNte3_Comment(0);
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
			}
			// groupe and sequence
			String group = "";
			String sequence = "";
			
			// result
			readOBXResults(order.getRESULT(idx).getOBX(), commentNTE, group, sequence,
				obrObservationDateTime);
		}
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
	
	private void setPatient(PID pid, String orderNumber, final boolean createIfNotFound)
		throws ParseException, HL7Exception{
		List<? extends IPatient> list = new ArrayList<IPatient>();
		String lastName = ""; //$NON-NLS-1$
		String firstName = ""; //$NON-NLS-1$
		String birthDate = ""; //$NON-NLS-1$
		String sex = Gender.FEMALE.value();
		pat = null;
		
		if (pat == null) {
			String patid = pid.getPatientID().getIDNumber().getValue();
			String patid_alternative =
				pid.getPid4_AlternatePatientIDPID(0).getCx1_IDNumber().getValue();
			if (StringTool.isNothing(patid)) {
				patid = pid.getPatientID().getCx1_IDNumber().getValue();
				if (StringTool.isNothing(patid)) {
					patid = pid.getPid2_PatientID().getIDNumber().getValue();
					if (StringTool.isNothing(patid)) {
						patid = pid.getAlternatePatientIDPID(0).getCx1_IDNumber().getValue();
						if (StringTool.isNothing(patid)) {
							patid = patid_alternative;
							if (patid == null) {
								patid = "";
							}
						}
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
			
			if (pid.getPid5_PatientName(0).getFamilyName().getFn1_Surname().getValue() != null)
				lastName = pid.getPid5_PatientName(0).getFamilyName().getFn1_Surname().getValue();
			if (pid.getPid5_PatientName(0).getGivenName().getValue() != null)
				firstName = pid.getPid5_PatientName(0).getGivenName().getValue();
			String patientName = firstName + " " + lastName;
			
			String sendingApplication =
				msh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
			String sendingFacility = msh.getMsh4_SendingFacility().getHd1_NamespaceID().getValue();
			String dateTimeOfMessage = msh.getMsh7_DateTimeOfMessage().getTs1_Time().getValue();
			String patientNotesAndComments  = ""; 
			
			observation = new ObservationMessage(sendingApplication, sendingFacility,
				dateTimeOfMessage, patid, patientName, patientNotesAndComments, patid_alternative, orderNumber);
				
			birthDate = pid.getDateTimeOfBirth().getTs1_Time().getValue();
			sex = pid.getAdministrativeSex().getValue();
			
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
						phone = pid.getPhoneNumberHome(0).getTelephoneNumber().getValue();
						
						pat = patientResolver.createPatient(lastName, firstName, birthDate, sex);
						pat.setPatientNr(patid);
						
						if (adr != null) {
							if (adr.getStreetAddress() != null) {
								pat.setStreet(adr.getStreetAddress().getComponent(0).toString());
							}
							if (adr.getZipOrPostalCode() != null) {
								pat.setZip(adr.getZipOrPostalCode().getValue());
							}
							if (adr.getCity() != null) {
								pat.setCity(adr.getCity().getValue());
							}
							if (adr.getCountry() != null) {
								try {
									Country cc = Country.valueOf(adr.getCountry().getValue());
									pat.setCountry(cc);
								} catch (Exception e) {
									// unknown country, just move on
								}
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
				commentNTE += comment.getValue();
			}
		}
		return commentNTE;
	}
	
	// ORU_R01_OBSERVATION obs.getobx
	private void readOBXResults(OBX obx, String commentNTE, String group, String sequence,
		String defaultDateTime) throws ParseException{
		String valueType = obx.getObx2_ValueType().getValue();
		String name = "";
		String itemCode = "";
		String unit = "";
		String range = "";
		String observationTime = obx.getObx14_DateTimeOfTheObservation().getTs1_Time().getValue();
		String status = "";
		Boolean flag;
		String rawAbnormalFlags;
		
		if (valueType.equals(HL7Constants.OBX_VALUE_TYPE_ED)) {
			String observationId =
				obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
				
			if (!"DOCUMENT".equals(observationId)) {
				logger.warn(MessageFormat.format(
					Messages.HL7_ORU_R01_Error_WrongObsIdentifier, observationId));
			}
			
			ED ed = (ED) obx.getObx5_ObservationValue(0).getData();
			String filename = ed.getEd3_DataSubtype().getValue();
			String encoding = ed.getEd4_Encoding().getValue();
			String data = ed.getEd5_Data().getValue();
			sequence = obx.getSetIDOBX().getValue();
			if (observationTime == null) {
				observationTime = obx.getObx19_DateTimeOfTheAnalysis().getTs1_Time().getValue();
			}
			
			observation.add(new EncapsulatedData(filename, encoding, data, observationTime,
				commentNTE, group, sequence));
		} else if (isTextOrNumeric(valueType)) {
			name = determineName(obx);
			
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
				// look for non numeric value
				if (value == null) {
					value = ((SN) tmp).getSn1_Comparator().getValue();
				}
			} else if (tmp instanceof CE) {
				value = ((CE) tmp).getCe2_Text().getValue();
			}
			
			itemCode = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
			unit = obx.getObx6_Units().getCe1_Identifier().getValue();
			range = obx.getObx7_ReferencesRange().getValue();
			rawAbnormalFlags = obx.getObx8_AbnormalFlags(0).getValue();
			flag = isPathologic(rawAbnormalFlags);
			if (observationTime == null) {
				observationTime = obx.getObx19_DateTimeOfTheAnalysis().getTs1_Time().getValue();
			}
			status = obx.getObx11_ObservationResultStatus().getValue();
			
			LabResultData lrd = new LabResultData(itemCode, name, unit, value, range, flag, rawAbnormalFlags,
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
	
	private String determineName(OBX obx){
		List<String> possibleNames = new ArrayList<>();
		possibleNames.add(obx.getObx4_ObservationSubID().getValue());
		possibleNames.add(obx.getObx3_ObservationIdentifier().getCe2_Text().getValue());
		possibleNames.add(obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue());
		return HL7Helper.determineName(possibleNames);
	}

	@Override
	public OrcMessage getOrcMessage(){
		try {
			if (message instanceof ORU_R01) {
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
			} else if (message instanceof OUL_R22) {
				OUL_R22 oul = (OUL_R22) message;
				if (oul != null) {
					OUL_R22_SPECIMEN specimen = oul.getSPECIMEN();
					if (specimen != null) {
						OUL_R22_ORDER oo = specimen.getORDER();
						if (oo != null) {
							return extractOrc(oo.getORC());
						}
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
				FN fn = op.getFamilyName();
				ST familyName = null;
				if (fn != null) {
					familyName = fn.getSurname();
					if (familyName == null) {
						familyName = fn.getOwnSurname();
					}
				}
				addNameValuesToOrcMessage(op.getGivenName(), familyName, orcMessage);
			}
			return orcMessage;
		}
		return null;
	}
}
