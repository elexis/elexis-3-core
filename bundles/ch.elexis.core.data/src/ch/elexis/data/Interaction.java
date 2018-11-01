package ch.elexis.data;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import au.com.bytecode.opencsv.CSVReader;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.VersionInfo;

/**
 * Local cache for medical interactions defined by epha.ch.
 * https://download.epha.ch/data/matrix/matrix.csv
 * 
 * TODO: Daily update
 * 
 * TODO: Progress info while loading CSV
 * 
 * TODO: Download from URL to CoreHubHelper.getWritableUserDir aka $HOME/elexis
 * 
 * @author Niklaus Giger
 *
 */
public class Interaction extends PersistentObject {
	
	private static Logger logger = LoggerFactory.getLogger(Interaction.class);
	
	public static final String TABLENAME = "ch_elexis_interaction"; //$NON-NLS-1$
	public static final String VERSION = "1.0.0"; //$NON-NLS-1$
	
	public static final String VERSIONID = "VERSION"; //$NON-NLS-1$
	
	// Cache for https://download.epha.ch/data/matrix/matrix.csv
	//"ATC1","Name1","ATC2","Name2","Info","Mechanismus","Effekt","Massnahmen","Grad"
	
	public static final String FLD_ATC1 = "ATC1"; //$NON-NLS-1$
	public static final String FLD_ATC2 = "ATC2"; //$NON-NLS-1$
	public static final String FLD_NAME1 = "NAME1"; //$NON-NLS-1$
	public static final String FLD_NAME2 = "NAME2"; //$NON-NLS-1$
	public static final String FLD_INFO = "INFO"; //$NON-NLS-1$
	public static final String FLD_MECHANISM = "MECHANISM"; //$NON-NLS-1$
	public static final String FLD_EFFECT = "EFFECT"; //$NON-NLS-1$
	public static final String FLD_MEASURES = "MEASURES"; //$NON-NLS-1$
	public static final String FLD_SEVERITY = "SEVERITY"; //$NON-NLS-1$
	
	// @formatter:off
	// From https://raw.githubusercontent.com/zdavatz/oddb.org/master/src/model/epha_interaction.rb
	public static final Map<String, String> Ratings =
		ImmutableMap.of(
			"A", "Keine Massnahmen erforderlich",
			"B", "Vorsichtsmassnahmen empfohlen",
			"C", "Regelmässige Überwachung",
			"D", "Kombination vermeiden",
			"X", "Kontraindiziert");

	// using the same color like https://raw.githubusercontent.com/zdavatz/AmiKo-Windows/master/css/interactions_css.css
	public static final Map<String, String> Colors = ImmutableMap.of(
		"A", "caff70",
		"B", "ffec8b",
		"C", "ffb90f",
		"D", "ff82ab",
		"X", "ff6a6a");
	// @formatter:on
	
	private static int notImported = 0;
	private static int importerInteractionsCreated = 0;
	
	// @formatter:off
	static final String create = 
			"CREATE TABLE " + TABLENAME + " (" + //$NON-NLS-1$ //$NON-NLS-2$
			"ID VARCHAR(25) primary key, " + //$NON-NLS-1$
			"lastupdate BIGINT," + //$NON-NLS-1$
			"deleted CHAR(1) default '0'," + //$NON-NLS-1$
			"atc1 VARCHAR(8)," + //$NON-NLS-1$
			"name1 VARCHAR(128)," + //$NON-NLS-1$
			"atc2 VARCHAR(8)," + //$NON-NLS-1$
			"name2 VARCHAR(128)," + //$NON-NLS-1$
			"info VARCHAR(255)," + //$NON-NLS-1$
			"mechanism VARCHAR(255)," + //$NON-NLS-1$
			"effect VARCHAR(255)," + //$NON-NLS-1$
			"measures VARCHAR(255)," + //$NON-NLS-1$
			"severity CHAR(1)" + //$NON-NLS-1$
			");" + //$NON-NLS-1$
			"CREATE UNIQUE INDEX atc1_atc2 ON " + TABLENAME + " (" + FLD_ATC1 + "," + FLD_ATC2 +");" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$			
			"INSERT INTO " + TABLENAME + " (ID," + FLD_NAME1 + ") VALUES (" + JdbcLink.wrap(VERSIONID) + "," + JdbcLink.wrap(VERSION) + ");"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	// @formatter:on
	
	static {
		addMapping(TABLENAME, FLD_ATC1, FLD_NAME1, FLD_ATC2, FLD_NAME2, FLD_INFO, FLD_MECHANISM,
			FLD_EFFECT, FLD_MEASURES, FLD_SEVERITY);
		
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(create);
		} else {
			Interaction version = load(VERSIONID);
			VersionInfo vi = new VersionInfo(version.get(FLD_NAME1));
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				version.set(FLD_NAME1, VERSION);
			}
		}
		importMappingFromCsv();
	}
	
	/**
	 * Empty creator
	 */
	public Interaction(){
		/* leer */
	}
	
	/**
	 * create a new interaction
	 * 
	 * @param atc1
	 * @param name1
	 * @param atc2
	 * @param name2
	 * @param mechanism
	 * @param effect
	 * @param measures
	 * @param severity
	 */
	Interaction(final String atc1, final String name1, final String atc2, final String name2,
		final String info, final String mechanism, final String effect, final String measures,
		String severity){
		create(null);
		severity = severity.trim();
		// "ATC1","Name1","ATC2","Name2","Info","Mechanismus","Effekt","Massnahmen","Grad"
		
		if (severity.length() > 1) {
			log.warn("Unable to import {} {} {} {} severity wrong {} {}", atc1, name1, atc2, name2,
				severity, severity.length());
			severity = "D";
		}
		if (!set(new String[] {
			FLD_ATC1, FLD_NAME1, FLD_ATC2, FLD_NAME2, FLD_INFO, FLD_MECHANISM, FLD_EFFECT,
			FLD_MEASURES, FLD_SEVERITY
		}, atc1, name1, atc2, name2, limit(info, 255), limit(mechanism, 255), limit(effect, 255),
			limit(measures, 255), severity)) {
			log.warn("Unable to import {} {} {} {} {}", atc1, name1, atc2, name2, severity);
		}
		
	}
	
	private static String limit(String aString, int maxLength){
		if (aString.length() > maxLength) {
			return aString.substring(0, maxLength);
		}
		return aString;
	}
	
	protected Interaction(final String id){
		super(id);
	}
	
	public static Interaction load(final String id){
		return new Interaction(id);
	}
	
	public static String getMeasures(String ATC_1, String ATC_2){
		Interaction ia = getByATC(ATC_1, ATC_2);
		return ia == null ? " " : ia.get(FLD_MEASURES);
	}
	
	public static String getEffects(String ATC_1, String ATC_2){
		Interaction ia = getByATC(ATC_1, ATC_2);
		return ia == null ? " " : ia.get(FLD_EFFECT);
	}
	
	public static String getSeverity(String ATC_1, String ATC_2){
		Interaction ia = getByATC(ATC_1, ATC_2);
		return ia == null ? " " : ia.get(FLD_SEVERITY);
	}
	
	@Override
	public String getLabel(){
		return String.format("%s %s - %s %s: %s", get(FLD_ATC1), get(FLD_ATC2), get(FLD_NAME1),
			get(FLD_NAME2), get(FLD_SEVERITY));
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Interaction getByContactAndItemName(String contactId, String itemName){
		Query<Interaction> qbe = new Query<Interaction>(Interaction.class);
		qbe.add("ID", Query.NOT_EQUAL, VERSIONID); //$NON-NLS-1$
		qbe.add(FLD_ATC1, Query.EQUALS, contactId);
		qbe.add(FLD_ATC2, Query.EQUALS, itemName);
		List<Interaction> res = qbe.execute();
		if (res.isEmpty()) {
			return null;
		} else {
			if (res.size() > 1) {
				throw new IllegalArgumentException(String.format(
					"Found more then 1 mapping for origin id [%s] - [%s]", contactId, itemName)); //$NON-NLS-1$
			}
			return res.get(0);
		}
	}
	
	public static Interaction getByATC(String atc1, String atc2){
		Query<Interaction> qbe = new Query<Interaction>(Interaction.class);
		qbe.add("ID", Query.NOT_EQUAL, VERSIONID); //$NON-NLS-1$
		qbe.add(FLD_ATC1, Query.EQUALS, atc1);
		qbe.add(FLD_ATC2, Query.EQUALS, atc2);
		List<Interaction> res = qbe.execute();
		if (res.isEmpty()) {
			return null;
		} else {
			if (res.size() > 1) {
				logger.warn(String.format("Found [%s] mappings for ATC [%s] and [%s]", res.size(),
					atc1, atc2));
				logger.info(
					String.format("Using mapping with item name [%s]", res.get(0).getLabel()));
			}
			return res.get(0);
		}
	}
	
	/**
	 * Create or update the mapping table. The CSV format is as follows <br\>
	 * 
	 * <pre>
	 * ATC1,Name1,ATC2,Name2,Info,Mechanismus,Effekt,Massnahmen,Grad
	 * </pre>
	 * 
	 * @param csv
	 */
	private static void importMappingFromCsv(){
		// Use year/day of year
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.D");
		LocalDateTime now = LocalDateTime.now();
		String today = dtf.format(now);
		double curYearDay = Float.parseFloat(today);
		
		Interaction version = load(VERSIONID);
		String lastParsed = version.get(FLD_NAME2);
		double last = lastParsed.length() == 0 ? 0.0 : Float.parseFloat(lastParsed);
		if (!(lastParsed.length() == 0) && (curYearDay - last) < 1.0) {
			logger.info("Skip importMappingFromCsv as last '{}' equals today {}", //$NON-NLS-1$				
				lastParsed, today);
			return;
		} else {
			logger.info("Starting importMappingFromCsv as last '{}' smaller today {}", //$NON-NLS-1$				
				lastParsed, today);
		}
		
		File file = new File(CoreHub.getWritableUserDir(), "matrix.csv");
		getDefaultConnection().exec("DELETE FROM " + TABLENAME + " WHERE ID != 'VERSION';");
		notImported = 0;
		importerInteractionsCreated = 0;
		try {
			CSVReader cr = new CSVReader(new FileReader(file), ',', '"');
			String[] line;
			while ((line = cr.readNext()) != null) {
				if (line.length == 9) {
					if (line[0].equalsIgnoreCase("ATC1")) { //$NON-NLS-1$
						// skip description line
						continue;
					}
					new Interaction(line[0], line[1], line[2], line[3], line[4], line[5], line[6],
						line[7], line[8]);
					importerInteractionsCreated++;
				} else {
					notImported++;
					logger.info(String.format("Skipping [%s] ", line.toString())); //$NON-NLS-1$
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			logger.info(String.format("Import aborted after %d interactions with %d failures ", //$NON-NLS-1$
				importerInteractionsCreated, notImported));
			return;
		}
		getDefaultConnection().exec(
			"UPDATE " + TABLENAME + " SET " + FLD_NAME2 + " = " + today + " WHERE ID = 'VERSION';");
		logger.info("Imported {} interactions setting NAME2 to ", importerInteractionsCreated, //$NON-NLS-1$
			today); //$
	}
}
