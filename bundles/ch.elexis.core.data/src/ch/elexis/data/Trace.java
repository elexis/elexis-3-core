package ch.elexis.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.IUser;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.net.NetTool;

/**
 * Trace actions done in the system by a user.
 */
public class Trace {
	
	public static final String TABLENAME = "traces";
	
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public static void addTraceEntry(final String username, final String workstation,
		final String action){
		executor.execute(() -> {
			String _username = username;
			if (StringUtils.isEmpty(username)) {
				IUser user = ContextServiceHolder.get().getActiveUser().orElse(null);
				if (user != null) {
					_username = user.getId();
				}
			}
			_username = (StringUtils.isEmpty(_username)) ? "unknown"
					: StringUtils.abbreviate(_username, 30);
			
			String _workstation = workstation;
			if (StringUtils.isEmpty(workstation)) {
				_workstation = NetTool.hostname;
			}
			_workstation = (StringUtils.isEmpty(_workstation)) ? "unknown"
					: StringUtils.abbreviate(_workstation, 40);
			String _action = (StringUtils.isEmpty(action)) ? "" : action;
			
			JdbcLink connection = PersistentObject.getConnection();
			
			String insertStatement = "INSERT INTO " + TABLENAME + " VALUES(?, ?, ?, ?)";
			
			if (connection != null) {
				PreparedStatement statement = connection.getPreparedStatement(insertStatement);
				try {
					statement.setLong(1, System.currentTimeMillis());
					statement.setString(2, _workstation);
					statement.setString(3, _username);
					statement.setString(4, _action);
					statement.execute();
				} catch (SQLException e) {
					LoggerFactory.getLogger(Trace.class).error("Catched this - FIX IT", e);
				} finally {
					connection.releasePreparedStatement(statement);
				}
			} else {
				// connection already gone ...
				LoggerFactory.getLogger(Trace.class).warn(
					"No DB connection for trace [station{} user{} action{}]", _workstation,
					_username, _action);
			}
		});
	}
	
	public static void addTraceEntry(String action){
		addTraceEntry(null, null, action);
	}
	
}
