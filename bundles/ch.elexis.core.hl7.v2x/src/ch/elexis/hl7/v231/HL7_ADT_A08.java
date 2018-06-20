package ch.elexis.hl7.v231;

import java.util.Date;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v231.message.ADT_A08;
import ca.uhn.hl7v2.model.v231.segment.EVN;
import ca.uhn.hl7v2.model.v231.segment.PV1;
import ca.uhn.hl7v2.parser.Parser;
import ch.elexis.hl7.HL7Writer;
import ch.elexis.hl7.data.HL7Konsultation;
import ch.elexis.hl7.data.HL7Mandant;
import ch.elexis.hl7.data.HL7Patient;
import ch.elexis.hl7.util.HL7Helper;

public class HL7_ADT_A08 extends HL7Writer {
	
	final String uniqueMessageControlID;
	final String uniqueProcessingID;
	final HL7Mandant mandant;
	
	public HL7_ADT_A08(final String sendingApplication1, final String sendingApplication3,
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
	 * Creates an ORU_R01 message
	 * 
	 * @param patient
	 * @param consultation
	 * @param labItem
	 * @param labwert
	 * 
	 * @return
	 */
	public String createText(final HL7Patient patient, HL7Konsultation consultation)
		throws DataTypeException, HL7Exception{
		
		ADT_A08 adt = new ADT_A08();
		// Message
		fillMSH(adt.getMSH(), "ADT", "A08", mandant, this.uniqueMessageControlID, //$NON-NLS-1$ //$NON-NLS-2$
			"8859/1", patient); //$NON-NLS-1$ //$NON-NLS-2$
		
		fillEVN(adt.getEVN());
		
		// Patient
		fillPID(adt.getPID(), patient);
		
		// Patient Visit
		fillPV1(adt.getPV1(), consultation);
		
		// Now, let's encode the message and look at the output
		HapiContext context = new DefaultHapiContext();
		Parser parser = context.getPipeParser();
		return parser.encode(adt);
	}
	
	private void fillEVN(EVN evn) throws DataTypeException{
		evn.getEvn1_EventTypeCode().setValue("A08");
		evn.getEvn2_RecordedDateTime().getTs1_TimeOfAnEvent()
			.setValue(HL7Helper.dateToString(new Date()));
	}
	
	private void fillPV1(PV1 pv1, HL7Konsultation consultation) throws DataTypeException{
		pv1.getAdmitDateTime().getTs1_TimeOfAnEvent()
			.setValue(HL7Helper.dateToString(consultation.getZeitpunkt()));
		pv1.getVisitNumber().getCx1_ID().setValue(consultation.getId());
		pv1.getAlternateVisitID().getCx1_ID().setValue(consultation.getId());
	}
	
	@Override
	public String getVersion(){
		return "2.3.1";
	}
}
