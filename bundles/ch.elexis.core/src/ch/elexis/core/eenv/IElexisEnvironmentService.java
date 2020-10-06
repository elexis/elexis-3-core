package ch.elexis.core.eenv;

public interface IElexisEnvironmentService {
	
	/**
	 * Entry in the Elexis#CONFIG TABLE with the elexis-environment hostname
	 */
	public static final String CFG_EE_HOSTNAME = "EE_HOSTNAME";
	
	/**
	 * The Open-Id secret to use for the elexis rcp client
	 */
	public static final String EE_RCP_OPENID_SECRET = "EE_RCP_OPENID_SECRET";
	
	/**
	 * The RSA public key used by the Keycloak ElexisEnvironment realm
	 */
	public static final String EE_KC_REALM_PUBLIC_KEY = "EE_KC_REALM_PUBLIC_KEY";
	
	/**
	 * Default station id of the Elexis-Server (can be overriden)
	 */
	public static final String ES_STATION_ID_DEFAULT = "ELEXIS-SERVER";
	
	/**
	 * @return the entry hostname of the elexis-environment
	 */
	public String getHostname();
	
	/**
	 * @return the version of the elexis-environment connected to.
	 */
	public String getVersion();
	
	/**
	 * @param key
	 * @return a property provided by the elexis-environment or <code>null</code> if not found
	 */
	public String getProperty(String key);
	
	default String getBaseUrl(){
		return "https://" + getHostname();
	}
	
	default String getRocketchatBaseUrl(){
		return getBaseUrl() + "/chat";
	}
	
	default String getRocketchatIntegrationBaseUrl(){
		return getRocketchatBaseUrl() + "/hooks/";
	}
	
	default String getBookstackBaseUrl(){
		return getBaseUrl() + "/bookstack";
	}
	
	default String getKeycloakBaseUrl(){
		return getBaseUrl() + "/keycloak";
	}
	
}
