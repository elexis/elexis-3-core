package ch.elexis.core.ee.json;

import com.google.gson.annotations.SerializedName;

public class KeycloakAccessTokenJwt {

	public long exp;

	public long iat;

	public String[] aud;

	public String sub;

	public String name;

	public String azp;

	@SerializedName("preferred_username")
	public String preferredUsername;

	@SerializedName("given_name")
	public String givenName;

	@SerializedName("family_name")
	public String familyName;

	public String email;

	@SerializedName("realm_access")
	public RealmAccess realmAccess;

	@SerializedName("resource_access")
	public ResourceAccess resourceAccess;

	// custom elexis
	@SerializedName("associated-contact-id")
	public String associatedContactId;

	public static class RealmAccess {

		public String[] roles;

	}

	public static class ResourceAccess {

		// which are relevant?
	}

}
