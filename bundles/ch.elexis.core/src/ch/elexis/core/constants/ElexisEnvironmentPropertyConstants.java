package ch.elexis.core.constants;

/**
 * System environment properties that are considered by Elexis.<br>
 * This is mostly targeted for Docker configuration of the Elexis-Server as username and password
 * should not be held as environment properties in a non-server system.<br>
 * The environment properties are considered case sensitive.
 */
public class ElexisEnvironmentPropertyConstants {
	
	/**
	 * The hostname/ip of the server, and an optional port
	 */
	public static String DB_HOST = "DB_HOST";
	public static String DB_DATABASE = "DB_DATABASE";
	public static String DB_TYPE = "DB_TYPE";
	public static String DB_USERNAME = "DB_USERNAME";
	public static String DB_PASSWORD = "DB_PASSWORD";

	public static String DB_JDBC_PARAMETER_STRING = "DB_JDBC_PARAMETER_STRING";
}
