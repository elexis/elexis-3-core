package ch.elexis.data.service.internal;

import javax.security.auth.login.LoginException;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IExternalLoginService;

@Component
public class CsvLoginService implements IExternalLoginService {
	
	private String[] rows;
	
	public void initCsvRows(String defaultContanctId){
		rows = new String[] {
			"sazgin1:password:" + defaultContanctId + ":" + RoleConstants.SYSTEMROLE_LITERAL_USER
				+ "," + RoleConstants.SYSTEMROLE_LITERAL_DOCTOR,
			"afi1:password:XXXXX:" + RoleConstants.SYSTEMROLE_LITERAL_USER + ","
				+ RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR
		};
		
	}
	
	@Override
	public IUser login(String username, char[] password) throws LoginException{
		
		IUser csvUser = new CsvUser(rows).login(username, password);
		if (csvUser == null) {
			throw new LoginException("Authentication failed");
		}
		return csvUser;
		
	}
}
