package ch.elexis.core.jpa.datasource.internal;

import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_DATABASE;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_HOST;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_JDBC_PARAMETER_STRING;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_PASSWORD;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_TYPE;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_USERNAME;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.utils.CoreUtil;

public class DataSourceConnectionParser {

	/**
	 * System property for adding ;AUTO_SERVER=TRUE to the h2 db connection string
	 */
	public static final String TEST_DBSERVER = "elexis.test.db.server";

	private int configSourceCode = -1;
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Try to find a valid existing database configuration, according to the given
	 * surrounding parameters. If none found (which is a valid scenario e.g. for
	 * Elexis RCP which reads from CoreHub.localCfg) return Optional empty
	 * 
	 * @return
	 */
	public Optional<DBConnection> parseAvailableParameters() {

		boolean isTestMode = CoreUtil.isTestMode();
		boolean isRunFromScratch = ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE));
		boolean traceActivated = Boolean.parseBoolean(System.getProperty("elexis.test.dbtrace"));
		boolean isServerMode = Boolean.parseBoolean(System.getProperty(TEST_DBSERVER));
		boolean isSkipDataSourceActivation = Boolean.parseBoolean(System.getProperty("elexis.skip.ds.activation"));

		if (isSkipDataSourceActivation) {
			// do not perform DataSource activation
			// used e.g. by tests who need to prepare a database first,
			// and will then care to call IElexisDataSourceService#setDBConnection
			return Optional.empty();
		}

		String prop_dbUser = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_USERNAME);
		String prop_dbPassword = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_PASSWORD);
		if (prop_dbPassword == null) {
			prop_dbPassword = "";
		}
		String prop_dbFlavor = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_FLAVOR);
		String prop_dbConnSpec = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_SPEC);
		if (StringUtils.isNotBlank(prop_dbConnSpec) && StringUtils.isBlank(prop_dbFlavor)) {
			prop_dbFlavor = determineFlavorFromJdbcString(prop_dbConnSpec);
		}

		File demoDBLocation = new File(CoreUtil.getWritableUserDir(), "demoDB");
		if (StringUtils.isNotBlank(System.getProperty(ElexisSystemPropertyConstants.DEMO_DB_LOCATION))) {
			demoDBLocation = new File(System.getProperty(ElexisSystemPropertyConstants.DEMO_DB_LOCATION));
		}

		Map<String, String> env = System.getenv();
		String env_dbType = env.get(DB_TYPE);
		String env_dbHost = env.get(DB_HOST);
		String env_dbDatabase = env.get(DB_DATABASE);
		String env_dbUsername = env.get(DB_USERNAME);
		String env_dbPassword = env.get(DB_PASSWORD);
		String env_jdbcParameterString = env.get(DB_JDBC_PARAMETER_STRING);

		// 1) isTestMode or (isRunFromScratch activated and no prop_* values set) ?
		// we use a fresh "in mem h2 db"
		if (isTestMode || (isRunFromScratch && StringUtils.isBlank(prop_dbConnSpec))) {
			String jdbcString = "jdbc:h2:mem:elexisFromScratch;DB_CLOSE_DELAY=-1";
			if (isServerMode) {
				jdbcString = "jdbc:h2:~/elexisTest/elexisTest;AUTO_SERVER=TRUE";
			}
			if (traceActivated) {
				jdbcString += ";TRACE_LEVEL_SYSTEM_OUT=2";
			}
			logger.info("Connecting to RunFromScratch H2 DB [" + jdbcString + "]");
			configSourceCode = 1;
			return Optional.of(new DBConnection(DBType.H2, jdbcString, "sa", new char[] {}));
		}

		// 2) are prop_* values set ?
		if (StringUtils.isNotBlank(prop_dbConnSpec)) {
			logger.info("Connecting to DB (Prop) [{}]", prop_dbConnSpec);
			DBType dbType = DBType.valueOfIgnoreCase(prop_dbFlavor)
					.orElseThrow(() -> new IllegalStateException("Unknown ch.elexis.dbFlavor"));
			if (isRunFromScratch) {
				// TODO checkIfEmpty throw IllegalState
				// PersistentObject#deleteAllTables
			}
			configSourceCode = 2;
			return Optional.of(new DBConnection(dbType, prop_dbConnSpec, prop_dbUser, prop_dbPassword.toCharArray()));
		}

		// 3) does a demoDB directory exist on the configured location ?
		if (demoDBLocation.exists() && demoDBLocation.isDirectory()) {
			logger.info("Connecting to demoDB [{}]", demoDBLocation.getAbsolutePath());
			String jdbcLink = createH2Link(demoDBLocation.getAbsolutePath() + File.separator + "db");
			if (StringUtils.isBlank(prop_dbUser)) {
				prop_dbUser = "sa";
			}
			configSourceCode = 3;
			return Optional.of(new DBConnection(DBType.H2, jdbcLink, prop_dbUser, prop_dbPassword.toCharArray()));
		}

		// 4) Are Environment Variables set ?
		if (isNotBlank(env_dbType) && isNotBlank(env_dbHost) && isNotBlank(env_dbDatabase) && isNotBlank(env_dbUsername)
				&& isNotBlank(env_dbUsername)) {
			Optional<DBType> dbType = DBConnection.DBType.valueOfIgnoreCase(env_dbType);
			if (dbType.isPresent()) {
				String env_jdbcLink = "jdbc:" + dbType.get().dbType.toLowerCase() + "://" + env_dbHost + "/"
						+ env_dbDatabase;
				if (isNotBlank(env_jdbcParameterString)) {
					env_jdbcLink += "?" + env_jdbcParameterString;
				}
				logger.info("Connecting to DB (Env) [{}]", env_jdbcLink);
				configSourceCode = 4;
				return Optional
						.of(new DBConnection(dbType.get(), env_jdbcLink, env_dbUsername, env_dbPassword.toCharArray()));
			}
		}

		// 5) -> CoreHub.localCfg can not be parsed here

		return Optional.empty();
	}

	private String createH2Link(String database) {
		String prefix = "jdbc:h2:";
		if (database.contains(".zip!")) {
			prefix += "zip:";
		}
		String connectString = "";
		// do not modify if database starts with in mem db prefix
		if (database.startsWith("jdbc:h2:mem:")) {
			connectString = database;
		} else {
			connectString = prefix + database + ";AUTO_SERVER=TRUE";
		}
		return connectString;
	}

	private String determineFlavorFromJdbcString(String prop_dbConnSpec) {
		String jdbcString = prop_dbConnSpec.trim().toLowerCase();
		if (jdbcString.startsWith("jdbc:h2")) {
			return "h2";
		} else if (jdbcString.startsWith("jdbc:mysql")) {
			return "mysql";
		} else if (jdbcString.startsWith("jdbc:postgresql")) {
			return "PostgreSQL";
		}
		return null;
	}

	public int getConfigSource() {
		return configSourceCode;
	}

}
