package ch.elexis.core.ui.services.internal;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.ILoginContributor;
import ch.elexis.core.utils.OsgiServiceUtil;

@Component(property = "id=login.dialog")
public class LoginDialogLoginContributor implements ILoginContributor {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public IUser performLogin(Object shell) throws LoginException {

		IElexisEnvironmentService elexisEnvironmentService = OsgiServiceUtil
				.getService(IElexisEnvironmentService.class, null).orElse(null);

		if (shell instanceof Shell) {
			// login dialog creates own shell, makes it appear in taskbar
			LocalUserLoginDialog loginDialog = new LocalUserLoginDialog(null, elexisEnvironmentService);
			loginDialog.create();
			loginDialog.getShell().setText(Messages.Core_Elexis_Login);
			loginDialog.setTitle(Messages.Core_Not_logged_in);
			loginDialog.setMessage(Messages.Core_Please_Enter_Name_and_Password);
			int retval = loginDialog.open();
			if (retval == Dialog.OK) {
				return loginDialog.getUser();
			}
		}

		return null;
	}

}
