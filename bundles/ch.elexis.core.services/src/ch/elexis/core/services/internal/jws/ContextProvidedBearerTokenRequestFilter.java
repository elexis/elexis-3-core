package ch.elexis.core.services.internal.jws;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.services.holder.ContextServiceHolder;

/**
 * Add the users AccessToken as Bearer to the current request if available
 */
@Provider
public class ContextProvidedBearerTokenRequestFilter implements ClientRequestFilter {

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		ContextServiceHolder.get().getTyped(AccessToken.class).ifPresent(
				accessToken -> requestContext.getHeaders().add("Authorization", "Bearer " + accessToken.getToken()));
	}
}
