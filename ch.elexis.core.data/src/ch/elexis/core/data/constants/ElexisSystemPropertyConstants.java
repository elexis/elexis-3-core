package ch.elexis.core.data.constants;

public class ElexisSystemPropertyConstants {
	
	public static final String LOGBACK_CONFIG_FILE = "logback.configurationFile";
	
	public static final String CLIENT_EMAIL = "ch.elexis.clientEmail";
	
	// Login related properties
	public static final String LOGIN_USERNAME = "ch.elexis.username";
	public static final String LOGIN_PASSWORD = "ch.elexis.password";
	
	// Database connection related properties (also considered for demo db)
	public static final String CONN_DB_USERNAME = "ch.elexis.dbUser";
	public static final String CONN_DB_PASSWORD = "ch.elexis.dbPw";
	public static final String CONN_DB_FLAVOR = "ch.elexis.dbFlavor";
	public static final String CONN_DB_SPEC = "ch.elexis.dbSpec";
	
	// Demo database related properties
	public static final String DEMO_DB_LOCATION = "demo.database.location";
	
	// Run-mode related properties
	public static final String RUN_MODE = "elexis-run-mode";
	
	// The Elexis run mode - from scratch
	public static final String RUN_MODE_FROM_SCRATCH = "RunFromScratch";
	
}
