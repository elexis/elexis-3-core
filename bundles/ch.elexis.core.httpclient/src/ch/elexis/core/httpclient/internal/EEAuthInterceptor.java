package ch.elexis.core.httpclient.internal;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.services.IContextService;

public class EEAuthInterceptor implements HttpRequestInterceptor {

	private final IContextService contextService;
	private final String eeHostname;

	public EEAuthInterceptor(String eeHostname, IContextService contextService) {
		this.eeHostname = eeHostname;
		this.contextService = contextService;
	}

	@Override
	public void process(HttpRequest request, EntityDetails entity, HttpContext context) {
		boolean eeTargetedRequest = request.getAuthority().toString().equals(eeHostname);
		if (eeTargetedRequest && "https".equals(request.getScheme())) {
			String accessToken = contextService.getTyped(AccessToken.class).map(AccessToken::getToken).orElse(null);
			if (accessToken == null) {
				LoggerFactory.getLogger(getClass()).info("No Bearer header for " + request.getPath());
			}
			request.addHeader("Authorization", "Bearer " + accessToken);
		}
	}

}
