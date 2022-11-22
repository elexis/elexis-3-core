package ch.elexis.core.model;

public class FallConstants {
	/**
	 * @deprecated - value moved to row BillingSystem
	 * @deprecated 3.6 - all extinfo values are moved to Fall#Gesetz
	 */
	public static final String FLD_EXTINFO_BILLING = "billing"; //$NON-NLS-1$

	public static final String FLD_EXT_NO_ELECTRONIC_DELIVERY = "NoElectronicDelivery"; //$NON-NLS-1$
	public static final String FLD_EXT_COPY_FOR_PATIENT = "CopyForPatient"; //$NON-NLS-1$
	public static final String FLD_EXT_KOSTENTRAEGER = "Kostenträger"; //$NON-NLS-1$
	public static final String FLD_EXT_RECHNUNGSEMPFAENGER = "Rechnungsempfänger"; //$NON-NLS-1$

	public static final String TYPE_DISEASE = Messages.Core_Illness; // $NON-NLS-1$
	public static final String TYPE_ACCIDENT = Messages.Core_Accident; // $NON-NLS-1$
	public static final String TYPE_MATERNITY = Messages.Fall_Maternity; // $NON-NLS-1$
	public static final String TYPE_PREVENTION = Messages.Fall_Prevention; // $NON-NLS-1$
	public static final String TYPE_BIRTHDEFECT = Messages.Fall_Birthdefect; // $NON-NLS-1$
	public static final String TYPE_OTHER = Messages.Core_Another;

	public static final String UVG_UNFALLNUMMER = "Unfallnummer";
	public static final String UVG_UNFALLDATUM = "Unfalldatum";
	public static final String IV_FALLNUMMER = "Fallnummer";

}
