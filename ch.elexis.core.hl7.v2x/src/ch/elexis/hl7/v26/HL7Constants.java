package ch.elexis.hl7.v26;

public interface HL7Constants {
	
	/**
	 * OBX VALUE TYPES <br>
	 * AD Address <br>
	 * CE Code Element <br>
	 * CWE Coded Entry <br>
	 * CF Coded Element With Formatted Values <br>
	 * CK Composite ID With Check Digit <br>
	 * CN Composite ID And Name <br>
	 * CP Composite Price <br>
	 * CX Extended Composite ID With Check Digit <br>
	 * DT Date <br>
	 * ED Encapsulated Data <br>
	 * FT Formatted Text (Display) <br>
	 * MO Money <br>
	 * NM Numeric <br>
	 * PN Person Name <br>
	 * RP Reference Pointer <br>
	 * SN Structured Numeric <br>
	 * ST String Data. <br>
	 * TM Time <br>
	 * TN Telephone Number <br>
	 * TS Time Stamp (Date & Time) <br>
	 * TX Text Data (Display) <br>
	 * XAD Extended Address <br>
	 * XCN Extended Composite Name And Number For Persons <br>
	 * XON Extended Composite Name And Number For Organizations <br>
	 * XPN Extended Person Number <br>
	 * XTN Extended Telecommunications Number <br>
	 */
	
	/**
	 * Encapsulated Data
	 */
	public static String OBX_VALUE_TYPE_ED = "ED"; //$NON-NLS-1$
	
	/**
	 * String Data
	 */
	public static String OBX_VALUE_TYPE_ST = "ST"; //$NON-NLS-1$
	
	/**
	 * Text Data
	 */
	public static String OBX_VALUE_TYPE_TX = "TX"; //$NON-NLS-1$
	
	/**
	 * Coded Entry
	 */
	public static String OBX_VALUE_TYPE_CWE = "CWE"; //$NON-NLS-1$
	
	/**
	 * Numeric Data
	 */
	public static String OBX_VALUE_TYPE_NM = "NM"; //$NON-NLS-1$
	
	/**
	 * Formatted Text
	 */
	public static String OBX_VALUE_TYPE_FT = "FT"; //$NON-NLS-1$
	
	/**
	 * Structured Numeric
	 */
	public static String OBX_VALUE_TYPE_SN = "SN";
	
	/**
	 * Code Element
	 */
	public static String OBX_VALUE_TYPE_CE = "CE";
	
	/**
	 * Comment Definitions
	 */
	public static String COMMENT_NAME = "Kommentar";
	public static String COMMENT_GROUP = "00 Kommentar";
	public static String COMMENT_CODE = "kommentar";
	
	/**
	 * OBX OBSERVATION STATUS <br>
	 * C Record coming over is a correction and thus replaces a final result <br>
	 * D Deletes the OBX record <br>
	 * F Final results; Can only be changed with a corrected result. <br>
	 * I Specimen in lab; results pending <br>
	 * P Preliminary results <br>
	 * R Results entered -- not verified <br>
	 * S Partial results <br>
	 * X Results cannot be obtained for this observation <br>
	 * U Results status change to Final. without retransmitting results already sent as
	 * ‘preliminary.’ E.g., radiology changes status from preliminary to final <br>
	 * W Post original as wrong, e.g., transmitted for wrong patient <br>
	 */
	
	/**
	 * OBX ABNORMAL FLAGS <br>
	 * L Below low normal <br>
	 * H Above high normal <br>
	 * LL Below lower panic limits <br>
	 * HH Above upper panic limits <br>
	 * < Below absolute low-off instrument scale <br>
	 * > Above absolute high-off instrument scale <br>
	 * N Normal (applies to non-numeric results) <br>
	 * A Abnormal (applies to non-numeric results) <br>
	 * AA Very abnormal (applies to non-numeric units, analogous to panic limits for numeric units) <br>
	 * null No range defined, or normal ranges don't apply <br>
	 * U Significant change up <br>
	 * D Significant change down <br>
	 * B Better--use when direction not relevant <br>
	 * W Worse--use when direction not relevant <br>
	 */
	
	/**
	 * OBX OBSERVATION STATUS <br>
	 * C Record coming over is a correction and thus replaces a final result <br>
	 * D Deletes the OBX record <br>
	 * F Final results; Can only be changed with a corrected result. <br>
	 * I Specimen in lab; results pending <br>
	 * P Preliminary results <br>
	 * R Results entered -- not verified <br>
	 * S Partial results <br>
	 * X Results cannot be obtained for this observation <br>
	 * U Results status change to Final. without retransmitting results already sent as
	 * ‘preliminary.’ E.g., radiology changes status from preliminary to final <br>
	 * W Post original as wrong, e.g., transmitted for wrong patient <br>
	 */
	
}
