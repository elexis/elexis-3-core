package ch.elexis.core.constants;

import ch.elexis.core.eenv.IElexisEnvironmentService;

public class ElexisSystemPropertyConstants {

	public static final boolean VERBOSE_ACL_NOTIFICATION = true;

	public static final String LOGBACK_CONFIG_FILE = "logback.configurationFile";

	public static final String CLIENT_EMAIL = "ch.elexis.clientEmail";

	// Login related properties
	public static final String LOGIN_USERNAME = "ch.elexis.username";
	public static final String LOGIN_PASSWORD = "ch.elexis.password";

	// Database connection related properties (also considered for demo db)
	public static final String CONN_DB_USERNAME = "ch.elexis.dbUser";
	public static final String CONN_DB_PASSWORD = "ch.elexis.dbPw";
	public static final String CONN_DB_FLAVOR = "ch.elexis.dbFlavor";
	@Deprecated
	public static final String CONN_DB_H2_AUTO_SERVER = "ch.elexis.dbH2AutoServer";
	public static final String CONN_DB_SPEC = "ch.elexis.dbSpec";

	// skip liquibase initialization and update
	public static final String CONN_SKIP_LIQUIBASE = "skip.liquibase";

	// Demo database related properties
	public static final String DEMO_DB_LOCATION = "demo.database.location";

	// Run-mode related properties
	public static final String RUN_MODE = "elexis-run-mode";

	// open db selection wizard on startup
	public static final String OPEN_DB_WIZARD = "openDBWizard";

	/**
	 * If the property elexis-run-mode is set to RunFromScratch then the connected
	 * database will be wiped out and initialized with default values for the
	 * mandant (007, topsecret). For mysql and postgresql this will only work if the
	 * database is empty! Therefore you mus call something like
	 * StringUtils.EMPTYdrop database miniDB; create dabase miniDB;" before starting
	 * Elexis.
	 */
	public static final String RUN_MODE_FROM_SCRATCH = "RunFromScratch";

	/**
	 * REST URL of the elexis server, e.g. http://localhost:8380/services" or, to
	 * administratively disconnect, set <code>disconnected</code>. If none set, but
	 * {@link IElexisEnvironmentService} is available, will connect against EE.
	 */
	public static final String ELEXIS_SERVER_REST_INTERFACE_URL = "elexisServerUrl";

	/**
	 * Elexis-Environment: Entry hostname of the elexis environment. If passed as
	 * system property, overrides both environment variable and Config stored value.
	 */
	public static final String EE_HOSTNAME = "EE_HOSTNAME";
}
