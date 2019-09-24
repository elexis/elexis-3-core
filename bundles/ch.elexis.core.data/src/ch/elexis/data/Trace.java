package ch.elexis.data;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
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
		Stm statement = connection.getStatement();
		try {
			statement.exec("INSERT INTO " + TABLENAME + " VALUES("
				+ Long.toString(System.currentTimeMillis()) + ", "
				+ connection.wrapFlavored(_workstation) + ", " + connection.wrapFlavored(_username)
				+ ", " + connection.wrapFlavored(_action) + ")");
		} finally {
			connection.releaseStatement(statement);
		}
	}
	
	public static void addTraceEntry(String action){
		addTraceEntry(null, null, action);
	}
	
}
