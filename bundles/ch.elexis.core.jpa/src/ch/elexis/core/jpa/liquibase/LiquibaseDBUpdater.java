package ch.elexis.core.jpa.liquibase;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entitymanager.ui.IDatabaseUpdateUi;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDBUpdater {

	private static Logger logger = LoggerFactory.getLogger(LiquibaseDBUpdater.class);

	private String changelogXmlUrl;

	private DataSource dataSource;

	private IDatabaseUpdateUi updateProgress;
	
	public LiquibaseDBUpdater(DataSource dataSource, IDatabaseUpdateUi updateProgress){
		this.dataSource = dataSource;
		this.changelogXmlUrl = "/db/elexisdb_master_update.xml";
		this.updateProgress = updateProgress;
	}
	
	public boolean update(){
		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(getClass().getClassLoader());

		Liquibase liquibase = null;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			final DatabaseConnection database = new JdbcConnection(connection);
			Database targetDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(database);

			liquibase = new Liquibase(changelogXmlUrl, resourceAccessor, targetDb);
			if (updateProgress != null) {
				liquibase.setChangeExecListener(new AbstractChangeExecListener() {
					@Override
					public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog,
						Database database, RunStatus runStatus){
						updateProgress.setMessage("Update execute: " + changeSet.getDescription());
					}
				});
			}
			logger.info("Updating database [" + connection + "] with liquibase");
			try {
				liquibase.update("");
			} catch (ValidationFailedException e) {
				logger.info("Validation failed clear checksums and retry");
				// removes current checksums from database, on next run checksums will be recomputed
				liquibase.clearCheckSums();
				liquibase.update("");
			}
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB update.", e);
			return false;
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
		return true;
	}
}
