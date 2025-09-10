package ch.elexis.core.eenv;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.jdt.Nullable;

public class AccessToken {

	private final String token;
	private final Date tokenIssuedAt;
	private final Date accessTokenExpiration;
	private final String username;
	private final String refreshToken;
	private final Date refreshTokenExpirationDate;
	private final String tokenEndpoint;
	private final String clientId;

	private final Map<String, String> claims;

	public AccessToken(String token, Date tokenIssuedAt, Date accessTokenExpiration, String username,
			String refreshToken, Date refreshTokenExpiration) {
		this(token, tokenIssuedAt, accessTokenExpiration, username, refreshToken, refreshTokenExpiration, null, null);
	}

	public AccessToken(String token, Date tokenIssuedAt, Date accessTokenExpiration, String username,
			String refreshToken, Date refreshTokenExpiration, String tokenEndpoint, String clientId) {
		this.token = token;
		this.tokenIssuedAt = tokenIssuedAt;
		this.accessTokenExpiration = accessTokenExpiration;
		this.username = username;
		this.refreshToken = refreshToken;
		this.refreshTokenExpirationDate = refreshTokenExpiration;
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.claims = new HashMap<String, String>();
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

	public Date getTokenIssuedAt() {
		return tokenIssuedAt;
	}

	public @Nullable String getClientId() {
		return clientId;
	}

	public @Nullable String getTokenEndpoint() {
		return tokenEndpoint;
	}

	public void addClaim(String claim, String value) {
		claims.put(claim, value);
	}

	public @Nullable String getClaim(String claim) {
		return claims.get(claim);
	}

}
