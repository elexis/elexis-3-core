package ch.elexis.core.services.eenv;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.ServerRequest;
import org.keycloak.adapters.ServerRequest.HttpFailure;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.time.TimeUtil;

public class RefreshAccessTokenTimerTask extends TimerTask {

	private final KeycloakDeployment keycloakDeployment;
	private final IContextService contextService;
	private final Logger logger;

	public RefreshAccessTokenTimerTask(KeycloakDeployment keycloakDeployment, IContextService contextService) {
		this.keycloakDeployment = keycloakDeployment;
		this.contextService = contextService;
		this.logger = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void run() {
		contextService.getTyped(AccessToken.class).ifPresent(this::refreshAccessToken);
	}

	private void refreshAccessToken(AccessToken accessToken) {

		Optional<IUser> activeUser = contextService.getActiveUser();
		if (!activeUser.isPresent()) {
			contextService.removeTyped(AccessToken.class);
			logger.warn("Found access-token but no active user. Removed access-token.");
			return;
		}

		String activeUserId = activeUser.get().getId();
		if (!StringUtils.equalsIgnoreCase(activeUserId, accessToken.getUsername())) {
			contextService.removeTyped(AccessToken.class);
			logger.warn("Found access-token for user [{}] but active user id [{}]. Removed access-token.",
					accessToken.getUsername(), activeUserId);
			return;
		}

		// TODO check validity - what if EE restarted??

		long accessTokenTimeLeft = accessToken.getAccessTokenExpiration().getTime() - new Date().getTime();
		if (accessTokenTimeLeft <= 60 * 1000) {
			// we need to refresh the access-token
			// 1) try via refreshToken if available and not expired
			Date refreshTokenExpiration = accessToken.getAccessTokenExpiration();
			if (refreshTokenExpiration != null) {
				if (refreshTokenExpiration.getTime() > new Date().getTime()) {
					try {
						AccessTokenResponse invokeRefresh = ServerRequest.invokeRefresh(keycloakDeployment,
								accessToken.getRefreshToken());
						AccessToken _accessToken = AccessTokenUtil.load(invokeRefresh);
						contextService.setTyped(_accessToken);
						logger.info("RT Refreshed access-token for [{}], valid until [{}], refresh until [{}]",
								activeUserId, TimeUtil.toLocalDateTime(_accessToken.getAccessTokenExpiration()),
								TimeUtil.toLocalDateTime(_accessToken.refreshTokenExpiration()));
						return;
					} catch (IOException | HttpFailure e) {
						logger.warn("Failed to refresh access-token via refresh-token", e);
					}
				}
			}

			// 2) via re-entering username password - we ask for re-login
			Map<String, Object> eventMap = new HashMap<>();
			eventMap.put(ElexisEventTopics.NOTIFICATION_PROPKEY_TITLE, "EE Login expired");
			eventMap.put(ElexisEventTopics.NOTIFICATION_PROPKEY_MESSAGE,
					"Your EE login expired, please re-login to Elexis");
			logger.warn("Re-Login required, user informed");
			contextService.sendEvent(ElexisEventTopics.BASE_NOTIFICATION + "warn", null, eventMap);
		}

	}

}
