package ch.elexis.core.ee.json;

import com.google.gson.annotations.SerializedName;

public class WellKnownRcp {

	public Config config = new Config();

	public static class Config {

		@SerializedName("enable-lock-service")
		public Boolean enableLockService = true;
	}
}
