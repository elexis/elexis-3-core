package ch.elexis.core.findings.fhir.po.codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.VersionInfo;

public class LocalCoding extends PersistentObject implements ICoding {
	
	protected static final String TABLENAME = "CH_ELEXIS_CORE_FINDINGS_LOCALCODING";
	protected static final String VERSION = "1.0.0";
	
	public static final String FLD_CODE = "code";
	public static final String FLD_DISPLAY = "display";
	private static final String FLD_MAPPED = "mapped";
	
	//@formatter:off
	protected static final String createDB =
	"CREATE TABLE " + TABLENAME + "(" +
	"ID					VARCHAR(25) PRIMARY KEY," +
	"lastupdate 		BIGINT," +
	"deleted			CHAR(1) default '0'," + 
	"code	            VARCHAR(25)," +
	"display      		TEXT," + 
	"mapped   	        TEXT" + ");" + 
	"CREATE INDEX CH_ELEXIS_CORE_FINDINGS_LOCALCODING_IDX1 ON " + TABLENAME + " (code);" +
	"INSERT INTO " + TABLENAME + " (ID, " + FLD_CODE + ") VALUES ('VERSION','" + VERSION + "');";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_ID, FLD_CODE, FLD_DISPLAY, FLD_MAPPED);
		
		LocalCoding version = load("VERSION");
		if (version.state() < PersistentObject.DELETED) {
			createOrModifyTable(createDB);
		} else {
			VersionInfo vi = new VersionInfo(version.get(FLD_CODE));
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				version.set(FLD_CODE, VERSION);
			}
		}
	}
	
	public static LocalCoding load(final String id){
		return new LocalCoding(id);
	}
	
	protected LocalCoding(final String id){
		super(id);
	}
	
	public LocalCoding(){
		// empty but necessary
	}
	
	public LocalCoding(ICoding coding){
		create(null);
		set(new String[] {
			FLD_CODE, FLD_DISPLAY
		}, coding.getCode(), coding.getDisplay());
	}
	
	@Override
	public String getSystem(){
		return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
	}
	
	@Override
	public String getCode(){
		return checkNull(get(FLD_CODE));
	}
	
	@Override
	public String getDisplay(){
		return checkNull(get(FLD_DISPLAY));
	}
	
	@Override
	public String getLabel(){
		return "[" + getCode() + "] " + getDisplay();
	}
	
	/**
	 * Get mapped codes. Codings are mapped if their meaning is the same.
	 * 
	 * @return
	 */
	public List<ICoding> getMappedCodes(){
		String mappedString = checkNull(get(FLD_MAPPED));
		if (!mappedString.isEmpty()) {
			return getMappedCodingFromString(mappedString);
		}
		return Collections.emptyList();
	}
	
	/**
	 * Set mapped codes. Codings are mapped if their meaning is the same.
	 * 
	 * @return
	 */
	public void setMappedCodes(List<ICoding> mappedCodes){
		String encoded = "";
		if (mappedCodes != null && !mappedCodes.isEmpty()) {
			encoded = getMappedCodingAsString(mappedCodes);
		}
		set(FLD_MAPPED, encoded);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	private static String MAPPED_SEPARATOR = "||";
	private static String MAPPED_SEPARATOR_SPLITTER = "\\|\\|";
	
	private static String MAPPED_FIELD_SEPARATOR = "^";
	
	private String getMappedCodingAsString(List<ICoding> mappedCoding){
		StringBuilder sb = new StringBuilder();
		for (ICoding iCoding : mappedCoding) {
			if (sb.length() > 0) {
				sb.append(MAPPED_SEPARATOR);
			}
			sb.append(getAsString(iCoding));
		}
		return sb.toString();
	}
	
	private String getAsString(ICoding coding){
		return coding.getSystem() + MAPPED_FIELD_SEPARATOR + coding.getCode()
			+ MAPPED_FIELD_SEPARATOR + coding.getDisplay();
	}
	
	private List<ICoding> getMappedCodingFromString(String encoded){
		String[] codeStrings = encoded.split(MAPPED_SEPARATOR_SPLITTER);
		if (codeStrings != null && codeStrings.length > 0) {
			List<ICoding> ret = new ArrayList<>();
			for (String string : codeStrings) {
				getCodingFromString(string).ifPresent(c -> ret.add(c));
			}
		}
		return Collections.emptyList();
	}
	
	private Optional<ICoding> getCodingFromString(String encoded){
		String[] codingParts = encoded.split(MAPPED_FIELD_SEPARATOR);
		if (codingParts != null && codingParts.length > 1) {
			if (codingParts.length == 2) {
				return Optional.of(new TransientCoding(codingParts[0], codingParts[1], ""));
			} else if (codingParts.length == 3) {
				return Optional
					.of(new TransientCoding(codingParts[0], codingParts[1], codingParts[2]));
			}
		}
		return Optional.empty();
	}
}
