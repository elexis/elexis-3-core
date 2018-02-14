package ch.elexis.core.constants;

public class XidConstants {
	
	/**
	 * Quality value for an ID that is valid only in the context of the issuing program
	 */
	public static final int ASSIGNMENT_LOCAL = 1;
	/**
	 * Quality value for an ID that is valid within a geographic or politic context (e.g. a
	 * nationally assigned ID)
	 */
	public static final int ASSIGNMENT_REGIONAL = 2;
	/**
	 * Quality value for an ID that can be used as global identifier
	 */
	public static final int ASSIGNMENT_GLOBAL = 3;
	
	/**
	 * Marker that the ID is a GUID (that is, guaranteed to exist only once through time and space)
	 */
	public static final int QUALITY_GUID = 4;
	
	/**
	 * ELEXIS INTERNAL domain
	 */
	public static final String ELEXIS = "www.elexis.ch/xid";
	public static final String DOMAIN_ELEXIS = ELEXIS;
	public static final int ELEXIS_QUALITY = ASSIGNMENT_LOCAL | QUALITY_GUID;
	
	/**
	 * Elexis INTERAL types
	 */
	public static final String DOMAIN_KONTAKT = XidConstants.DOMAIN_ELEXIS + "/kontakt/";
	public static final String XID_KONTAKT_ANREDE = DOMAIN_KONTAKT + "anrede";
	public static final String XID_KONTAKT_KANTON = DOMAIN_KONTAKT + "kanton";
	public static final String XID_KONTAKT_SPEZ = DOMAIN_KONTAKT + "spez";
	public static final String XID_KONTAKT_ROLLE = DOMAIN_KONTAKT + "rolle";
	public static final String XID_KONTAKT_LAB_SENDING_FACILITY = DOMAIN_KONTAKT
		+ "lab/sendingfacility";
	
	/**
	 * GLOBAL
	 */
	public static final String EAN = "www.xid.ch/id/ean"; // European Article Number
	public static final String DOMAIN_EAN = EAN;
	public static final int EAN_QUALITY = ASSIGNMENT_REGIONAL;
	public static final String DOMAIN_RECIPIENT_EAN = "www.xid.ch/id/recipient_ean";
	public static final String OID = "www.xid.ch/id/oid"; // Object Identifier
	public static final String DOMAIN_OID = OID;
	public static final int OID_QUALITY = ASSIGNMENT_GLOBAL | QUALITY_GUID;
	
	/**
	 * SWITZERLAND
	 */
	public static final String CH_PASSPORT = "www.xid.ch/id/passport/ch";
	public static final String DOMAIN_SWISS_PASSPORT = CH_PASSPORT;
	public static final int CH_PASSPORT_QUALITY = ASSIGNMENT_GLOBAL;
	public static final String CH_AHV = "www.ahv.ch/xid"; // Alters- und Hinterbliebenenversicherung
	public static final String DOMAIN_AHV = CH_AHV;
	public static final int CH_AHV_QUALITY = ASSIGNMENT_REGIONAL;
	
	/**
	 * AUSTRIA
	 */
	public static final String AT_PASSPORT = "www.xid.ch/id/passport/at";
	public static final String DOMAIN_AUSTRIAN_PASSPORT = AT_PASSPORT;
	public static final int AT_PASSPORT_QUALITY = ASSIGNMENT_GLOBAL;
	public static final String AT_SVNR = "www.sozialversicherung.at/svnr"; // Sozialversicherung
	public static final int AT_SVNR_QUALITY = ASSIGNMENT_REGIONAL;
	
	/**
	 * GERMANY
	 */
	public static final String DE_PASSPORT = "www.xid.ch/id/passport/de";
	public static final String DOMAIN_GERMAN_PASSPORT = DE_PASSPORT;
	public static final int DE_PASSPORT_QUALITY = ASSIGNMENT_GLOBAL;
}
