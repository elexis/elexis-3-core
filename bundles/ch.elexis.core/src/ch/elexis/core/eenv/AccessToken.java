package ch.elexis.core.eenv;

import java.util.Date;

import ch.elexis.core.jdt.Nullable;

public class AccessToken {

	private final String token;
	private final Date accessTokenExpiration;
	private final String username;
	private final String refreshToken;
	private final Date refreshTokenExpirationDate;
	private final String tokenEndpoint;
	private final String clientId;

	public AccessToken(String token, Date accessTokenExpiration, String username, String refreshToken,
			Date refreshTokenExpiration) {
		this(token, accessTokenExpiration, username, refreshToken, refreshTokenExpiration, null, null);
	}

	public AccessToken(String token, Date accessTokenExpiration, String username, String refreshToken,
			Date refreshTokenExpiration, String tokenEndpoint, String clientId) {
		this.token = token;
		this.accessTokenExpiration = accessTokenExpiration;
		this.username = username;
		this.refreshToken = refreshToken;
		this.refreshTokenExpirationDate = refreshTokenExpiration;
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
	}

	public String getToken() {
		return token;
	}

	public Date getAccessTokenExpiration() {
		return accessTokenExpiration;
	}

	public String getUsername() {
		return username;
	}

	public @Nullable String getRefreshToken() {
		return refreshToken;
	}

	public @Nullable Date refreshTokenExpiration() {
		return refreshTokenExpirationDate;
	}

	public @Nullable String getClientId() {
		return clientId;
	}

	public @Nullable String getTokenEndpoint() {
		return tokenEndpoint;
	}

}
