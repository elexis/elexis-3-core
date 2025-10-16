package ch.elexis.hl7.v2x.kikons;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v251.message.ORU_R01;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.model.OrcMessage;
import ch.elexis.hl7.model.TextData;

/**
 * HL7 importer for AI consultation reports (Consult Notes).
 * <p>
 * This reader extracts text observations (LOINC 11488-4 "Consult note") from
 * ORU_R01 messages and converts them into {@link TextData} objects.
 * 
 * @author Dalibor Aksic
 */
public class HL7ImporterKIKonsReader extends HL7Reader {

	private static final Logger logger = LoggerFactory.getLogger(HL7ImporterKIKonsReader.class);

	public HL7ImporterKIKonsReader(Message message) {
		super(message);
	}

	/**
	 * Reads the sender information from the MSH segment.
	 *
	 * @return the sending application and facility as a single string
	 */
	@Override
	public String getSender() {
		try {
			if (message instanceof ca.uhn.hl7v2.model.v251.message.ORU_R01 oru251) {
				var msh = oru251.getMSH();
				String sender = msh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
				String facility = msh.getMsh4_SendingFacility().getHd1_NamespaceID().getValue();
				return (sender != null ? sender : StringUtils.EMPTY) + StringUtils.SPACE
						+ (facility != null ? facility : StringUtils.EMPTY);
			} else if (message instanceof ca.uhn.hl7v2.model.v25.message.ORU_R01 oru25) {
				var msh = oru25.getMSH();
				String sender = msh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
				String facility = msh.getMsh4_SendingFacility().getHd1_NamespaceID().getValue();
				return (sender != null ? sender : StringUtils.EMPTY) + StringUtils.SPACE
						+ (facility != null ? facility : StringUtils.EMPTY);
			} else {
				return "Unknown sender"; //$NON-NLS-1$
			}
		} catch (Exception e) {
			logger.error("Error reading sender information from MSH segment", e); //$NON-NLS-1$
			return "Unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * Reads the consultation text (LOINC 11488-4) from the HL7 message.
	 *
	 * @param resolver         optional patient resolver (unused here)
	 * @param createIfNotFound unused
	 * @return an {@link ObservationMessage} containing consultation text
	 * @throws ElexisException if parsing fails
	 */
	@Override
	public ObservationMessage readObservation(ch.elexis.hl7.HL7PatientResolver resolver, boolean createIfNotFound)
			throws ElexisException {
		observation = null;
		try {
			ca.uhn.hl7v2.model.v25.message.ORU_R01 oru;
			try {
				oru = (ca.uhn.hl7v2.model.v25.message.ORU_R01) message;
			} catch (ClassCastException e) {
				if (message instanceof ca.uhn.hl7v2.model.v251.message.ORU_R01 oru251) {
					return readObservation251(oru251);
				}
				throw new ElexisException("Unsupported HL7 message version: " + message.getVersion(), e); //$NON-NLS-1$
			}

			ORU_R01_PATIENT_RESULT pr = oru.getPATIENT_RESULT();
			if (pr == null) {
				logger.warn("No PATIENT_RESULT segment found"); //$NON-NLS-1$
				return null;
			}

			ORU_R01_ORDER_OBSERVATION orderObs = pr.getORDER_OBSERVATION();
			if (orderObs == null) {
				logger.warn("No ORDER_OBSERVATION segment found"); //$NON-NLS-1$
				return null;
			}

			observation = new ObservationMessage("AI Importer", getSender(), null, null, null, null, null, null); //$NON-NLS-1$

			for (int i = 0; i < orderObs.getOBSERVATIONReps(); i++) {
				ORU_R01_OBSERVATION obs = orderObs.getOBSERVATION(i);
				OBX obx = obs.getOBX();
				if (obx == null)
					continue;

				String loinc = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
				String valueType = obx.getObx2_ValueType().getValue();

				if ("11488-4".equals(loinc) && "TX".equalsIgnoreCase(valueType)) { //$NON-NLS-1$ //$NON-NLS-2$
					String rawText = obx.getObx5_ObservationValue(0).getData().toString();
					if (StringUtils.isNotBlank(rawText)) {
						String text = rawText.replace("\\X0A\\", "\n").replace("\\X0D\\", "\r").replace("\\F\\", "|") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
								.replace("\\T\\", "&").replace("\\S\\", "^"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						observation.add(new TextData("Consult Note", text, null, "AI Consultation", null)); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			return observation;

		} catch (Exception e) {
			logger.error("Error while parsing AI consultation (HL7 v2.5)", e); //$NON-NLS-1$
			throw new ElexisException("Error while reading AI consultation", e); //$NON-NLS-1$
		}
	}

	/**
	 * AI consultation messages usually do not contain an ORC segment.
	 *
	 * @return always null
	 */
	@Override
	public OrcMessage getOrcMessage() {
		return null;
	}

	/**
	 * Reads the consultation text from HL7 v2.5.1 messages.
	 */
	private ObservationMessage readObservation251(ORU_R01 oru) throws ElexisException {
		try {
			var pr = oru.getPATIENT_RESULT();
			if (pr == null) {
				logger.warn("No PATIENT_RESULT segment found (v2.5.1)"); //$NON-NLS-1$
				return null;
			}

			var orderObs = pr.getORDER_OBSERVATION();
			if (orderObs == null) {
				logger.warn("No ORDER_OBSERVATION segment found (v2.5.1)"); //$NON-NLS-1$
				return null;
			}

			String konsId = null;
			try {
				var pv1 = pr.getPATIENT().getVISIT().getPV1();
				if (pv1 != null && pv1.getPv119_VisitNumber().getCx1_IDNumber().getValue() != null) {
					konsId = pv1.getPv119_VisitNumber().getCx1_IDNumber().getValue();
				}
			} catch (Exception e) {
				logger.warn("No consultation ID found in PV1-19", e); //$NON-NLS-1$
			}

			observation = new ObservationMessage("AI Importer", getSender(), null, null, null, null, null, konsId //$NON-NLS-1$
			);

			for (int i = 0; i < orderObs.getOBSERVATIONReps(); i++) {
				var obs = orderObs.getOBSERVATION(i);
				var obx = obs.getOBX();
				if (obx == null)
					continue;

				String loinc = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
				String valueType = obx.getObx2_ValueType().getValue();

				if ("11488-4".equals(loinc) && "TX".equalsIgnoreCase(valueType)) { //$NON-NLS-1$ //$NON-NLS-2$
					String rawText = obx.getObx5_ObservationValue(0).getData().toString();
					if (StringUtils.isNotBlank(rawText)) {
						String text = rawText.replace("\\X0A\\", "\n").replace("\\X0D\\", "\r").replace("\\F\\", "|") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
								.replace("\\T\\", "&").replace("\\S\\", "^"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						observation.add(new TextData("Consult Note", text, null, "AI Consultation", null)); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			return observation;

		} catch (Exception e) {
			logger.error("Error while parsing AI consultation (HL7 v2.5.1)", e); //$NON-NLS-1$
			throw new ElexisException("Error while reading AI consultation (HL7 v2.5.1)", e); //$NON-NLS-1$
		}
	}
}
