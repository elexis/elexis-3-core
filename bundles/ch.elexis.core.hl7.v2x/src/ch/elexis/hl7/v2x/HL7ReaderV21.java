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
import ca.uhn.hl7v2.model.v21.datatype.AD;
import ca.uhn.hl7v2.model.v21.datatype.CE;
import ca.uhn.hl7v2.model.v21.datatype.CN;
import ca.uhn.hl7v2.model.v21.datatype.FT;
import ca.uhn.hl7v2.model.v21.datatype.NM;
import ca.uhn.hl7v2.model.v21.datatype.ST;
import ca.uhn.hl7v2.model.v21.datatype.TX;
import ca.uhn.hl7v2.model.v21.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v21.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v21.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v21.message.ORU_R01;
import ca.uhn.hl7v2.model.v21.segment.MSH;
import ca.uhn.hl7v2.model.v21.segment.NTE;
import ca.uhn.hl7v2.model.v21.segment.OBR;
import ca.uhn.hl7v2.model.v21.segment.OBX;
import ca.uhn.hl7v2.model.v21.segment.ORC;
import ca.uhn.hl7v2.model.v21.segment.PID;
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
import ch.rgw.tools.TimeTool;

public class HL7ReaderV21 extends HL7Reader {
	static Logger logger = LoggerFactory.getLogger(HL7ReaderV21.class);
	
	public HL7ReaderV21(Message message){
		super(message);
	}
	
	@Override
	public String getSender() throws ElexisException{
		String sender;
		MSH msh;
		try {
			msh = (MSH) message.get("MSH");
			sender = msh.getMsh4_SENDINGFACILITY().getValue();
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
				String obrObservationDateTime = obr.getObr7_OBSERVATIONDATETIME().getValue();
				
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
						CE ce = obr.getObr4_UNIVERSALSERVICEIDENT(); // .getObr47_FillerSupplementalServiceInformation(k);
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
			
		} catch (ParseException | HL7Exception e) {
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
			String sendingApplication = oru.getMSH().getMsh3_SENDINGAPPLICATION().getValue();
			String sendingFacility = oru.getMSH().getMsh4_SENDINGFACILITY().getValue();
			String dateTimeOfMessage = oru.getMSH().getMsh7_DATETIMEOFMESSAGE().getValue();
			
			PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
			
			String patid = pid.getPid3_PATIENTIDINTERNALINTERNALID().getCk1_IDNumber().getValue();
			String patid_alternative = pid.getPid4_ALTERNATEPATIENTID().getValue();
			if (StringTool.isNothing(patid)) {
				patid = pid.getPid2_PATIENTIDEXTERNALEXTERNALID().getCk1_IDNumber().getValue();
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
				.getOrc2_PLACERORDER().getComponent(0).toString();
				
			if (pid.getPid5_PATIENTNAME().getPn1_FamilyName().getValue() != null)
				lastName = pid.getPid5_PATIENTNAME().getPn1_FamilyName().getValue();
			if (pid.getPid5_PATIENTNAME().getPn2_GivenName().getValue() != null)
				firstName = pid.getPid5_PATIENTNAME().getGivenName().getValue();
			String patientName = firstName + " " + lastName;
			String patientNotesAndComments = readPatientNotesAndComments(oru.getPATIENT_RESULT().getPATIENT());
			
			observation = new ObservationMessage(sendingApplication, sendingFacility,
				dateTimeOfMessage, patid, patientName, patientNotesAndComments, patid_alternative, orderNumber);
				
			birthDate = pid.getPid7_DATEOFBIRTH().getValue();
			sex = pid.getPid8_SEX().getValue();
			
			if ((patid == null) || (list.size() != 1)) {
				// We did not find the patient using the PatID, so we try the
				// name and birthdate			
				list = patientResolver.findPatientByNameAndBirthdate(StringTool.normalizeCase(lastName), StringTool.normalizeCase(firstName), new TimeTool(birthDate).toString(TimeTool.DATE_COMPACT));
				
				if ((list != null) && (list.size() == 1)) {
					pat = list.get(0);
				} else {
					if (createIfNotFound) {
						String phone = StringConstants.EMPTY;
						AD adr = pid.getPid11_PATIENTADDRESS();
						phone = pid.getPid13_PHONENUMBERHOME(0).getValue();
						
						pat = patientResolver.createPatient(lastName, firstName, birthDate, sex);
						pat.setPatientNr(patid);
						
						if (adr.getAd1_StreetAddress().getValue() != null) {
							pat.setStreet(adr.getAd1_StreetAddress().getValue());
						}
						if (adr.getAd5_Zip().getValue() != null) {
							pat.setZip(adr.getAd5_Zip().getValue());
						}
						if (adr.getAd3_City().getValue() != null) {
							pat.setCity(adr.getAd3_City().getValue());
						}
						if (adr.getAd6_Country().getValue() != null) {
							Country country = Country.fromValue(adr.getAd6_Country().getValue());
							pat.setCountry(Country.valueOf(country.name()));
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
	
	private String readPatientNotesAndComments(ca.uhn.hl7v2.model.v21.group.ORU_R01_PATIENT oru_R01_PATIENT){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < oru_R01_PATIENT.getNTEReps(); i++) {
			TX comment = oru_R01_PATIENT.getNTE(i).getCOMMENT(0);
			sb.append(comment.toString());
			if (oru_R01_PATIENT.getNTEReps() > i) {
				sb.append("\n");
			}
		}
		return sb.toString();
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
			AbstractPrimitive comment = nte.getNte3_COMMENT(0);
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
		String valueType = obx.getObx2_VALUETYPE().getValue();
		String name = "";
		String itemCode = "";
		String unit = "";
		String range = "";
		String observationTime = "";
		String status = "";
		Boolean flag;
		String rawAbnormalFlag;
		
		if (isTextOrNumeric(valueType)) {
			name = obx.getObx4_OBSERVATIONSUBID().getValue();
			if (name == null) {
				name = obx.getObx3_OBSERVATIONIDENTIFIER().getCe2_Text().getValue();
				if (name == null) {
					name = obx.getObx3_OBSERVATIONIDENTIFIER().getCe1_Identifier().getValue();
				}
			}
			String value = "";
			Object tmp = obx.getObx5_OBSERVATIONRESULTS().getData();
			
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
			
			itemCode = obx.getObx3_OBSERVATIONIDENTIFIER().getCe1_Identifier().getValue();
			unit = obx.getObx6_UNITS().getValue();
			range = obx.getObx7_REFERENCESRANGE().getValue();
			rawAbnormalFlag = obx.getObx8_ABNORMALFLAGS(0).getValue();
			flag = isPathologic(obx.getObx8_ABNORMALFLAGS(0).getValue());
			observationTime = defaultDateTime;
			status = obx.getObx11_OBSERVRESULTSTATUS().getValue();
			
			LabResultData lrd = new LabResultData(itemCode, name, unit, value, range, flag, rawAbnormalFlag,
				defaultDateTime, observationTime, commentNTE, group, sequence, status,
				extractName(obx.getObx4_OBSERVATIONSUBID()));
				
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
			CN ops = orc.getORDERINGPROVIDER();
			addNameValuesToOrcMessage(ops.getFamilyName(), ops.getGivenName(), orcMessage);
			return orcMessage;
		}
		return null;
	}
}
