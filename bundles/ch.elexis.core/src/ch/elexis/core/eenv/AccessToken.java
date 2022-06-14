package ch.elexis.core.eenv;

import java.util.Date;

public class AccessToken {

	private final String token;
	private final Date expirationTime;
	private final String username;

	public AccessToken(String token, Date expirationTime, String username) {
		this.token = token;
		this.expirationTime = expirationTime;
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public Date getExpirationTime() {
		return expirationTime;
	}

	public String getUsername() {
		return username;
	}

}
