package ch.elexis.core.ui.eenv.login;

import javax.security.auth.login.LoginException;

import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.ILoginContributor;

@Component(property = "id=login.elexisenvironment")
public class ElexisEnvironmentLoginContributor implements ILoginContributor {

	@Reference
	private IElexisEnvironmentService elexisEnvironmentService;

	static final String OAUTH2_CLIENT_ID = "elexis-rcp-openid";
	static final String REALM_ID = "ElexisEnvironment";

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IUser performLogin(Object shell) throws LoginException {

		if (!(shell instanceof Shell)) {
			return null;
		}

		String baseUrl = elexisEnvironmentService.getKeycloakBaseUrl();
		String clientSecret = elexisEnvironmentService.getProperty(IElexisEnvironmentService.EE_RCP_OPENID_SECRET);
		// TODO what if clientSecret not yet set?
		String realmPublicKey = elexisEnvironmentService.getProperty(IElexisEnvironmentService.EE_KC_REALM_PUBLIC_KEY);

		ElexisEnvironmentLoginDialog loginTitleAreaDialog = new ElexisEnvironmentLoginDialog((Shell) shell,
				clientSecret, baseUrl, realmPublicKey);
		loginTitleAreaDialog.open();
		return loginTitleAreaDialog.getUser();
	}

}
