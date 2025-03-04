package ch.elexis.core.jpa.liquibase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.List;

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
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDBInitializer {

	private static Logger logger = LoggerFactory.getLogger(LiquibaseDBInitializer.class);

	private DataSource dataSource;

	private String changelogXmlUrl;

	private IDatabaseUpdateUi updateProgress;

	public LiquibaseDBInitializer(DataSource dataSource, IDatabaseUpdateUi updateProgress) {
		this.dataSource = dataSource;
		this.changelogXmlUrl = "/db/elexisdb_master_initial.xml"; //$NON-NLS-1$
		this.updateProgress = updateProgress;
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
			DatabaseChangeLogLock[] existinglocks = liquibase.listLocks();
			if (existinglocks.length > 0) {
				long timestamp = existinglocks[0].getLockGranted().getTime();
				if (((System.currentTimeMillis() - timestamp) / 1000) > 14400) {
					logger.warn("Releasing lock older than 4h"); //$NON-NLS-1$
					liquibase.forceReleaseLocks();
				} else {
					updateProgress.setMessage(Messages.LiquibaseDBInitializer_Database_Locked
							+ existinglocks[0].getLockedBy() + Messages.LiquibaseDBInitializer_At // $NON-NLS-2$
							+ existinglocks[0].getLockGranted());
				}
			}
			if (updateProgress != null) {
				liquibase.setChangeExecListener(new AbstractChangeExecListener() {

					@Override
					public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
							RunStatus runStatus) {
						updateProgress
								.setMessage(Messages.LiquibaseDBInitializer_Init_Execute + changeSet.getDescription());
					}
				});
			}
			// only execute if the db does not exist already
			// else sync the changelog as the db already exists
			if (isFirstStart(connection)) {
				logger.info("Initialize database [" + connection + "] with liquibase"); //$NON-NLS-1$ //$NON-NLS-2$
				liquibase.update(StringUtils.EMPTY);
			} else {
				logger.info("Synchronize liquibase log of database [" + connection + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					liquibase.changeLogSync(StringUtils.EMPTY);
				} catch (Exception e) {
					if (e.getCause() instanceof ValidationFailedException || e instanceof ValidationFailedException) {
						logger.info("Validation failed clear checksums and retry"); //$NON-NLS-1$
						// removes current checksums from database, on next run checksums will be
						// recomputed
						liquibase.clearCheckSums();
						liquibase.changeLogSync(StringUtils.EMPTY);
					} else {
						logger.error("Exception performing DB init", e); //$NON-NLS-1$
					}
				}
			}
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB init.", e); //$NON-NLS-1$
			if (e instanceof SQLNonTransientConnectionException && updateProgress != null) {
				updateProgress
						.requestDatabaseConnectionConfiguration(e.getMessage());
			}
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

	private boolean isFirstStart(Connection connection) {
		return !getDbTables(connection).contains("CONFIG"); //$NON-NLS-1$
	}

	private static List<String> getDbTables(Connection con) {
		List<String> ret = new ArrayList<>();
		ResultSet result = null;
		try {
			DatabaseMetaData metaData = con.getMetaData();

			result = metaData.getTables(con.getCatalog(), null, "%", new String[] { "TABLE" }); //$NON-NLS-1$ //$NON-NLS-2$

			while (result.next()) {
				String tableName = result.getString("TABLE_NAME"); //$NON-NLS-1$
				ret.add(tableName.toUpperCase());
			}
		} catch (SQLException ex) {
			throw new IllegalStateException("An exception occured while trying to" + "analyse the database.", ex); //$NON-NLS-1$ //$NON-NLS-2$
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
