package ch.elexis.core.jpa.liquibase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDBInitializer {

	private static Logger logger = LoggerFactory.getLogger(LiquibaseDBInitializer.class);

	private DataSource dataSource;

	private String changelogXmlUrl;

	public LiquibaseDBInitializer(DataSource dataSource){
		this.dataSource = dataSource;
		this.changelogXmlUrl = "/db/elexisdb_master_initial.xml";
	}

	public void init() {
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader());

		Liquibase liquibase = null;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			final DatabaseConnection database = new JdbcConnection(connection);
			Database targetDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(database);

			liquibase = new Liquibase(changelogXmlUrl, resourceAccessor, targetDb);
			// only execute if the db does not exist already
			// else sync the changelog as the db already exists
			if (isFirstStart(connection)) {
				logger.info("Initialize database [" + connection + "] with liquibase");
				liquibase.update("");
			} else {
				logger.info("Synchronize liquibase log of database [" + connection + "]");
				try {
					liquibase.changeLogSync("");
				} catch (ValidationFailedException e) {
					logger.info("Validation failed clear checksums and retry");
					// removes current checksums from database, on next run checksums will be recomputed
					liquibase.clearCheckSums();
					liquibase.changeLogSync("");
				}
			}
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB init.", e);
		} finally {
			try {
				if (liquibase != null) {
					liquibase.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}
	
	private boolean isFirstStart(Connection connection){
		return !getDbTables(connection).contains("CONFIG");
	}
	
	private static List<String> getDbTables(Connection con){
		List<String> ret = new ArrayList<String>();
		ResultSet result = null;
		try {
			DatabaseMetaData metaData = con.getMetaData();
			
			result = metaData.getTables(con.getCatalog(), null, "%", new String[] {
				"TABLE"
			});
			
			while (result.next()) {
				String tableName = result.getString("TABLE_NAME");
				ret.add(tableName.toUpperCase());
			}
		} catch (SQLException ex) {
			throw new IllegalStateException(
				"An exception occured while trying to" + "analyse the database.", ex);
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException ex) {
					// ignore
				}
			}
		}
		return ret;
	}
}
