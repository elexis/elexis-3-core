package ch.elexis.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.net.NetTool;

/**
 * Trace actions done in the system by a user.
 */
public class Trace {
	
	public static final String TABLENAME = "traces";
	
	public static void addTraceEntry(String username, String workstation, String action){
		if (StringUtils.isEmpty(username)) {
			User user = (User) ElexisEventDispatcher.getSelected(User.class);
			if (user != null) {
				username = user.getId();
			}
		}
		String _username =
			(StringUtils.isEmpty(username)) ? "unknown" : StringUtils.abbreviate(username, 30);
		
		if (StringUtils.isEmpty(workstation)) {
			workstation = NetTool.hostname;
		}
		String _workstation = (StringUtils.isEmpty(workstation)) ? "unknown"
				: StringUtils.abbreviate(workstation, 40);
		String _action = (StringUtils.isEmpty(action)) ? "" : action;
		
		JdbcLink connection = PersistentObject.getConnection();
		
		String insertStatement = "INSERT INTO " + TABLENAME + " VALUES(?, ?, ?, ?)";
		
		PreparedStatement statement = connection.getPreparedStatement(insertStatement);
		try {
			statement.setString(1, Long.toString(System.currentTimeMillis()));
			statement.setString(2, _workstation);
			statement.setString(3, _username);
			statement.setString(4, _action);
			statement.execute();
		} catch (SQLException e) {
			LoggerFactory.getLogger(Trace.class).error("Catched this - FIX IT", e);
		} finally {
			connection.releasePreparedStatement(statement);
		}
	}
	
	public static void addTraceEntry(String action){
		addTraceEntry(null, null, action);
	}
	
}
