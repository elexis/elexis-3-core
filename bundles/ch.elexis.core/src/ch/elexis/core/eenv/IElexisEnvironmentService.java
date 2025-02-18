package ch.elexis.core.eenv;

import com.google.gson.JsonObject;

public interface IElexisEnvironmentService {

	/**
	 * Entry in the Elexis#CONFIG TABLE with the elexis-environment hostname
	 */
	static final String CFG_EE_HOSTNAME = "EE_HOSTNAME";

	/**
	 * The Open-Id secret to use for the elexis rcp client
	 */
	static final String EE_RCP_OPENID_SECRET = "EE_RCP_OPENID_SECRET";

	/**
	 * The RSA public key used by the Keycloak ElexisEnvironment realm
	 */
	static final String EE_KC_REALM_PUBLIC_KEY = "EE_KC_REALM_PUBLIC_KEY";

	/**
	 * Default station id of the Elexis-Server
	 */
	static final String ES_STATION_ID_DEFAULT = "ELEXIS-SERVER";

	/**
	 * The ID of the keycloak realm
	 */
	static final String EE_KEYCLOAK_REALM_ID = "ElexisEnvironment";

	/**
	 * @since EER3
	 */
	JsonObject getStatus();

	/**
	 * @return the entry hostname of the elexis-environment
	 */
	String getHostname();

	/**
	 * @return the version of the elexis-environment connected to.
	 */
	String getVersion();

	/**
	 * @param key
	 * @return a property provided by the elexis-environment or <code>null</code> if
	 *         not found
	 */
	String getProperty(String key);

	/**
	 * Connects to Keycloak to load the access token into the context
	 *
	 * @param username
	 * @param password
	 */
	void loadAccessToken(String username, char[] password);

	default String getBaseUrl() {
		return "https://" + getHostname();
	}

	default String getBookstackBaseUrl() {
		return getBaseUrl() + "/bookstack";
	}

	default String getKeycloakBaseUrl() {
		return getBaseUrl() + "/keycloak";
	}

	default String getOcrMyPdfBaseUrl() {
		return getBaseUrl() + "/ocrmypdf/";
	}

	default String getSolrBaseUrl() {
		return getBaseUrl() + "/solr/";
	}

	default String getNextcloudBaseUrl() {
		return getBaseUrl() + "/cloud/";
	}

}
