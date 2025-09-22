package ch.elexis.core.ui.dialogs.eedep;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import com.equo.chromium.swt.Browser;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.oauth2.AuthorizationCodeFlowWithPKCE;
import jakarta.inject.Inject;

public class OidcBrowserLoginDialog extends Dialog {

	@Inject
	IElexisEnvironmentService elexisEnvironmentService;

	String authorizationEndpoint;
	AuthorizationCodeFlowWithPKCE acfwpkce;

	public OidcBrowserLoginDialog(Shell shell, String authorizationEndpoint) {
		super(shell);
		acfwpkce = new AuthorizationCodeFlowWithPKCE();
		this.authorizationEndpoint = authorizationEndpoint;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		// FIXME should use external browser if possible to re-use session?!
		// or internal browser to share session for other sites (e.g. bookstack)
		// why? to be able to use non password methods -> Windows Hello
		// FHV has coupled windows login with sso (samlsso) -> redirect to saml sso html
		// page
		// with javascript to confirm
		Browser browser = new Browser(parent, SWT.None);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		String clientSecret = System.getProperty(ElexisSystemPropertyConstants.EE_CLIENTSECRET);
		try {
			URI initiateFlowGetBrowserUrl = acfwpkce.initiateFlowGetBrowserUrl(URI.create(authorizationEndpoint),
					"elexis-rcp-openid", clientSecret);
			browser.setUrl(initiateFlowGetBrowserUrl.toString());

		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			browser.setText("<HTML>" + e.getMessage() + "</HTML>");
			LoggerFactory.getLogger(getClass()).warn("Authorization Flow Error", e);
		}

		return parent;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

//	/**
//	 * Return the initial size of the dialog.
//	 */
//	@Override
//	protected Point getInitialSize() {
//		return new Point(450, 300);
//	}

}
