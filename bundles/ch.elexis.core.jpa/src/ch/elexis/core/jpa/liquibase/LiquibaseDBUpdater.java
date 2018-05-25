package ch.elexis.core.jpa.liquibase;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDBUpdater {

	private static Logger logger = LoggerFactory.getLogger(LiquibaseDBUpdater.class);

	private String changelogXmlUrl;

	private DataSource dataSource;

	public LiquibaseDBUpdater(DataSource dataSource){
		this.dataSource = dataSource;
		this.changelogXmlUrl = "/db/elexisdb_master_update.xml";
	}
	
	public void update() {
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader());

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			final DatabaseConnection database = new JdbcConnection(connection);
			Database targetDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(database);

			Liquibase liquibase = new Liquibase(changelogXmlUrl, resourceAccessor, targetDb);
			liquibase.update("");
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB init.", e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				// ignore
			}
		}
	}
}
