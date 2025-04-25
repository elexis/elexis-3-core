package ch.elexis.core.services.oauth2;

import com.google.gson.annotations.SerializedName;

public class KeycloakOAuth2DeviceAuthorizationResponse {

	/**
	 * REQUIRED
	 */
	@SerializedName("device_code")
	protected String deviceCode;

	/**
	 * REQUIRED
	 */
	@SerializedName("user_code")
	protected String userCode;

	/**
	 * REQUIRED
	 */
	@SerializedName("verification_uri")
	protected String verificationUri;

	/**
	 * OPTIONAL
	 */
	@SerializedName("verification_uri_complete")
	protected String verificationUriComplete;

	/**
	 * REQUIRED
	 */
	@SerializedName("expires_in")
	protected long expiresIn;

	/**
	 * OPTIONAL
	 */
	@SerializedName("interval")
	protected long interval;

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getVerificationUri() {
		return verificationUri;
	}

	public void setVerificationUri(String verificationUri) {
		this.verificationUri = verificationUri;
	}

	public String getVerificationUriComplete() {
		return verificationUriComplete;
	}

	public void setVerificationUriComplete(String verificationUriComplete) {
		this.verificationUriComplete = verificationUriComplete;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

}
