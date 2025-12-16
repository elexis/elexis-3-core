package ch.elexis.core.jpa.liquibase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entitymanager.ui.IDatabaseUpdateUi;
import ch.elexis.core.l10n.Messages;
import liquibase.Scope;
import liquibase.Scope.ScopedRunner;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.AbstractChangeExecListener;
import liquibase.command.CommandScope;
import liquibase.command.core.ClearChecksumsCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.ChangeExecListenerCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;

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
		try {
			logger.info("Updating database [" + dataSource + "] with liquibase"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				applyLiquibaseChangelist(dataSource.getConnection(), changelogXmlUrl);
			} catch (Exception e) {
				if (e.getCause() instanceof ValidationFailedException || e instanceof ValidationFailedException) {
					logger.info("Validation failed clear checksums and retry"); //$NON-NLS-1$
					// removes current checksums from database, on next run checksums will be
					// recomputed
					clearCheckSums(dataSource.getConnection());
					applyLiquibaseChangelist(dataSource.getConnection(), changelogXmlUrl);
				} else {
					logger.error("Exception performing DB init", e); //$NON-NLS-1$
				}
			}
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB update.", e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	private void clearCheckSums(Connection connection) throws LiquibaseException {
		try (var database = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new JdbcConnection(connection))) {
			Map<String, Object> scopeObjects = Map.of(Scope.Attr.database.name(), database,
					Scope.Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor(getClass().getClassLoader()));

			Scope.child(scopeObjects, (ScopedRunner<?>) () -> new CommandScope(ClearChecksumsCommandStep.COMMAND_NAME)
					.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
					.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, database.getConnection().getURL())
					.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG,
							new AbstractChangeExecListener() {
								@Override
								public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog,
										Database database, RunStatus runStatus) {
									if (updateProgress != null) {
										updateProgress.setMessage(Messages.LiquibaseDBUpdater_Update_execute
												+ changeSet.getDescription());
									} else {
										logger.info(Messages.LiquibaseDBUpdater_Update_execute
												+ changeSet.getDescription());
									}
								}
							})
					.execute());
		} catch (LiquibaseException e) {
			throw e;
		} catch (Exception e) {
			// AutoClosable.close() may throw Exception
			throw new LiquibaseException(e);
		}

	}

	public void applyLiquibaseChangelist(Connection connection, String changelistClasspathResource)
			throws LiquibaseException {
		try (var database = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new JdbcConnection(connection))) {
			Map<String, Object> scopeObjects = Map.of(Scope.Attr.database.name(), database,
					Scope.Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor(getClass().getClassLoader()));

			Scope.child(scopeObjects,
					(ScopedRunner<?>) () -> new CommandScope("update")
							.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
							.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelistClasspathResource)
							.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS,
									new ChangeLogParameters(database))
							.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG,
									new AbstractChangeExecListener() {
										@Override
										public void willRun(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog,
												Database database, RunStatus runStatus) {
											if (updateProgress != null) {
												updateProgress.setMessage(Messages.LiquibaseDBUpdater_Update_execute
														+ changeSet.getDescription());
											} else {
												logger.info(Messages.LiquibaseDBUpdater_Update_execute
														+ changeSet.getDescription());
											}
										}
									})
							.execute());
		} catch (LiquibaseException e) {
			throw e;
		} catch (Exception e) {
			// AutoClosable.close() may throw Exception
			throw new LiquibaseException(e);
		}
	}
}
