package ch.elexis.hl7;

import org.apache.commons.lang3.StringUtils;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v26.segment.ORC;
import ca.uhn.hl7v2.validation.impl.WithdrawnDatatypeRule;
import ch.elexis.hl7.data.HL7Kontakt;
import ch.elexis.hl7.data.HL7Mandant;
import ch.elexis.hl7.data.HL7Patient;
import ch.elexis.hl7.util.HL7Helper;

public abstract class HL7Writer {
	List<String> errorList = new Vector<>();
	List<String> warnList = new Vector<>();

	protected String sendingApplication1 = StringUtils.EMPTY;
	protected String sendingApplication3 = StringUtils.EMPTY;
	protected String receivingApplication1 = StringUtils.EMPTY;
	protected String receivingApplication3 = StringUtils.EMPTY;
	protected String receivingFacility = StringUtils.EMPTY;

	static {
		System.setProperty(WithdrawnDatatypeRule.PROP_DISABLE_RULE, "true");
	}

	public HL7Writer() {
		super();
	}

	public HL7Writer(final String sendingApplication1, final String sendingApplication3,
			final String receivingApplication1, final String receivingApplication3, final String receivingFacility) {
		this();
		this.sendingApplication1 = sendingApplication1;
		this.sendingApplication3 = sendingApplication3;
		this.receivingApplication1 = receivingApplication1;
		this.receivingApplication3 = receivingApplication3;
		this.receivingFacility = receivingFacility;
	}

	/**
	 * Returns version of HL7
	 *
	 * @return
	 */
	public abstract String getVersion();

	/**
	 * Clears all errors and warnings
	 */
	public void clearMessages() {
		errorList = new Vector<>();
		warnList = new Vector<>();
	}

	/**
	 * Returns error list
	 *
	 * @return
	 */
	public List<String> getErrorList() {
		return errorList;
	}

	/**
	 * Returns warning list
	 *
	 * @return
	 */
	public List<String> getWarningList() {
		return warnList;
	}

	/**
	 * Adds parsing error
	 *
	 * @param error
	 */
	protected void addError(String error) {
		errorList.add(error);
	}

	/**
	 * Adds a warning message
	 *
	 * @param error
	 */
	protected void addWarning(String warn) {
		errorList.add(warn);
	}

	/**
	 * Fills MSH segment
	 *
	 * @param msh
	 * @param patient
	 */
	protected void fillMSH(final ca.uhn.hl7v2.model.v26.segment.MSH msh, final String messageId, final String event,
			final HL7Mandant mandant, final String uniqueMessageControlID, final String uniqueProcessingID,
			final HL7Patient patient) throws DataTypeException {
		msh.getMsh1_FieldSeparator().setValue("|"); //$NON-NLS-1$
		msh.getMsh2_EncodingCharacters().setValue("^~\\&"); //$NON-NLS-1$
		// Name der sendenden Anwendung. Dessen Eindeutigkeit im Kommunikations-Netzwerk
		// liegt
		// in der Verantwortung des jeweiligen Systemadministrators. Nimm diesen Text:
		// CHELEXIS
		msh.getMsh3_SendingApplication().getHd1_NamespaceID().setValue(this.sendingApplication1); // $NON-NLS-1$
		if (this.sendingApplication3 != null) {
			msh.getMsh3_SendingApplication().getHd2_UniversalID().setValue(StringUtils.EMPTY);
			msh.getMsh3_SendingApplication().getHd3_UniversalIDType().setValue(this.sendingApplication3); // $NON-NLS-1$
		}
		// Name der sendenden Institution. Optional (Beschreibung gemäss HL7 Standard).
		// Gemäss HD Type Definition von HL7 folgendermassen:
		// <mandantenkürzel>^<EAN des Mandanten>^L Beispiel: mf7601234567890^L
		msh.getMsh4_SendingFacility().getHd1_NamespaceID().setValue(mandant.getLabel());
		msh.getMsh4_SendingFacility().getHd2_UniversalID().setValue(mandant.getEan());
		msh.getMsh4_SendingFacility().getHd3_UniversalIDType().setValue("L"); //$NON-NLS-1$
		// Name der empfangenden Anwendung. Eindeutigkeit dito MSH.3
		// MSH-5: IMED
		msh.getMsh5_ReceivingApplication().getHd1_NamespaceID().setValue(this.receivingApplication1);
		if (this.receivingApplication3 != null) {
			msh.getMsh5_ReceivingApplication().getHd2_UniversalID().setValue(StringUtils.EMPTY);
			msh.getMsh5_ReceivingApplication().getHd3_UniversalIDType().setValue(this.receivingApplication3); // $NON-NLS-1$
		}
		// Name der empfangenden Institution . Optional (Beschreibung gemäss HL7
		// Standard).
		// Vergleiche auch MSH.5
		// MSH-6: PRAXIS
		msh.getMsh6_ReceivingFacility().getHd1_NamespaceID().setValue(this.receivingFacility);
		msh.getMsh7_DateTimeOfMessage().setValue(HL7Helper.dateToString(new Date()));
		msh.getMsh8_Security().setValue(StringUtils.EMPTY);
		msh.getMsh9_MessageType().getMessageCode().setValue(messageId);
		msh.getMsh9_MessageType().getTriggerEvent().setValue(event);
		msh.getMsh9_MessageType().getMessageStructure().setValue(StringUtils.EMPTY);
		// Eindeutige Nachrichtennummer: GUID
		if (uniqueMessageControlID != null) {
			msh.getMsh10_MessageControlID().setValue(uniqueMessageControlID);
		}
		if (uniqueProcessingID != null) {
			msh.getMsh11_ProcessingID().getPt1_ProcessingID().setValue(uniqueProcessingID);
		}
		msh.getMsh12_VersionID().getVid1_VersionID().setValue(getVersion());
	}

	/**
	 * Fills MSH segment
	 *
	 * @param msh
	 * @param patient
	 */
	protected void fillMSH(final ca.uhn.hl7v2.model.v231.segment.MSH msh, final String messageId, final String event,
			final HL7Mandant mandant, final String uniqueMessageControlID, final String encoding,
			final HL7Patient patient) throws DataTypeException {
		msh.getMsh1_FieldSeparator().setValue("|"); //$NON-NLS-1$
		msh.getMsh2_EncodingCharacters().setValue("^~\\&"); //$NON-NLS-1$
		// Name der sendenden Anwendung. Dessen Eindeutigkeit im Kommunikations-Netzwerk
		// liegt
		// in der Verantwortung des jeweiligen Systemadministrators. Nimm diesen Text:
		// CHELEXIS
		msh.getMsh3_SendingApplication().getHd1_NamespaceID().setValue(this.sendingApplication1); // $NON-NLS-1$
		if (this.sendingApplication3 != null) {
			msh.getMsh3_SendingApplication().getHd2_UniversalID().setValue(StringUtils.EMPTY);
			msh.getMsh3_SendingApplication().getHd3_UniversalIDType().setValue(this.sendingApplication3); // $NON-NLS-1$
		}
		// Name der sendenden Institution. Optional (Beschreibung gemäss HL7 Standard).
		// Gemäss HD Type Definition von HL7 folgendermassen:
		// <mandantenkürzel>^<EAN des Mandanten>^L Beispiel: mf7601234567890^L
		msh.getMsh4_SendingFacility().getHd1_NamespaceID().setValue(mandant.getLabel());
		msh.getMsh4_SendingFacility().getHd2_UniversalID().setValue(mandant.getEan());
		msh.getMsh4_SendingFacility().getHd3_UniversalIDType().setValue("L"); //$NON-NLS-1$
		// Name der empfangenden Anwendung. Eindeutigkeit dito MSH.3
		// MSH-5: IMED
		msh.getMsh5_ReceivingApplication().getHd1_NamespaceID().setValue(this.receivingApplication1);
		if (this.receivingApplication3 != null) {
			msh.getMsh5_ReceivingApplication().getHd2_UniversalID().setValue(StringUtils.EMPTY);
			msh.getMsh5_ReceivingApplication().getHd3_UniversalIDType().setValue(this.receivingApplication3); // $NON-NLS-1$
		}
		// Name der empfangenden Institution . Optional (Beschreibung gemäss HL7
		// Standard).
		// Vergleiche auch MSH.5
		// MSH-6: PRAXIS
		msh.getMsh6_ReceivingFacility().getHd1_NamespaceID().setValue(this.receivingFacility);
		msh.getMsh7_DateTimeOfMessage().getTs1_TimeOfAnEvent().setValue(HL7Helper.dateToString(new Date()));
		msh.getMsh8_Security().setValue(StringUtils.EMPTY);
		msh.getMsh9_MessageType().getMessageType().setValue(messageId);
		msh.getMsh9_MessageType().getTriggerEvent().setValue(event);
		msh.getMsh9_MessageType().getMessageStructure().setValue(StringUtils.EMPTY);
		// Eindeutige Nachrichtennummer: GUID
		if (uniqueMessageControlID != null) {
			msh.getMsh10_MessageControlID().setValue(uniqueMessageControlID);
		}
		msh.getMsh11_ProcessingID().getPt1_ProcessingID().setValue("P");
		msh.getMsh12_VersionID().getVid1_VersionID().setValue(getVersion());
		msh.getMsh18_CharacterSet(0).setValue(encoding);
	}

	/**
	 * Fills PID segment
	 *
	 * @param pid
	 * @param patient
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	protected void fillPID(final ca.uhn.hl7v2.model.v26.segment.PID pid, final HL7Patient patient)
			throws DataTypeException, HL7Exception {
		String sex = StringUtils.EMPTY;
		if (patient.isMale() != null) {
			sex = "M"; //$NON-NLS-1$
			if (!patient.isMale().booleanValue()) {
				sex = "F"; //$NON-NLS-1$
			}
		}
		pid.getPid1_SetIDPID().setValue("1"); //$NON-NLS-1$
		pid.getPid2_PatientID().getIDNumber().setValue(patient.getPatCode());
		pid.getPid3_PatientIdentifierList(0).getIDNumber().setValue(patient.getPatCode());
		pid.getPid4_AlternatePatientIDPID(0).getIDNumber().setValue(patient.getPatCode());
		addKontaktToXPN(pid.getPid5_PatientName(0), patient);
		pid.getPid16_MaritalStatus().getCwe1_Identifier().setValue(StringUtils.EMPTY);
		pid.getPid7_DateTimeOfBirth().setValue(HL7Helper.dateToString(patient.getBirthdate()));

		pid.getPid8_AdministrativeSex().setValue(sex);
		pid.getPid9_PatientAlias(0).getXpn1_FamilyName().getFn1_Surname().setValue(StringUtils.EMPTY);
		pid.getPid10_Race(0).getCwe1_Identifier().setValue(StringUtils.EMPTY);
		addAddressToXAD(pid.getPid11_PatientAddress(0), patient);
		pid.getPid12_CountyCode().setValue(StringUtils.EMPTY);
		addPhone1ToXTN(pid.getPid13_PhoneNumberHome(0), patient);
		addPhone2ToXTN(pid.getPid14_PhoneNumberBusiness(0), patient);
	}

	/**
	 * Fills PID segment
	 *
	 * @param pid
	 * @param patient
	 * @throws DataTypeException
	 * @throws HL7Exception
	 */
	protected void fillPID(final ca.uhn.hl7v2.model.v231.segment.PID pid, final HL7Patient patient)
			throws DataTypeException, HL7Exception {
		String sex = StringUtils.EMPTY;
		if (patient.isMale() != null) {
			sex = "M"; //$NON-NLS-1$
			if (!patient.isMale().booleanValue()) {
				sex = "F"; //$NON-NLS-1$
			}
		}
		pid.getPid1_SetIDPID().setValue("1"); //$NON-NLS-1$
		pid.getPid2_PatientID().getID().setValue(patient.getPatCode());
		pid.getPid3_PatientIdentifierList(0).getID().setValue(patient.getPatCode());
		pid.getPid4_AlternatePatientIDPID(0).getID().setValue(patient.getPatCode());
		addKontaktToXPN(pid.getPid5_PatientName(0), patient);
		pid.getPid16_MaritalStatus().getCe1_Identifier().setValue(StringUtils.EMPTY);
		pid.getPid7_DateTimeOfBirth().getTs1_TimeOfAnEvent().setValue(HL7Helper.dateToString(patient.getBirthdate()));

		pid.getPid8_Sex().setValue(sex);
		pid.getPid9_PatientAlias(0).getXpn1_FamilyLastName().getFn1_FamilyName().setValue(StringUtils.EMPTY);
		pid.getPid10_Race(0).getCe1_Identifier().setValue(StringUtils.EMPTY);
		addAddressToXAD(pid.getPid11_PatientAddress(0), patient);
		pid.getPid12_CountyCode().setValue(StringUtils.EMPTY);
		addPhone1ToXTN(pid.getPid13_PhoneNumberHome(0), patient);
	}

	/**
	 * Fills ORC segment
	 *
	 * @param orc
	 * @param orderControl (ORC-1)
	 * @param orderNumber  (ORC-2)
	 * @throws DataTypeException
	 */
	protected void fillORC(final ORC orc, final String orderControl, final Long orderNumber) throws DataTypeException {
		orc.getOrc1_OrderControl().setValue(orderControl);
		if (orderNumber != null) {
			orc.getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().setValue(orderNumber.toString());
		}
	}

	/**
	 * Adds patient data to XPN segment
	 *
	 * @param xpn
	 * @param patient
	 * @throws DataTypeException
	 */
	protected void addKontaktToXPN(ca.uhn.hl7v2.model.v26.datatype.XPN xpn, final HL7Kontakt kontakt)
			throws DataTypeException {
		String name = StringUtils.EMPTY;
		String vorname = StringUtils.EMPTY;
		String title = StringUtils.EMPTY;
		if (kontakt != null) {
			name = kontakt.getName();
			vorname = kontakt.getFirstname();
			title = kontakt.getTitle();
		}
		xpn.getXpn1_FamilyName().getSurname().setValue(name);
		xpn.getXpn2_GivenName().setValue(vorname);
		xpn.getXpn3_SecondAndFurtherGivenNamesOrInitialsThereof().setValue(StringUtils.EMPTY);
		xpn.getXpn4_SuffixEgJRorIII().setValue(StringUtils.EMPTY);
		xpn.getXpn5_PrefixEgDR().setValue(StringUtils.EMPTY);
		xpn.getXpn6_DegreeEgMD().setValue(title);
		xpn.getXpn7_NameTypeCode().setValue(StringUtils.EMPTY);
		xpn.getXpn8_NameRepresentationCode().setValue(StringUtils.EMPTY);
		xpn.getXpn9_NameContext().getCwe1_Identifier().setValue(StringUtils.EMPTY);
	}

	/**
	 * Adds patient data to XPN segment
	 *
	 * @param xpn
	 * @param patient
	 * @throws DataTypeException
	 */
	protected void addKontaktToXPN(ca.uhn.hl7v2.model.v231.datatype.XPN xpn, final HL7Kontakt kontakt)
			throws DataTypeException {
		String name = StringUtils.EMPTY;
		String vorname = StringUtils.EMPTY;
		String title = StringUtils.EMPTY;
		if (kontakt != null) {
			name = kontakt.getName();
			vorname = kontakt.getFirstname();
			title = kontakt.getTitle();
		}
		xpn.getXpn1_FamilyLastName().getFamilyName().setValue(name);
		xpn.getXpn2_GivenName().setValue(vorname);
		xpn.getXpn4_SuffixEgJRorIII().setValue(StringUtils.EMPTY);
		xpn.getXpn5_PrefixEgDR().setValue(StringUtils.EMPTY);
		xpn.getXpn6_DegreeEgMD().setValue(title);
		xpn.getXpn7_NameTypeCode().setValue(StringUtils.EMPTY);
		xpn.getXpn8_NameRepresentationCode().setValue(StringUtils.EMPTY);
	}

	/**
	 * Adds address of kontakt to XAD segment
	 *
	 * @param xad
	 * @param kontakt
	 * @throws DataTypeException
	 */
	protected void addAddressToXAD(ca.uhn.hl7v2.model.v26.datatype.XAD xad, final HL7Kontakt kontakt)
			throws DataTypeException {
		String street = StringUtils.EMPTY;
		String other = StringUtils.EMPTY;
		String city = StringUtils.EMPTY;
		String zip = StringUtils.EMPTY;
		String country = StringUtils.EMPTY;
		if (kontakt != null) {
			street = kontakt.getAddress1();
			other = kontakt.getAddress2();
			city = kontakt.getCity();
			zip = kontakt.getZip();
			country = kontakt.getCountry();
		}
		xad.getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue(street);
		xad.getXad2_OtherDesignation().setValue(other);
		xad.getXad3_City().setValue(city);
		xad.getXad4_StateOrProvince().setValue(StringUtils.EMPTY);
		xad.getXad5_ZipOrPostalCode().setValue(zip);
		xad.getXad6_Country().setValue(country);
	}

	/**
	 * Adds address of kontakt to XAD segment
	 *
	 * @param xad
	 * @param kontakt
	 * @throws DataTypeException
	 */
	protected void addAddressToXAD(ca.uhn.hl7v2.model.v231.datatype.XAD xad, final HL7Kontakt kontakt)
			throws DataTypeException {
		String street = StringUtils.EMPTY;
		String other = StringUtils.EMPTY;
		String city = StringUtils.EMPTY;
		String zip = StringUtils.EMPTY;
		String country = StringUtils.EMPTY;
		if (kontakt != null) {
			street = kontakt.getAddress1();
			other = kontakt.getAddress2();
			city = kontakt.getCity();
			zip = kontakt.getZip();
			country = kontakt.getCountry();
		}
		xad.getXad1_StreetAddress().setValue(street);
		xad.getXad2_OtherDesignation().setValue(other);
		xad.getXad3_City().setValue(city);
		xad.getXad4_StateOrProvince().setValue(StringUtils.EMPTY);
		xad.getXad5_ZipOrPostalCode().setValue(zip);
		xad.getXad6_Country().setValue(country);
	}

	/**
	 * Adds contact informations to XTN segment
	 *
	 * @param xtn
	 * @param kontakt
	 * @throws DataTypeException
	 */
	protected void addPhone1ToXTN(ca.uhn.hl7v2.model.v26.datatype.XTN xtn, final HL7Kontakt kontakt)
			throws DataTypeException {
		String phone1 = StringUtils.EMPTY;
		String email = StringUtils.EMPTY;
		String fax = StringUtils.EMPTY;
		if (kontakt != null) {
			phone1 = kontakt.getPhone1();
			email = kontakt.getEmail();
			fax = kontakt.getFax();
		}
		if (phone1 != null && !phone1.isEmpty()) {
			xtn.getXtn2_TelecommunicationUseCode().setValue("PRN"); //$NON-NLS-1$
			xtn.getXtn3_TelecommunicationEquipmentType().setValue("PH"); //$NON-NLS-1$
			xtn.getXtn4_CommunicationAddress().setValue(phone1);
		} else if (email != null && !email.isEmpty()) {
			xtn.getXtn2_TelecommunicationUseCode().setValue("PRN"); //$NON-NLS-1$
			xtn.getXtn3_TelecommunicationEquipmentType().setValue("Internet"); //$NON-NLS-1$
			xtn.getXtn4_CommunicationAddress().setValue(email);
		}
		xtn.getXtn5_CountryCode().setValue(StringUtils.EMPTY);
		xtn.getXtn6_AreaCityCode().setValue(StringUtils.EMPTY);
		xtn.getXtn7_LocalNumber().setValue(StringUtils.EMPTY);
		xtn.getXtn8_Extension().setValue(StringUtils.EMPTY);
		xtn.getXtn9_AnyText().setValue(StringUtils.EMPTY);
		xtn.getXtn10_ExtensionPrefix().setValue(StringUtils.EMPTY);
		xtn.getXtn11_SpeedDialCode().setValue(fax);
	}

	/**
	 * Adds contact informations to XTN segment
	 *
	 * @param xtn
	 * @param kontakt
	 * @throws DataTypeException
	 */
	protected void addPhone1ToXTN(ca.uhn.hl7v2.model.v231.datatype.XTN xtn, final HL7Kontakt kontakt)
			throws DataTypeException {
		String phone1 = StringUtils.EMPTY;
		String email = StringUtils.EMPTY;
		String fax = StringUtils.EMPTY;
		if (kontakt != null) {
			phone1 = kontakt.getPhone1();
			email = kontakt.getEmail();
			fax = kontakt.getFax();
		}
		if (phone1 != null) {
			phone1 = phone1.replaceAll("[^\\d.]", StringUtils.EMPTY);
			xtn.getPhoneNumber().setValue(phone1);
		}
		if (email != null) {
			xtn.getEmailAddress().setValue(email);
		}
		xtn.getXtn2_TelecommunicationUseCode().setValue(StringUtils.EMPTY);
		xtn.getXtn3_TelecommunicationEquipmentType().setValue(StringUtils.EMPTY);
		xtn.getXtn5_CountryCode().setValue(StringUtils.EMPTY);
		xtn.getXtn6_AreaCityCode().setValue(StringUtils.EMPTY);
		xtn.getXtn8_Extension().setValue(StringUtils.EMPTY);
		xtn.getXtn9_AnyText().setValue(StringUtils.EMPTY);
	}

	/**
	 * Adds contact informations to XTN segment
	 *
	 * @param xtn
	 * @param kontakt
	 * @throws DataTypeException
	 */
	protected void addPhone2ToXTN(ca.uhn.hl7v2.model.v26.datatype.XTN xtn, final HL7Kontakt kontakt)
			throws DataTypeException {
		String phone2 = StringUtils.EMPTY;
		if (kontakt != null) {
			phone2 = kontakt.getPhone2();
		}
		if (phone2 != null && !phone2.isEmpty()) {
			xtn.getXtn2_TelecommunicationUseCode().setValue("WPN"); //$NON-NLS-1$
			xtn.getXtn3_TelecommunicationEquipmentType().setValue("PH"); //$NON-NLS-1$
			xtn.getXtn4_CommunicationAddress().setValue(phone2);
		}
	}
}
