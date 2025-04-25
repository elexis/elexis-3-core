package ch.elexis.core.services.oauth2;

import com.google.gson.annotations.SerializedName;

public class KeycloakAccessTokenResponse {

	@SerializedName("access_token")
	protected String token;

	@SerializedName("expires_in")
	protected long expiresIn;

	@SerializedName("refresh_expires_in")
	protected long refreshExpiresIn;

	@SerializedName("refresh_token")
	protected String refreshToken;

	@SerializedName("token_type")
	protected String tokenType;

	@SerializedName("id_token")
	protected String idToken;

	@SerializedName("not-before-policy")
	protected int notBeforePolicy;

	@SerializedName("session_state")
	protected String sessionState;

	// OIDC Financial API Read Only Profile : scope MUST be returned in the response
	// from Token Endpoint
	@SerializedName("scope")
	protected String scope;

	@SerializedName("error")
	protected String error;

	@SerializedName("error_description")
	protected String errorDescription;

	@SerializedName("error_uri")
	protected String errorUri;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public long getRefreshExpiresIn() {
		return refreshExpiresIn;
	}

	public void setRefreshExpiresIn(long refreshExpiresIn) {
		this.refreshExpiresIn = refreshExpiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public int getNotBeforePolicy() {
		return notBeforePolicy;
	}

	public void setNotBeforePolicy(int notBeforePolicy) {
		this.notBeforePolicy = notBeforePolicy;
	}

	public String getSessionState() {
		return sessionState;
	}

	public void setSessionState(String sessionState) {
		this.sessionState = sessionState;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getErrorUri() {
		return errorUri;
	}

	public void setErrorUri(String errorUri) {
		this.errorUri = errorUri;
	}
}
