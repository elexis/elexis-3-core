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
	
	// open db selection wizard on startup
	public static final String OPEN_DB_WIZARD = "openDBWizard";
	
	// The Elexis run mode - from scratch
	public static final String RUN_MODE_FROM_SCRATCH = "RunFromScratch";
	
	/**
	 * Allows bypassing the ErsterMandantDialog on startup. Intended for GUI tests,
	 * and automated setup of an initial DB for a new practice
	 * @since 3.8
	 */
	public static final String FIRST_MANDANT_NAME = "ch.elexis.firstMandantName"; //$NON-NLS-1$
	/**
	 * Allows bypassing the ErsterMandantDialog on startup. Intended for GUI tests,
	 * and automated setup of an initial DB for a new practice
	 * @since 3.8
	 */
	public static final String FIRST_MANDANT_EMAIL = "ch.elexis.firstMandantEmail"; //$NON-NLS-1$
	/**
	 * Allows bypassing the ErsterMandantDialog on startup. Intended for GUI tests,
	 * and automated setup of an initial DB for a new practice
	 * @since 3.8
	 */
	public static final String FIRST_MANDANT_PASSWORD = "ch.elexis.firstMandantPassword"; //$NON-NLS-1$

	/**
	 * REST URL of the elexis server, e.g. http://localhost:8380/services"
	 */
	public static final String ELEXIS_SERVER_REST_INTERFACE_URL = "elexisServerUrl";
}
