package ch.elexis.hl7.v26;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;

import org.apache.commons.codec.binary.Base64;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v26.datatype.FT;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v26.datatype.CWE;
import ca.uhn.hl7v2.model.v26.datatype.ED;
import ca.uhn.hl7v2.model.v26.datatype.NM;
import ca.uhn.hl7v2.model.v26.datatype.ST;
import ca.uhn.hl7v2.model.v26.datatype.TX;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v26.message.ORU_R01;
import ca.uhn.hl7v2.model.v26.segment.NTE;
import ca.uhn.hl7v2.model.v26.segment.OBR;
import ca.uhn.hl7v2.model.v26.segment.OBX;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
//import ca.uhn.hl7v2.HL7Exception;
//import ca.uhn.hl7v2.model.AbstractPrimitive;
//import ca.uhn.hl7v2.model.DataTypeException;
//import ca.uhn.hl7v2.model.Message;
//import ca.uhn.hl7v2.model.Type;
//import ca.uhn.hl7v2.model.v26.datatype.CWE;
//import ca.uhn.hl7v2.model.v26.datatype.ED;
//import ca.uhn.hl7v2.model.v26.datatype.NM;
//import ca.uhn.hl7v2.model.v26.datatype.ST;
//import ca.uhn.hl7v2.model.v26.datatype.TX;
//import ca.uhn.hl7v2.model.v26.group.ORU_R01_ORDER_OBSERVATION;
//import ca.uhn.hl7v2.model.v26.message.ORU_R01;
//import ca.uhn.hl7v2.model.v26.segment.NTE;
//import ca.uhn.hl7v2.model.v26.segment.OBR;
//import ca.uhn.hl7v2.model.v26.segment.OBX;
//import ca.uhn.hl7v2.model.v26.segment.PID;
//import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
//import ca.uhn.hl7v2.parser.Parser;
//import ca.uhn.hl7v2.parser.PipeParser;
import ch.elexis.hl7.HL7Writer;
import ch.elexis.hl7.data.HL7LaborItem;
import ch.elexis.hl7.data.HL7LaborItem.Typ;
import ch.elexis.hl7.data.HL7LaborWert;
import ch.elexis.hl7.data.HL7Mandant;
import ch.elexis.hl7.data.HL7Patient;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.StringData;
import ch.elexis.hl7.model.TextData;

public class HL7_ORU_R01 extends HL7Writer {
	
	// constants for OBR-47
	public static final String CODINGSYSTEM_DORNER_GROUP_CODE = "99DGC"; //$NON-NLS-1$
	public static final String CODINGSYSTEM_DORNER_GROUP_POSITION = "99DGP"; //$NON-NLS-1$
	
	final String uniqueMessageControlID;
	final String uniqueProcessingID;
	final HL7Mandant mandant;
	
	public HL7_ORU_R01(){
		super();
		this.uniqueMessageControlID = null;
		this.uniqueProcessingID = null;
		this.mandant = null;
	}
	
	public HL7_ORU_R01(final String sendingApplication1, final String sendingApplication3,
		final String receivingApplication1, final String receivingApplication3,
		final String receivingFacility, final String uniqueMessageControlID,
		final String uniqueProcessingID, HL7Mandant mandant){
		super(sendingApplication1, sendingApplication3, receivingApplication1,
			receivingApplication3, receivingFacility);
		this.uniqueMessageControlID = uniqueMessageControlID;
		this.uniqueProcessingID = uniqueProcessingID;
		this.mandant = mandant;
	}
	
	/**
	 * Reads an ORU_R01 HL7 file
	 * 
	 * @param text
	 *            ISO-8559-1 String
	 * @return
	 * @throws HL7Exception
	 */
	public ORU_R01 read(String text) throws HL7Exception{
		Parser p = new PipeParser();
		p.setValidationContext(new ElexisValidation());
		Message hl7Msg = p.parse(text);
		if (hl7Msg instanceof ORU_R01) {
			return (ORU_R01) hl7Msg;
		} else {
			addError(MessageFormat.format(
				Messages.HL7_ORU_R01_Error_WrongMsgType, hl7Msg.getName())); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Reads an observation ORU_R01 HL7 file
	 * 
	 * @param text
	 *            ISO-8559-1 String
	 * @return
	 * @throws IOException
	 * @throws EncodingNotSupportedException
	 * @throws HL7Exception
	 * @throws ParseException
	 */
	public ObservationMessage readObservation(final String text) throws IOException,
		EncodingNotSupportedException, HL7Exception, ParseException{
		clearMessages();
		ObservationMessage observation = null;
		
		ORU_R01 oru = read(text);
		if (oru != null) {
			String msh3_sendingApplication =
				oru.getMSH().getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
			String msh4_sendingFacility =
				oru.getMSH().getMsh4_SendingFacility().getHd1_NamespaceID().getValue();
			String msh7_dateTimeOfMessage = oru.getMSH().getMsh7_DateTimeOfMessage().getValue();
			
			PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
			String pid2_patientId = pid.getPid2_PatientID().getCx1_IDNumber().getValue();
			String pid4_alternatePatientId =
				pid.getPid4_AlternatePatientIDPID(0).getCx1_IDNumber().getValue();
			String tmp1 = "";
			String tmp2 = "";
			if (pid.getPid5_PatientName(0).getName() != null)
				tmp1 = pid.getPid5_PatientName(0).getFamilyName().getFn1_Surname().getValue();
			if (pid.getPid5_PatientName(0).getFamilyName() != null)
				tmp2 = pid.getPid5_PatientName(0).getGivenName().getValue();
			String pid5_patientName = tmp1 + " " + tmp2;
			String nteAfterPid_patientNotesAndComments = readPatientNotesAndComments(oru.getPATIENT_RESULT().getPATIENT());
			String orc2_placerOrderNumber =
				oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC().getOrc2_PlacerOrderNumber()
					.getEi1_EntityIdentifier().getValue();
			observation =
				new ObservationMessage(msh3_sendingApplication, msh4_sendingFacility,
					msh7_dateTimeOfMessage, pid2_patientId, pid5_patientName, nteAfterPid_patientNotesAndComments,
					pid4_alternatePatientId, orc2_placerOrderNumber);
			
			int obscount = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
			for (int j = 0; j < obscount; j++) {
				String appendedTX = ""; //$NON-NLS-1$
				OBR obr = oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getOBR();
				String obrDateOfObservation = obr.getObr7_ObservationDateTime().getValue();
				
				// Order Comments
				String orderCommentNTE = null;
				for (int n = 0; n < oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getNTEReps(); n++) {
					NTE nte = oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getNTE(n);
					AbstractPrimitive comment = nte.getNte3_Comment(0);
					if (comment != null) {
						if (orderCommentNTE != null) {
							orderCommentNTE += "\n";
						} else {
							orderCommentNTE = "";
						}
						orderCommentNTE += comment.getValue();
					}
				}
				if (orderCommentNTE != null) {
					observation.add(new TextData(HL7Constants.COMMENT_NAME, orderCommentNTE,
						obrDateOfObservation, HL7Constants.COMMENT_GROUP, null));
				}
				
				for (int i = 0; i < oru.getPATIENT_RESULT().getORDER_OBSERVATION(j)
					.getOBSERVATIONReps(); i++) {
					// Notes and Comments (NTE)
					String commentNTE = null;
					for (int n = 0; n < oru.getPATIENT_RESULT().getORDER_OBSERVATION(j)
						.getOBSERVATION(i).getNTEReps(); n++) {
						NTE nte =
							oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getOBSERVATION(i)
								.getNTE(n);
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
					
					// Observation
					// Gruppe/Sortierung
					String group = null;
					String sequence = null;
					for (int k = 0; k < 2; k++) {
						CWE cwe = obr.getObr47_FillerSupplementalServiceInformation(k);
						if (cwe != null) {
							String code = "";
							if (cwe.getCwe3_NameOfCodingSystem() != null)
								code = cwe.getCwe3_NameOfCodingSystem().getValue();
							if (CODINGSYSTEM_DORNER_GROUP_CODE.equalsIgnoreCase(code)) {
								if (cwe.getCwe2_Text() != null) {
									group = cwe.getCwe2_Text().getValue();
								}
							}
							if (CODINGSYSTEM_DORNER_GROUP_POSITION.equalsIgnoreCase(code)) {
								if (cwe.getCwe1_Identifier() != null) {
									sequence = cwe.getCwe1_Identifier().getValue();
								}
							}
						}
					}
					
					// Resultate
					OBX obx =
						oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getOBSERVATION(i).getOBX();
					String valueType = obx.getObx2_ValueType().getValue();
					if (HL7Constants.OBX_VALUE_TYPE_ED.equals(valueType)) {
						String observationId =
							obx.getObx3_ObservationIdentifier().getCwe1_Identifier().getValue();
						if (!"DOCUMENT".equals(observationId)) { //$NON-NLS-1$
							addWarning(MessageFormat.format(
								Messages.HL7_ORU_R01_Error_WrongObsIdentifier,
								observationId));
						}
						ED ed = (ED) obx.getObx5_ObservationValue(0).getData();
						String filename = ed.getEd3_DataSubtype().getValue();
						String encoding = ed.getEd4_Encoding().getValue();
						String data = ed.getEd5_Data().getValue();
						String dateOfObservation =
							obx.getObx14_DateTimeOfTheObservation().getValue();
						observation.add(new EncapsulatedData(filename, encoding, data,
							dateOfObservation, commentNTE, group, sequence));
					} else if (HL7Constants.OBX_VALUE_TYPE_ST.equals(valueType)) {
						String name = obx.getObx4_ObservationSubID().getValue();
						
						String valueST = ""; //$NON-NLS-1$
						Object value = obx.getObx5_ObservationValue(0).getData();
						if (value instanceof ST) {
							valueST = ((ST) obx.getObx5_ObservationValue(0).getData()).getValue();
						}
						String unit = obx.getObx6_Units().getCwe1_Identifier().getValue();
						String range = obx.getObx7_ReferencesRange().getValue();
						String dateOfObservation =
							obx.getObx14_DateTimeOfTheObservation().getValue();
						observation.add(new StringData(name, unit, valueST, range,
							dateOfObservation, commentNTE, group, sequence));
					} else if (HL7Constants.OBX_VALUE_TYPE_TX.equals(valueType)) {
						String valueTX = ""; //$NON-NLS-1$
						Object value = obx.getObx5_ObservationValue(0).getData();
						if (value instanceof TX) {
							valueTX = ((TX) obx.getObx5_ObservationValue(0).getData()).getValue();
						}
						appendedTX += valueTX + "\n"; //$NON-NLS-1$
					} else {
						addError(MessageFormat.format("Value type {0} is not implemented!", //$NON-NLS-1$
							valueType));
					}
				}
				
				if (appendedTX.length() > 0) {
					// Find name in CWE: <Identifier>^<Text>. <br>
					// <Text> if exists else use <Identifier>
					String name = null;
					CWE cweIdentifier = obr.getObr4_UniversalServiceIdentifier();
					if (cweIdentifier.getCwe2_Text() != null) {
						name = cweIdentifier.getCwe2_Text().getValue();
					}
					if (name == null || name.trim().length() == 0) {
						name = cweIdentifier.getCwe1_Identifier().getValue();
					}
					observation
						.add(new TextData(name, appendedTX, obrDateOfObservation, null, null));
				}
			}
		}
		
		return observation;
	}
	
	private String readPatientNotesAndComments(ORU_R01_PATIENT patient){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < patient.getNTEReps(); i++) {
			FT comment = patient.getNTE(i).getComment(0);
			sb.append(comment.toString());
			if (patient.getNTEReps() > i) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Creates an ORU_R01 message
	 * 
	 * @param patient
	 * @param labItem
	 * @param labwert
	 * 
	 * @return
	 */
	public String createText(final HL7Patient patient, final HL7LaborItem labItem,
		final HL7LaborWert labwert) throws DataTypeException, HL7Exception{
		
		ORU_R01 oru = new ORU_R01();
		// Message
		fillMSH(oru.getMSH(), "ORU", "R01", mandant, this.uniqueMessageControlID, //$NON-NLS-1$ //$NON-NLS-2$
			this.uniqueProcessingID, patient); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Patient
		PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
		fillPID(pid, patient);
		
		ORU_R01_ORDER_OBSERVATION orderObservation = oru.getPATIENT_RESULT().getORDER_OBSERVATION();
		fillORC(orderObservation.getORC(), "RE", null); //$NON-NLS-1$
		
		addResultInternal(oru, patient, labItem, labwert, 0);
		
		// Now, let's encode the message and look at the output
		Parser parser = new PipeParser();
		return parser.encode(oru);
	}
	
	/**
	 * Adds a ORU_R01 observation result
	 * 
	 * @param oru
	 * @param patient
	 * @param labItem
	 * @param labwert
	 * @return
	 */
	public String addResult(final ORU_R01 oru, final HL7Patient patient,
		final HL7LaborItem labItem, final HL7LaborWert labwert) throws DataTypeException,
		HL7Exception{
		int reps = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
		return addResultInternal(oru, patient, labItem, labwert, reps);
	}
	
	/**
	 * Adds a ORU_R01 observation result
	 * 
	 * @param oru
	 * @param patient
	 * @param labItem
	 * @param labwert
	 * @param initial
	 * @return
	 */
	private String addResultInternal(final ORU_R01 oru, final HL7Patient patient,
		final HL7LaborItem labItem, final HL7LaborWert labwert, int orderObservationIndex)
		throws DataTypeException, HL7Exception{
		
		// Observation
		ORU_R01_ORDER_OBSERVATION orderObservation =
			(ORU_R01_ORDER_OBSERVATION) oru.getPATIENT_RESULT().getORDER_OBSERVATION(
				orderObservationIndex);
		fillOBR(orderObservation.getOBR(), orderObservationIndex, labItem);
		fillOBX(orderObservation.getOBSERVATION().getOBX(), patient, labItem, labwert);
		if (labwert.getKommentar() != null && labwert.getKommentar().length() > 0) {
			fillNTE(orderObservation.getNTE(), labwert);
		}
		// Now, let's encode the message and look at the output
		Parser parser = new PipeParser();
		return parser.encode(oru);
	}
	
	@Override
	public String getVersion(){
		return "2.6"; //$NON-NLS-1$
	}
	
	/**
	 * Adds labor data to CWE segment
	 * 
	 * @param cwe
	 * @param labItem
	 */
	private void fillCWE(final CWE cwe, final HL7LaborItem laborItem, final HL7LaborWert laborWert)
		throws DataTypeException{
		// OBX-3: Observation Identifier <LabResult.ID>^<LabItems.KUERZEL>^^^^^^^<LabItems.TITEL>
		cwe.getCwe1_Identifier().setValue(laborWert.getId());
		cwe.getCwe2_Text().setValue(laborItem.getKuerzel());
		cwe.getCwe9_OriginalText().setValue(laborItem.getTitel());
	}
	
	/**
	 * Fills OBR segment
	 * 
	 * @param obr
	 * @param labItem
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillOBR(final OBR obr, final int index, final HL7LaborItem labItem)
		throws DataTypeException, HL7Exception{
		obr.getObr1_SetIDOBR().setValue(new Integer(index + 1).toString());
		
		// OBR-4: Observation Identifier <LabResult.ID>^<LabItems.KUERZEL>^^^^^^^<LabItems.TITEL>
		CWE cwe4 = obr.getObr4_UniversalServiceIdentifier();
		cwe4.getCwe1_Identifier().setValue(labItem.getId());
		cwe4.getCwe2_Text().setValue(labItem.getKuerzel());
		cwe4.getCwe9_OriginalText().setValue(labItem.getTitel());
		
		// OBR-47: <ID der Gruppe>^<Gruppe>^99DGC~<Position>^^99DGP
		CWE egc = obr.getFillerSupplementalServiceInformation(0);
		egc.getCwe2_Text().setValue(labItem.getGruppe());
		egc.getCwe3_NameOfCodingSystem().setValue("99EGC");
		CWE egp = obr.getFillerSupplementalServiceInformation(1);
		egp.getCwe1_Identifier().setValue(labItem.getPrio());
		egp.getCwe3_NameOfCodingSystem().setValue("99EGP");
	}
	
	/**
	 * Fills OBX segment
	 * 
	 * @param obx
	 * @param labItem
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillOBX_TX(final OBX obx, final int index, final String text)
		throws DataTypeException, HL7Exception{
		// OBX|35|TX||| Augmentin S
		obx.getObx1_SetIDOBX().setValue(new Integer(index + 1).toString()); //$NON-NLS-1$
		obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_TX);
		TX textType = new TX(null);
		textType.setValue(text);
		obx.getObx5_ObservationValue(0).setData(textType);
	}
	
	/**
	 * Zahl:
	 * <ul>
	 * <li>Wenn <LabResult.RESULTAT> kleiner als Ref Mann?, bzw. Ref Frau?, dann "L"</li>
	 * <li>Wenn <LabResult.RESULTAT> grösser als Ref Mann?, bzw. Ref Frau?, dann "H"</li>
	 * <li>Wenn <LabResult.RESULTAT> innerhalb Ref Mann?, bzw. Ref Frau?, dann "N"</li>
	 * <li>Wenn Text == <LabResult.RESULTAT> dann "N" sonst "A"</li>
	 * </ul>
	 * Wenn kein Referenzbereich, dann keine Angabe im Abnormal-Flag
	 * 
	 * @param value
	 * @return Abnormal Flags "L"=Low, "H"=High, "N"=Normal, "A"=Abnormal
	 */
	private String getAbnormalFlag(HL7Patient patient, final HL7LaborItem labItem,
		final HL7LaborWert laborWert){
		String resultat = laborWert.getResultat();
		
		String refValue = labItem.getRefFrau();
		if (patient.isMale()) {
			refValue = labItem.getRefMann();
		}
		
		if (refValue == null)
			return "";
		if (refValue == "")
			return "";
		
		if (resultat != null) {
			Double doubleObj = null;
			try {
				doubleObj = Double.parseDouble(resultat);
			} catch (NumberFormatException e) {
				// Do nothing. Just go on as text
			}
			if (doubleObj != null) {
				// Numeric value
				if (refValue == null) {
					return "N"; //$NON-NLS-1$
				} else {
					if (refValue.trim().startsWith("<")) { //$NON-NLS-1$
						try {
							double ref = Double.parseDouble(refValue.substring(1).trim());
							if (doubleObj.doubleValue() <= ref) {
								return "N"; //$NON-NLS-1$
							} else {
								return "H"; //$NON-NLS-1$
							}
						} catch (NumberFormatException nfe) {
							// don't mind
						}
					} else if (refValue.trim().startsWith(">")) { //$NON-NLS-1$
						try {
							double ref = Double.parseDouble(refValue.substring(1).trim());
							if (doubleObj >= ref) {
								return "N"; //$NON-NLS-1$
							} else {
								return "L"; //$NON-NLS-1$
							}
						} catch (NumberFormatException nfe) {
							// again, don't mind
						}
					} else {
						String[] range = refValue.split("\\s*-\\s*"); //$NON-NLS-1$
						if (range.length == 2) {
							try {
								double lower = Double.parseDouble(range[0]);
								double upper = Double.parseDouble(range[1]);
								if (doubleObj.doubleValue() < lower) {
									return "L"; //$NON-NLS-1$
								} else if (doubleObj.doubleValue() > upper) {
									return "H"; //$NON-NLS-1$
								} else {
									return "N"; //$NON-NLS-1$
								}
							} catch (NumberFormatException nre) {
								// still, we don't mind
							}
						}
					}
				}
			} else {
				if (laborWert.getFlags() == 0) {
					return "N";
				}
			}
		}
		return "A"; //$NON-NLS-1$
	}
	
	/**
	 * Fills OBX segment
	 * 
	 * @param obx
	 * @param labItem
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillOBX(final OBX obx, final HL7Patient patient, final HL7LaborItem laborItem,
		final HL7LaborWert laborWert) throws DataTypeException, HL7Exception{
		obx.getObx1_SetIDOBX().setValue("1"); //$NON-NLS-1$
		
		Type type = null;
		if (laborItem.getTyp().equals(Typ.NUMERIC)) {
			obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_NM);
			// Entweder NM bei numerischen Resultat oder TX bei Textresultat
			Double doubleObj = null;
			try {
				doubleObj = Double.parseDouble(laborWert.getResultat());
			} catch (NumberFormatException e) {
				// Do nothing. Just go on as text
			}
			if (doubleObj != null) {
				// Numerisch
				NM numericType = new NM(null);
				numericType.setValue(laborWert.getResultat().trim());
				type = numericType;
			} else {
				obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_TX);
				TX textType = new TX(null);
				textType.setValue(laborWert.getResultat());
				type = textType;
			}
		} else if (laborItem.getTyp().equals(Typ.TEXT)) {
			obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_TX);
			TX textType = new TX(null);
			textType.setValue(laborWert.getResultat());
			type = textType;
		} else if (laborItem.getTyp().equals(Typ.ABSOLUTE)) {
			obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_CWE);
			CWE codedEntryType = new CWE(null);
			String labResult = Messages.HL7_ORU_R01_LabResult_Abs_Neg;
			if (laborWert.getResultat() != null) {
				String trimLowercaseResult = laborWert.getResultat().trim().toLowerCase();
				if (trimLowercaseResult.startsWith("pos") || trimLowercaseResult.startsWith("+")) { //$NON-NLS-1$ //$NON-NLS-2$
					labResult = Messages.HL7_ORU_R01_LabResult_Abs_Pos;
				}
			}
			codedEntryType.getCwe1_Identifier().setValue(labResult);
			codedEntryType.getCwe2_Text().setValue(laborWert.getResultat());
			type = codedEntryType;
		} else if (laborItem.getTyp().equals(Typ.FORMULA)) {
			// Entweder NM bei numerischen Resultat oder TX bei Textresultat
			Double doubleObj = null;
			try {
				doubleObj = Double.parseDouble(laborWert.getResultat());
			} catch (NumberFormatException e) {
				// Do nothing. Just go on as text
			}
			if (doubleObj != null) {
				// Numerisch
				obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_NM);
				NM numericType = new NM(null);
				numericType.setValue(laborWert.getResultat());
				type = numericType;
			} else {
				// Text
				obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_TX);
				TX textType = new TX(null);
				textType.setValue(laborWert.getResultat());
				type = textType;
			}
		} else if (laborItem.getTyp().equals(Typ.DOCUMENT)) {
			if (laborWert.getDocData() != null) {
				obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_ED);
				// ^application^^BASE64^<LabResult.RESULTAT(Base64-codiert)>
				ED encapsulatedType = new ED(null);
				encapsulatedType.getEd2_TypeOfData().setValue("application"); //$NON-NLS-1$
				encapsulatedType.getEd4_Encoding().setValue("BASE64"); //$NON-NLS-1$
				String base64Value =
					new String(Base64.encodeBase64(laborWert.getResultat().getBytes()));
				encapsulatedType.getEd5_Data().setValue(base64Value);
				type = encapsulatedType;
			} else {
				// Sonst einfach TX Wert
				obx.getObx2_ValueType().setValue(HL7Constants.OBX_VALUE_TYPE_TX);
				TX textType = new TX(null);
				textType.setValue(laborWert.getResultat());
				type = textType;
			}
		}
		
		fillCWE(obx.getObx3_ObservationIdentifier(), laborItem, laborWert);
		// OBX-5: Observation Value <LabResult.RESULTAT>, bwz bei Dokument:
		// ^application^^BASE64^<LabResult.RESULTAT(Base64-codiert)>
		obx.getObx5_ObservationValue(0).setData(type);
		
		// OBX-6: Units <LabItems.EINHEIT>
		obx.getObx6_Units().getCwe1_Identifier().setValue(laborItem.getEinheit());
		// OBX-7: References Range <LabItems.REFMANN>, bzw <LabItems.REFFRAU> je nach Geschlecht
		String refRange = "";
		if (patient.isMale()) {
			refRange = laborItem.getRefMann();
		} else {
			refRange = laborItem.getRefFrau();
		}
		if (refRange != null) {
			obx.getObx7_ReferencesRange().setValue(refRange.trim());
		}
		// OBX-8: Abnormal flags "L" (Low), bzw "H" (High) bei Zahlen. Sonst "N"
		obx.getObx8_AbnormalFlags(0).setValue(getAbnormalFlag(patient, laborItem, laborWert));
		// OBX-11: Observation Result Status "F" (Final Result)
		obx.getObx11_ObservationResultStatus().setValue("F"); //$NON-NLS-1$
		// OBX-14: Date/Time of Observation <LabResult.DATUM>
		obx.getObx14_DateTimeOfTheObservation().setValue(laborWert.getZeitpunkt());
	}
	
	/**
	 * Fills NTE segment
	 * 
	 * @param nte
	 * @param labItem
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillNTE(final NTE nte, final HL7LaborWert laborWert) throws DataTypeException,
		HL7Exception{
		nte.getNte1_SetIDNTE().setValue("1"); //$NON-NLS-1$
		nte.getNte3_Comment(0).setValue(laborWert.getKommentar().replace("\n", ";"));
	}
}
