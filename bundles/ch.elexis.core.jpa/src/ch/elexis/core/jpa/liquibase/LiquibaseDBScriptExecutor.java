package ch.elexis.core.jpa.liquibase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;

public class LiquibaseDBScriptExecutor {

	private static Logger logger = LoggerFactory.getLogger(LiquibaseDBScriptExecutor.class);

	private DataSource dataSource;

	public LiquibaseDBScriptExecutor(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	public boolean execute(String changeId, String sqlScript){
		CustomResourceAccessor resourceAccessor = new CustomResourceAccessor();
		changeId = changeId + ".xml";
		resourceAccessor.setContent(changeId, sqlScript);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			final DatabaseConnection database = new JdbcConnection(connection);
			Database targetDb =
				DatabaseFactory.getInstance().findCorrectDatabaseImplementation(database);
			
			Liquibase liquibase = new Liquibase(changeId, resourceAccessor, targetDb);
			liquibase.update("");
			return true;
		} catch (LiquibaseException | SQLException e) {
			// log and try to carry on
			logger.warn("Exception on DB execute script [" + changeId + "]", e);
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
		
		public void setContent(String changeId, String script){
			this.changeId = changeId;
			// append header
			stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\" xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd\">\n");
			
			// append the changeSet including the sql script
			stringBuilder.append("<changeSet author=\"" + getClass().getSimpleName() + "\" id=\""
				+ changeId.substring(0, changeId.lastIndexOf('.')) + "\">\n");
			stringBuilder
				.append("<sql dbms=\"h2\" splitStatements=\"true\" stripComments=\"true\">\n");
			stringBuilder.append(script);
			stringBuilder.append("\n</sql>");
			
			stringBuilder.append("\n</changeSet>");
			// close header
			stringBuilder.append("\n</databaseChangeLog>");
		}
		
		@Override
		public Set<InputStream> getResourcesAsStream(String changeId) throws IOException{
			if (this.changeId.equals(changeId)) {
				return Collections.singleton(new ByteArrayInputStream(
					stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
			}
			return Collections.emptySet();
		}
		
		@Override
		public Set<String> list(String arg0, String arg1, boolean arg2, boolean arg3, boolean arg4)
			throws IOException{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public ClassLoader toClassLoader(){
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
