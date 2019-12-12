package ch.elexis.core.ui.services.internal;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.ILoginContributor;

@Component(immediate = true)
public class LoginDialogLoginContributor implements ILoginContributor {
	
	@Override
	public int getPriority(){
		return 0;
	}
	
	@Override
	public IUser performLogin(Object shell) throws LoginException{
		
		if (shell instanceof Shell) {
			LocalUserLoginDialog loginDialog = new LocalUserLoginDialog((Shell) shell);
			loginDialog.create();
			loginDialog.getShell().setText(Messages.LoginDialog_loginHeader);
			loginDialog.setTitle(Messages.LoginDialog_notLoggedIn);
			loginDialog.setMessage(Messages.LoginDialog_enterUsernamePass);
			int retval = loginDialog.open();
			if (retval == Dialog.OK) {
				return loginDialog.getUser();
			}
		}
		
		return null;
	}
	
}
