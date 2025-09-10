package ch.elexis.core.ee.json;

import com.google.gson.annotations.SerializedName;

public class OpenIdConfiguration {

	public String issuer;

	@SerializedName("token_endpoint")
	public String tokenEndpoint;

}
