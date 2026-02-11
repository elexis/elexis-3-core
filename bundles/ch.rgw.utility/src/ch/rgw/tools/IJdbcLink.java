package ch.rgw.tools;

import java.sql.PreparedStatement;

/**
 * @Deprecated this is only used to uncouple code that still requires JdbcLink
 */
public interface IJdbcLink {

	int exec(String string);

	PreparedStatement prepareStatement(String string);

}
