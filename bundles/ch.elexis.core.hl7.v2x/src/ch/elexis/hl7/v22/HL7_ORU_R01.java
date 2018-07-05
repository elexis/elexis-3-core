/*******************************************************************************
 * 
 * The authorship of this code and the accompanying materials is held by 
 * medshare GmbH, Switzerland. All rights reserved. 
 * http://medshare.net
 * 
 * This code and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0
 * 
 * Year of publication: 2012
 * 
 *******************************************************************************/
package ch.elexis.hl7.v22;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractPrimitive;
import ca.uhn.hl7v2.model.GenericPrimitive;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v22.datatype.CE;
import ca.uhn.hl7v2.model.v22.datatype.FT;
import ca.uhn.hl7v2.model.v22.datatype.ST;
import ca.uhn.hl7v2.model.v22.datatype.TX;
import ca.uhn.hl7v2.model.v22.message.ORU_R01;
import ca.uhn.hl7v2.model.v22.segment.NTE;
import ca.uhn.hl7v2.model.v22.segment.OBR;
import ca.uhn.hl7v2.model.v22.segment.OBX;
import ca.uhn.hl7v2.model.v22.segment.PID;
import ca.uhn.hl7v2.model.v26.datatype.ED;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.hl7.HL7Writer;
import ch.elexis.hl7.data.HL7Mandant;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.StringData;
import ch.elexis.hl7.model.TextData;
import ch.elexis.hl7.v26.ElexisValidation;
import ch.elexis.hl7.v26.HL7Constants;
import ch.elexis.hl7.v26.Messages;

/**
 * Parser for HL7 ORU_R01 messages
 */
/**
 * @author tony
 * 
 */
public class HL7_ORU_R01 extends HL7Writer {
	
	final String uniqueMessageControlID;
	final String uniqueProcessingID;
	final HL7Mandant mandant;
	
	/**
	 * Default constructor
	 */
	public HL7_ORU_R01(){
		super();
		this.uniqueMessageControlID = null;
		this.uniqueProcessingID = null;
		this.mandant = null;
	}
	
	/**
	 * Constructs a new message
	 * 
	 * @param sendingApplication1
	 *            Value for MSH3.1
	 * @param sendingApplication3
	 *            Value for MSH3.3
	 * @param receivingApplication1
	 *            Value for MSH5.1
	 * @param receivingApplication3
	 *            Value for MSH5.3
	 * @param receivingFacility
	 *            Value for MSH6.1
	 * @param uniqueMessageControlID
	 *            Value for MSH10
	 * @param uniqueProcessingID
	 *            Value for MSH11
	 * @param mandant
	 *            Value for MSH4
	 */
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
	 * @param readWithValidation
	 *            True for parsing with validation, False for parsing without validation
	 * @return The ORU_R01 message
	 * @throws HL7Exception
	 */
	public ORU_R01 read(String text, boolean readWithValidation) throws HL7Exception{
		Parser p = new PipeParser();
		if (readWithValidation) {
			p.setValidationContext(new ElexisValidation());
		} else {
			p.setValidationContext(new NoValidation());
		}
		Message hl7Msg = p.parse(text);
		if (hl7Msg instanceof ORU_R01) {
			return (ORU_R01) hl7Msg;
		} else {
			addError(
				MessageFormat.format(Messages.HL7_ORU_R01_Error_WrongMsgType, hl7Msg.getName()));
		}
		return null;
	}
	
	/**
	 * Reads an observation from the ORU_R01 HL7 message
	 * 
	 * @param text
	 *            ISO-8559-1 String
	 * @return The observation
	 * @throws IOException
	 * @throws EncodingNotSupportedException
	 * @throws HL7Exception
	 * @throws ParseException
	 * @throws ElexisException
	 */
	public ObservationMessage readObservation(final String text)
		throws IOException, ElexisException{
		return readObservation(text, true);
	}
	
	/**
	 * Reads an observation from the ORU_R01 HL7 message
	 * 
	 * @param text
	 *            ISO-8559-1 String
	 * @return The observation
	 * @throws IOException
	 * @throws ElexisException
	 * @throws EncodingNotSupportedException
	 * @throws HL7Exception
	 * @throws ParseException
	 */
	public ObservationMessage readObservation(final String text, boolean readWithValidation)
		throws IOException, ElexisException{
		clearMessages();
		ObservationMessage observation = null;
		
		try {
			ORU_R01 oru = read(text, readWithValidation);
			
			if (oru != null) {
				String msh3_sendingApplication =
					oru.getMSH().getMsh3_SendingApplication().getValue();
				String msh4_sendingFacility = oru.getMSH().getMsh4_SendingFacility().getValue();
				String msh7_dateTimeOfMessage =
					oru.getMSH().getMsh7_DateTimeOfMessage().getComponent(0).toString();
				String msh10_messageControlId = oru.getMSH().getMsh10_MessageControlID().getValue();
				
				PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
				String pid2_patientId =
					pid.getPid2_PatientIDExternalID().getCk1_IDNumber().getValue();
				if ((pid2_patientId == null) || ("".equals(pid2_patientId)))
					pid2_patientId =
						pid.getPid3_PatientIDInternalID(0).getCm_pat_id1_IDNumber().getValue();
				String pid4_alternatePatientId = pid.getPid4_AlternatePatientID().getValue();
				String pid5_patientLastName = "";
				String pid5_patientFirstName = "";
				if (pid.getPid5_PatientName().getName() != null)
					pid5_patientLastName = pid.getPid5_PatientName().getFamilyName().getValue();
				if (pid.getPid5_PatientName().getFamilyName() != null)
					pid5_patientFirstName = pid.getPid5_PatientName().getGivenName().getValue();
				String pid7_patientBirthdate = pid.getPid7_DateOfBirth().getComponent(0).toString();
				String pid8_patientSex = pid.getPid8_Sex().getValue();
				FT[] nteAfterPid_patientNotesAndCommentsArray =
					oru.getPATIENT_RESULT().getPATIENT().getNTE().getComment();
				String nteAfterPid_patientNotesAndComments =
					String.join("\n", nteAfterPid_patientNotesAndCommentsArray[0].getValue());
				if ("W".equals(pid8_patientSex.toUpperCase()))
					pid8_patientSex = "F";
				String orc2_placerOrderNumber = oru.getPATIENT_RESULT().getORDER_OBSERVATION()
					.getORC().getOrc2_PlacerOrderNumber().getCm_placer1_UniquePlacerId().getValue();
				String orc3_fillerOrderNumber = oru.getPATIENT_RESULT().getORDER_OBSERVATION()
					.getORC().getOrc3_FillerOrderNumber().getCm_filler1_UniqueFillerId().getValue();
				String orc9_dateTimeOfTransaction = oru.getPATIENT_RESULT().getORDER_OBSERVATION()
					.getORC().getOrc9_DateTimeOfTransaction().getComponent(0).toString();
				observation = new ObservationMessage(msh3_sendingApplication, msh4_sendingFacility,
					msh7_dateTimeOfMessage, msh10_messageControlId, orc9_dateTimeOfTransaction,
					pid2_patientId, pid5_patientLastName, pid5_patientFirstName,
					nteAfterPid_patientNotesAndComments, pid7_patientBirthdate, pid8_patientSex,
					pid4_alternatePatientId, orc2_placerOrderNumber, orc3_fillerOrderNumber);
				
				int obscount = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
				for (int j = 0; j < obscount; j++) {
					String appendedTX = ""; //$NON-NLS-1$
					OBR obr = oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getOBR();
					String obrDateOfObservation =
						obr.getObr7_ObservationDateTime().getComponent(0).toString();
					
					if ((obrDateOfObservation == null) || ("".equals(obrDateOfObservation)))
						obrDateOfObservation = obr.getObr22_ResultsReportStatusChangeDateTime()
							.getComponent(0).toString();
					
					if ((obrDateOfObservation == null) || ("".equals(obrDateOfObservation)))
						obrDateOfObservation = orc9_dateTimeOfTransaction;
					
					if ((obrDateOfObservation == null) || ("".equals(obrDateOfObservation)))
						obrDateOfObservation = msh7_dateTimeOfMessage;
					
					// Order Comments
					String orderCommentNTE = null;
					for (int n = 0; n < oru.getPATIENT_RESULT().getORDER_OBSERVATION(j)
						.getNTEReps(); n++) {
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
						TextData txtData = new TextData(HL7Constants.COMMENT_NAME, orderCommentNTE,
							obrDateOfObservation, HL7Constants.COMMENT_GROUP, null);
						observation.add(txtData);
					}
					
					for (int i = 0; i < oru.getPATIENT_RESULT().getORDER_OBSERVATION(j)
						.getOBSERVATIONReps(); i++) {
						// Notes and Comments (NTE)
						String commentNTE = null;
						for (int n = 0; n < oru.getPATIENT_RESULT().getORDER_OBSERVATION(j)
							.getOBSERVATION(i).getNTEReps(); n++) {
							NTE nte = oru.getPATIENT_RESULT().getORDER_OBSERVATION(j)
								.getOBSERVATION(i).getNTE(n);
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
						
						// Resultate
						OBX obx = oru.getPATIENT_RESULT().getORDER_OBSERVATION(j).getOBSERVATION(i)
							.getOBX();
						String valueType = obx.getObx2_ValueType().getValue();
						if (HL7Constants.OBX_VALUE_TYPE_ED.equals(valueType)) {
							String observationId =
								obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
							if (!"DOCUMENT".equals(observationId)) { //$NON-NLS-1$
								addWarning(MessageFormat.format(
									Messages.HL7_ORU_R01_Error_WrongObsIdentifier, observationId));
							}
							ED ed = (ED) obx.getObx5_ObservationValue().getData();
							String filename = ed.getEd3_DataSubtype().getValue();
							String kuerzel = filename;
							String encoding = ed.getEd4_Encoding().getValue();
							String data = ed.getEd5_Data().getValue();
							String dateOfObservation =
								obx.getObx14_DateTimeOfTheObservation().getComponent(0).toString();
							EncapsulatedData encapsData = new EncapsulatedData(filename, encoding,
								data, dateOfObservation, commentNTE, null, null);
							observation.add(encapsData);
						} else if (HL7Constants.OBX_VALUE_TYPE_ST.equals(valueType)) {
							String kuerzel =
								obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
							String name =
								obx.getObx3_ObservationIdentifier().getCe2_Text().getValue();
							
							String valueST = ""; //$NON-NLS-1$
							Object value = obx.getObx5_ObservationValue().getData();
							if (value instanceof ST) {
								valueST =
									((ST) obx.getObx5_ObservationValue().getData()).getValue();
							}
							String unit = obx.getObx6_Units().getCe1_Identifier().getValue();
							String range = obx.getObx7_ReferencesRange().getValue();
							String dateOfObservation =
								obx.getObx14_DateTimeOfTheObservation().getComponent(0).toString();
							StringData strData = new StringData(name, unit, valueST, range,
								dateOfObservation, commentNTE, null, null);
							observation.add(strData);
						} else if (HL7Constants.OBX_VALUE_TYPE_TX.equals(valueType)) {
							String valueTX = ""; //$NON-NLS-1$
							Object value = obx.getObx5_ObservationValue().getData();
							if (value instanceof TX) {
								valueTX =
									((TX) obx.getObx5_ObservationValue().getData()).getValue();
							}
							appendedTX += valueTX + "\n"; //$NON-NLS-1$
						} else if (HL7Constants.OBX_VALUE_TYPE_FT.equals(valueType)) {
							String kuerzel =
								obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
							String name =
								obx.getObx3_ObservationIdentifier().getCe2_Text().getValue();
							
							String valueST = ""; //$NON-NLS-1$
							Object value = obx.getObx5_ObservationValue().getData();
							if (value instanceof ST) {
								ST st = (ST) value;
								valueST = st.getValue();
							} else if (value instanceof FT) {
								FT ft = (FT) value;
								valueST = ft.getValue();
							} else if (value instanceof GenericPrimitive) {
								GenericPrimitive gp = (GenericPrimitive) value;
								valueST = gp.getValue();
							} else {
								addError(MessageFormat.format(
									"Value type of FT ({0}) is not implemented!", //$NON-NLS-1$
									value.getClass().getName()));
								
							}
							String unit = obx.getObx6_Units().getCe1_Identifier().getValue();
							String range = obx.getObx7_ReferencesRange().getValue();
							String dateOfObservation =
								obx.getObx14_DateTimeOfTheObservation().getComponent(0).toString();
							StringData data = new StringData(name, unit, valueST, range,
								dateOfObservation, commentNTE, null, null);
							observation.add(data);
						} else {
							addError(MessageFormat.format("Value type {0} is not implemented!", //$NON-NLS-1$
								valueType));
						}
					}
					
					if (appendedTX.length() > 0) {
						// Find name in CE: <Identifier>^<Text>. <br>
						// <Text> if exists else use <Identifier>
						String name = null;
						CE ceIdentifier = obr.getObr4_UniversalServiceID();
						String kuerzel = ceIdentifier.getCe1_Identifier().getValue();
						if (ceIdentifier.getCe2_Text() != null) {
							name = ceIdentifier.getCe2_Text().getValue();
						}
						if (name == null || name.trim().length() == 0) {
							name = kuerzel;
						}
						TextData textData =
							new TextData(name, appendedTX, obrDateOfObservation, null, null);
						observation.add(textData);
					}
				}
			}
		} catch (HL7Exception | ParseException e) {
			throw new ElexisException(e.getMessage(), e);
		}
		return observation;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.hl7.HL7Parser#getVersion()
	 */
	@Override
	public String getVersion(){
		return "2.2";
	}
}
