package ch.elexis.core.jpa.liquibase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.OpenOptions;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDBScriptExecutor {

	private static Logger logger = LoggerFactory.getLogger(LiquibaseDBScriptExecutor.class);

	private DataSource dataSource;

	public LiquibaseDBScriptExecutor(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean execute(String changeId, String sqlScript) {
		CustomResourceAccessor resourceAccessor = new CustomResourceAccessor();
		changeId = changeId + ".xml"; //$NON-NLS-1$
		resourceAccessor.setContent(changeId, sqlScript);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			final DatabaseConnection database = new JdbcConnection(connection);
			Database targetDb = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(database);

			Liquibase liquibase = new Liquibase(changeId, resourceAccessor, targetDb);
			liquibase.update(StringUtils.EMPTY);
			liquibase.close();
			return true;
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB execute script [" + changeId + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("Exception on DB execute script [" + changeId + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
			return false;
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

	private class CustomResourceAccessor implements ResourceAccessor {

		private StringBuilder stringBuilder = new StringBuilder();
		private String changeId;

		private Resource scriptResource;

		public void setContent(String changeId, String script) {
			this.changeId = changeId;
			// append header
			stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" //$NON-NLS-1$
					+ "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd\">\n"); //$NON-NLS-1$

			// append the changeSet including the sql script
			stringBuilder.append("<changeSet author=\"" + getClass().getSimpleName() + "\" id=\"" //$NON-NLS-1$ //$NON-NLS-2$
					+ changeId.substring(0, changeId.lastIndexOf('.')) + "\">\n"); //$NON-NLS-1$
			stringBuilder.append("<sql dbms=\"h2\" splitStatements=\"true\" stripComments=\"true\">\n"); //$NON-NLS-1$
			stringBuilder.append(script);
			stringBuilder.append("\n</sql>"); //$NON-NLS-1$

			stringBuilder.append("\n</changeSet>"); //$NON-NLS-1$
			// close header
			stringBuilder.append("\n</databaseChangeLog>"); //$NON-NLS-1$

			scriptResource = new Resource() {

				@Override
				public Resource resolveSibling(String other) {
					return null;
				}

				@Override
				public Resource resolve(String other) {
					return null;
				}

				@Override
				public OutputStream openOutputStream(OpenOptions openOptions) throws IOException {
					return null;
				}

				@Override
				public InputStream openInputStream() throws IOException {
					return new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
				}

				@Override
				public boolean isWritable() {
					return false;
				}

				@Override
				public URI getUri() {
					try {
						return new URI(changeId);
					} catch (URISyntaxException e) {
						LoggerFactory.getLogger(getClass()).error("Error getting changeid uri");
					}
					return null;
				}

				@Override
				public String getPath() {
					return changeId;
				}

				@Override
				public boolean exists() {
					return true;
				}
			};
		}

		@Override
		public void close() throws Exception {

		}

		@Override
		public List<String> describeLocations() {
			return Collections.singletonList(changeId);
		}

		@Override
		public List<Resource> getAll(String path) throws IOException {
			if (path.contains(changeId)) {
				return Collections.singletonList(scriptResource);
			}
			return Collections.emptyList();
		}

		@Override
		public List<Resource> search(String path, boolean recursive) throws IOException {
			if (path.contains(changeId)) {
				return Collections.singletonList(scriptResource);
			}
			return Collections.emptyList();
		}

	}
}
