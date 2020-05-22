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
	public int getPriority(){
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public IUser performLogin(Object shell) throws LoginException{
		
		if (!(shell instanceof Shell)) {
			return null;
		}
		
		String baseUrl = elexisEnvironmentService.getKeycloakBaseUrl();
		String clientSecret =
			elexisEnvironmentService.getProperty(IElexisEnvironmentService.EE_RCP_OPENID_SECRET);
		// TODO what if clientSecret not yet set?
		String realmPublicKey =
			elexisEnvironmentService.getProperty(IElexisEnvironmentService.EE_KC_REALM_PUBLIC_KEY);
		
		//		final String apiSecret = "ad03191d-63ee-4c94-8be0-d5f2ae3d049a";
		//		final String baseUrl = "https://ee.medevit.at/keycloak";
		
//		final String realmPublicKey =
//			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4579OpoV09ysbmM3Yuwm5TflaM1BszUS8YuCI8B7BgIwwewvecpruUJdFeEJ3HF6pQJh90aApQ+h+P3M45wFY9ZjM3bJNnoMe+ydxPfwZZSN7WgXc+slRzAS1TtPmP8Hc5hbimry39odLQKIpl5ZMVBQ1lqmDCFOyj+Kug1BNVWoRk3r3S9VNhgZivviqKW8ti2qJZ+Zv8liiS4s0dVEU8eS0D70TUQ8NyI9QsR7tAbGeihX70x4OwGD6fst4DjDKsY62DYfjF4sMWfWxYLZAdhwY4xD/YXl2Ylz+mrhFHEAB+k00X1Td4v929Df8uonrtuuY7jTjGRruMkASfTjuQIDAQAB";
		
		ElexisEnvironmentLoginDialog loginTitleAreaDialog =
			new ElexisEnvironmentLoginDialog((Shell) shell, clientSecret, baseUrl, realmPublicKey);
		loginTitleAreaDialog.open();
		return loginTitleAreaDialog.getUser();
	}
	
}
