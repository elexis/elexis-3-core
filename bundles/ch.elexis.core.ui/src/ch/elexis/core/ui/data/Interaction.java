package ch.elexis.core.ui.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import au.com.bytecode.opencsv.CSVReader;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.ElexisStatusProgressMonitor;
import ch.elexis.core.l10n.Messages;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
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
	public static final String FLD_ABUSE_ID_FOR_VERSION = FLD_NAME1;
	public static final String FLD_ABUSE_ID_FOR_LAST_PARSED = FLD_NAME2;
	public static final String FLD_ABUSE_ID_FOR_SHA = FLD_MECHANISM;
	
	// @formatter:off
	// From https://raw.githubusercontent.com/zdavatz/oddb.org/master/src/model/epha_interaction.rb
	public static final Map<String, String> Ratings =
		ImmutableMap.of(
			"A", Messages.Interaction_Class_A,
			"B", Messages.Interaction_Class_B,
			"C", Messages.Interaction_Class_C,
			"D", Messages.Interaction_Class_D,
			"X", Messages.Interaction_Class_X
);

	// using the same color like https://raw.githubusercontent.com/zdavatz/AmiKo-Windows/master/css/interactions_css.css
	public static final Map<String, String> Colors = ImmutableMap.of(
		"A", "caff70",	//$NON-NLS-1$ $NON-NLS-2$
		"B", "ffec8b",	//$NON-NLS-1$ $NON-NLS-2$
		"C", "ffb90f",	//$NON-NLS-1$ $NON-NLS-2$
		"D", "ff82ab",	//$NON-NLS-1$ $NON-NLS-2$
		"X", "ff6a6a");	//$NON-NLS-1$ $NON-NLS-2$
	// @formatter:on
	
	private static Logger logger = LoggerFactory.getLogger(Interaction.class);
	private static int notImported = 0;
	private static String hash_from_file = "";
	private static int importerInteractionsCreated = 0;
	private static final String MATRIX_CSV_URL = "https://download.epha.ch/data/matrix/matrix.csv"; //$NON-NLS-1$
	private static final File MATRIX_CSV_LOCAL =
		new File(CoreHub.getWritableUserDir(), "matrix.csv");//$NON-NLS-1$
	
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
			"INSERT INTO " + TABLENAME + " (ID," + FLD_ABUSE_ID_FOR_VERSION + ") VALUES (" + JdbcLink.wrap(VERSIONID) + "," + JdbcLink.wrap(VERSION) + ");"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
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
			log.warn("Unable to import {} {} {} {} severity wrong {} {}", atc1, name1, atc2, name2, //$NON-NLS-1$
				severity, severity.length());
			severity = "D";
		}
		if (!set(new String[] {
			FLD_ATC1, FLD_NAME1, FLD_ATC2, FLD_NAME2, FLD_INFO, FLD_MECHANISM, FLD_EFFECT,
			FLD_MEASURES, FLD_SEVERITY
		}, atc1, name1, atc2, name2, limit(info, 255), limit(mechanism, 255), limit(effect, 255),
			limit(measures, 255), severity)) {
			log.warn("Unable to import {} {} {} {} {}", atc1, name1, atc2, name2, severity); //$NON-NLS-1$
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
		return String.format("%s %s - %s %s: %s", get(FLD_ATC1), get(FLD_ATC2), get(FLD_NAME1), //$NON-NLS-1$
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
				logger.warn(String.format("Found [%s] mappings for ATC [%s] and [%s]", res.size(), //$NON-NLS-1$
					atc1, atc2));
				logger.info(
					String.format("Using mapping with item name [%s]", res.get(0).getLabel())); //$NON-NLS-1$
			}
			return res.get(0);
		}
	}
	
	private static void downloadMatrix(){
		try {
			logger.info("Start downloading {}", MATRIX_CSV_URL);
			FileUtils.copyURLToFile(new URL(MATRIX_CSV_URL), MATRIX_CSV_LOCAL);
			logger.info("Finished downloading to {}", MATRIX_CSV_LOCAL);
		} catch (IOException e) {
			logger.warn("Unable to download {} to {}: {}", MATRIX_CSV_URL, MATRIX_CSV_LOCAL,
				e.getMessage());
		}
	}
	
	private static boolean getShaFromFile(){
		Interaction version = load(VERSIONID);
		try {
			byte[] b = Files.readAllBytes(MATRIX_CSV_LOCAL.toPath());
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(b);
			// hash_from_file = Base64.getEncoder().encodeToString(digest);
			hash_from_file = javax.xml.bind.DatatypeConverter.printHexBinary(digest);
			String sha25_from_db = version.get(FLD_ABUSE_ID_FOR_SHA);
			logger.info("digest for  '{}' {} {}", //$NON-NLS-1$		
				MATRIX_CSV_LOCAL, sha25_from_db, hash_from_file);
			return (hash_from_file.equalsIgnoreCase(sha25_from_db));
		} catch (NoSuchAlgorithmException | IOException e) {
			logger.info("Error calculating digest for  '{}' {}", //$NON-NLS-1$		
				MATRIX_CSV_LOCAL, e.getMessage());
			return false;
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
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.D"); //$NON-NLS-1$
		LocalDateTime now = LocalDateTime.now();
		String today = dtf.format(now);
		double curYearDay = Float.parseFloat(today);
		
		Interaction version = load(VERSIONID);
		String lastParsed = version.get(FLD_ABUSE_ID_FOR_LAST_PARSED);
		double last = lastParsed.length() == 0 ? 0.0 : Float.parseFloat(lastParsed);
		if (!(lastParsed.length() == 0) && (curYearDay - last) < 1.0) {
			logger.info("Skip importMappingFromCsv as last '{}' equals today {}", //$NON-NLS-1$				
				lastParsed, today);
			return;
		} else {
			logger.info("Starting importMappingFromCsv as last '{}' smaller today {}", //$NON-NLS-1$				
				lastParsed, today);
		}
		
		getDefaultConnection().exec("DELETE FROM " + TABLENAME + " WHERE ID != 'VERSION';"); //$NON-NLS-1$ //$NON-NLS-2$
		notImported = 0;
		importerInteractionsCreated = 0;
		if (MATRIX_CSV_LOCAL.exists() && getShaFromFile()) {
			return;
		}
		IProgressService ipm = PlatformUI.getWorkbench().getProgressService();
		try {
			ipm.runInUI(PlatformUI.getWorkbench().getProgressService(),
				new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException{
						try {
							monitor.beginTask(String.format(
								Messages.VerrDetailDialog_DownloadInteractions, MATRIX_CSV_URL), 5); // average length of matrix_csv
							downloadMatrix();
							logger.info("Start importing interactions from {} ", //$NON-NLS-1$
								MATRIX_CSV_LOCAL);
							CSVReader cr =
								new CSVReader(new FileReader(MATRIX_CSV_LOCAL), ',', '"');
							String info =
								String.format(Messages.VerrDetailDialog_ImportInteractions,
									MATRIX_CSV_LOCAL.toString());
							monitor.beginTask(info,
								(int) (Files.size(MATRIX_CSV_LOCAL.toPath()) / 600)); // average length of matrix_csv
							ElexisEvent progress = new ElexisEvent(ipm,
								ElexisStatusProgressMonitor.class,
								ElexisEvent.EVENT_OPERATION_PROGRESS, ElexisEvent.PRIORITY_HIGH);
							ElexisEventDispatcher.getInstance().fire(progress);
							cr = new CSVReader(new FileReader(MATRIX_CSV_LOCAL), ',', '"');
							String[] line;
							while ((line = cr.readNext()) != null) {
								monitor.worked(1);
								if (line.length == 9) {
									if (line[0].equalsIgnoreCase("ATC1")) { //$NON-NLS-1$
										// skip description line
										continue;
									}
									new Interaction(line[0], line[1], line[2], line[3], line[4],
										line[5], line[6], line[7], line[8]);
									importerInteractionsCreated++;
								} else {
									notImported++;
									logger.info(String.format("Skipping [%s] ", line.toString())); //$NON-NLS-1$
								}
							}
						} catch (Exception ex) {
							ExHandler.handle(ex);
							logger.info(String.format(
								"Import aborted after %d interactions with %d failures ", //$NON-NLS-1$
								importerInteractionsCreated, notImported));
							if (ipm != null) {
								monitor.done();
							}
							return;
						}
						monitor.done();
					}
				}, null);
		} catch (Throwable ex) {
			ExHandler.handle(ex);
		}
		getShaFromFile();
		getDefaultConnection().exec(
			"UPDATE " + TABLENAME + " SET " + FLD_ABUSE_ID_FOR_LAST_PARSED + " = '" + today + "', " //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$
				+ FLD_ABUSE_ID_FOR_SHA + " = '" + hash_from_file + "' WHERE ID = 'VERSION';"); //$NON-NLS-1$ $NON-NLS-2$
		logger.info("Imported {} interactions setting date {} sha {}", importerInteractionsCreated, //$NON-NLS-1$
			today, hash_from_file); //$
	}
}
