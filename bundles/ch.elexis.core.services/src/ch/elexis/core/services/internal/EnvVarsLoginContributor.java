package ch.elexis.core.services.internal;

import java.util.Optional;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.ILoginContributor;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

/**
 * Allow bypassing the login dialog, eg. for automated GUI-tests. Example: when
 * having a demoDB you may login directly by passing
 * <code>-vmargs -Dch.elexis.username=test -Dch.elexis.password=test</code> as
 * command line parameters to Elexis.
 *
 * This service performs authentication only against the local database.
 *
 * @since 3.8 extracted from CoreOperationAdvisor
 */
@Component(property = "id=login.envvars", immediate = true)
public class EnvVarsLoginContributor implements ILoginContributor {

	private Optional<IUser> dbUser;

	@Override
	public int getPriority() {
		return 1000;
	}

	@Override
	public IUser performLogin(Object shell) throws LoginException {
		String username = System.getProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME);
		String password = System.getProperty(ElexisSystemPropertyConstants.LOGIN_PASSWORD);

		if (StringUtils.isNotEmpty(username)) {
			LoggerFactory.getLogger(getClass()).warn("Bypassing LoginDialog with username " + username);
			AccessControlServiceHolder.get().doPrivileged(() -> {
				dbUser = CoreModelServiceHolder.get().load(username, IUser.class);
			});
			if (dbUser.isPresent()) {
				IUser user = dbUser.get().login(username, password.toCharArray());
				if (user != null && user.isActive()) {
					return user;
				} else {
					LoggerFactory.getLogger(getClass()).error("Authentication failed.");
				}
			}
		}

		return null;
	}

}
