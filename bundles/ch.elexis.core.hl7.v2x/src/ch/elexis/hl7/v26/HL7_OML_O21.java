package ch.elexis.hl7.v26;

import java.util.Date;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v26.datatype.CWE;
import ca.uhn.hl7v2.model.v26.message.OML_O21;
import ca.uhn.hl7v2.model.v26.segment.IN1;
import ca.uhn.hl7v2.model.v26.segment.NK1;
import ca.uhn.hl7v2.model.v26.segment.PV1;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ch.elexis.hl7.HL7Writer;
import ch.elexis.hl7.data.HL7Kostentraeger;
import ch.elexis.hl7.data.HL7Mandant;
import ch.elexis.hl7.data.HL7Patient;
import ch.elexis.hl7.util.HL7Helper;

import ch.rgw.tools.StringTool;
public class HL7_OML_O21 extends HL7Writer {
	
	final String uniqueMessageControlID;
	final String uniqueProcessingID;
	final HL7Mandant mandant;
	
	public HL7_OML_O21(final String sendingApplication1, final String sendingApplication3,
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
	 * Creates an OMG_O19 message
	 * 
	 * @param patient
	 * @param kostentraeger
	 * @param rechnungsempfaenger
	 * @param auftragsNummer
	 * @param plan
	 *            Abrechnungssystem (MV, UVG, VVG, KVG, usw)
	 * @param beginDate
	 * @param vnr
	 *            Versicherungs-, Fall- oder Unfallnr
	 * @return
	 */
	public String createText(final HL7Patient patient, final HL7Kostentraeger rechnungsempfaenger,
		final HL7Kostentraeger kostentraeger, final String plan, final Date beginDate,
		final String fallNr, final long auftragsNummer) throws DataTypeException, HL7Exception{
		
		OML_O21 omg = new OML_O21();
		fillMSH(omg.getMSH(), "OML", "O21", mandant, this.uniqueMessageControlID, //$NON-NLS-1$ //$NON-NLS-2$
			this.uniqueProcessingID, patient); //$NON-NLS-1$ //$NON-NLS-2$
		fillPID(omg.getPATIENT().getPID(), patient);
		fillNK1(omg.getPATIENT().getNK1(), rechnungsempfaenger);
		fillPV1(omg.getPATIENT().getPATIENT_VISIT().getPV1(), patient, beginDate);
		fillIN1(omg.getPATIENT().getINSURANCE().getIN1(), patient, kostentraeger, plan, fallNr);
		fillORC(omg.getORDER().getORC(), "1", auftragsNummer); //$NON-NLS-1$
		
		// Now, let's encode the message and look at the output
		Parser parser = new PipeParser();
		return parser.encode(omg);
	}
	
	@Override
	public String getVersion(){
		return "2.6"; //$NON-NLS-1$
	}
	
	/**
	 * Fills NK1 segment
	 * 
	 * @param nk1
	 * @param rechnungsempfaenger
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillNK1(final NK1 nk1, final HL7Kostentraeger rechnungsempfaenger)
		throws DataTypeException, HL7Exception{
		nk1.getNk11_SetIDNK1().setValue("1"); //$NON-NLS-1$
		addKontaktToXPN(nk1.getNk12_Name(0), rechnungsempfaenger);
		
		CWE cwe = nk1.getNk13_Relationship();
		cwe.getCwe1_Identifier().setValue(StringTool.leer); //$NON-NLS-1$
		cwe.getCwe2_Text().setValue("INVOICERECEIPT"); //$NON-NLS-1$
		
		addAddressToXAD(nk1.getNk14_Address(0), rechnungsempfaenger);
		addPhone1ToXTN(nk1.getNk15_PhoneNumber(0), rechnungsempfaenger);
		addPhone2ToXTN(nk1.getNk16_BusinessPhoneNumber(0), rechnungsempfaenger);
	}
	
	/**
	 * Fills PV1 segment
	 * 
	 * @param pv1
	 * @param patient
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillPV1(final PV1 pv1, final HL7Patient patient, final Date beginDate)
		throws DataTypeException, HL7Exception{
		pv1.getPv11_SetIDPV1().setValue("1"); //$NON-NLS-1$
		pv1.getPv12_PatientClass().setValue("O"); //$NON-NLS-1$
		
		// PLV-13: Aktueller Aufenthaltsort des Patienten, optional
		// Empfehlung: Wenn vorhanden, dann ausfüllen -> In unserem Fall leer lassen
		pv1.getPv14_AdmissionType().setValue(StringTool.leer); //$NON-NLS-1$
		pv1.getPv15_PreadmitNumber().getCx1_IDNumber().setValue(StringTool.leer); //$NON-NLS-1$
		pv1.getPv16_PriorPatientLocation().getPl1_PointOfCare().setValue(StringTool.leer); //$NON-NLS-1$
		
		// Fallnummer, optional (Beschreibung gemäss HL7 Standard)
		// Empfehlung: Wenn vorhanden, dann ausfüllen -> In unserem Fall leer lassen oder den Key
		// des Falles nehmen
		pv1.getPv119_VisitNumber().getIDNumber().setValue(StringTool.leer); //$NON-NLS-1$
		// ...
		pv1.getPv144_AdmitDateTime().setValue(HL7Helper.dateToString(beginDate));
	}
	
	/**
	 * Fills IN1 segment
	 * 
	 * @param in1
	 * @param patient
	 * @param kostentraeger
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	private void fillIN1(final IN1 in1, final HL7Patient patient,
		final HL7Kostentraeger kostentraeger, final String plan, final String fallNr)
		throws DataTypeException, HL7Exception{
		in1.getIn11_SetIDIN1().setValue("1"); //$NON-NLS-1$
		in1.getIn12_InsurancePlanID().getCwe1_Identifier().setValue(plan);
		// EAN Nummer der Versicherung
		// Beispiel: EAN123456789^^^CHEMEDIAT;
		in1.getIn13_InsuranceCompanyID(0).getCx1_IDNumber()
			.setValue("EAN" + kostentraeger.getEan()); //$NON-NLS-1$
		in1.getIn13_InsuranceCompanyID(0).getCx4_AssigningAuthority().getHd1_NamespaceID()
			.setValue("CHEMEDIAT"); //$NON-NLS-1$
		in1.getIn14_InsuranceCompanyName(0).getXon1_OrganizationName()
			.setValue(kostentraeger.getName());
		
		addAddressToXAD(in1.getIn15_InsuranceCompanyAddress(0), kostentraeger);
		addKontaktToXPN(in1.getIn116_NameOfInsured(0), patient);
		in1.getIn136_PolicyNumber().setValue(fallNr);
		
		addAddressToXAD(in1.getIn119_InsuredSAddress(0), patient);
	}
}
