package ch.elexis.core.jpa.liquibase;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entitymanager.ui.IDatabaseUpdateUi;
import ch.elexis.core.l10n.Messages;
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

	public LiquibaseDBUpdater(DataSource dataSource, IDatabaseUpdateUi updateProgress) {
		this.dataSource = dataSource;
		this.changelogXmlUrl = "/db/elexisdb_master_update.xml"; //$NON-NLS-1$
		this.updateProgress = updateProgress;
	}

	public boolean update() {
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
					public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
							RunStatus runStatus) {
						updateProgress
								.setMessage(Messages.LiquibaseDBUpdater_Update_execute + changeSet.getDescription());
					}
				});
			}
			System.out.println("Updating database [" + connection + "] with liquibase");
			logger.info("Updating database [" + connection + "] with liquibase"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				liquibase.update(StringUtils.EMPTY);
			} catch (ValidationFailedException e) {
				logger.info("Validation failed clear checksums and retry"); //$NON-NLS-1$
				// removes current checksums from database, on next run checksums will be
				// recomputed
				liquibase.clearCheckSums();
				liquibase.update(StringUtils.EMPTY);
			}
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB update.", e); //$NON-NLS-1$
			System.out.println("Exception on DB update." + e);
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
