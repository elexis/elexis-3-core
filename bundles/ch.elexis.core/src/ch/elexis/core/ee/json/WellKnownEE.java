package ch.elexis.core.ee.json;

import com.google.gson.annotations.SerializedName;

public class WellKnownEE {

	@SerializedName("openid-configuration")
	public String openidConfiguration;

	public EE ee;

	public static class EE {

		public Git git;

		public Config config;

	}

	public static class Git {

		public String branch;
	}

	public static class Config {

		@SerializedName("organisation-name")
		public String organisationName;
	}
}
