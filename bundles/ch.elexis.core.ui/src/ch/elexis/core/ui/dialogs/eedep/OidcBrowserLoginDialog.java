package ch.elexis.core.ui.dialogs.eedep;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.swt.DisplayUISynchronize;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.equo.chromium.swt.Browser;
import com.google.gson.Gson;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.services.oauth2.AuthorizationCodeFlowWithPKCE;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import jakarta.inject.Inject;

public class OidcBrowserLoginDialog extends Dialog {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	Gson gson;

	UISynchronize uiSynchronizer;

	private String tokenEndpoint;
	private String authorizationEndpoint;
	private AuthorizationCodeFlowWithPKCE acfwpkce;
	private ObjectStatus<AccessToken> accessToken;

	private Browser browser;

	public OidcBrowserLoginDialog(Shell shell, String authorizationEndpoint, String tokenEndpoint) {
		super(shell);
		this.tokenEndpoint = tokenEndpoint;
		acfwpkce = new AuthorizationCodeFlowWithPKCE("elexis-rcp-openid");
		this.authorizationEndpoint = authorizationEndpoint;
		uiSynchronizer = new DisplayUISynchronize(shell.getDisplay());
		CoreUiUtil.injectServices(this);
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
		browser = new Browser(parent, SWT.None);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browser.setText("<HTML>Initiating login ...</HTML>");

		new Thread(handleFlowRunnable).start();

		return parent;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public AccessToken getAccessToken() {
		return accessToken.get();
	}

	@Override
	protected void cancelPressed() {
		acfwpkce.abort();
		super.cancelPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	private Runnable handleFlowRunnable = () -> {

		String clientSecret = System.getProperty(ElexisSystemPropertyConstants.EE_CLIENTSECRET);
		try {

			URI initiateFlowGetBrowserUrl = acfwpkce.initiateFlowGetBrowserUrl(URI.create(authorizationEndpoint),
					clientSecret);
			uiSynchronizer.asyncExec(() -> browser.setUrl(initiateFlowGetBrowserUrl.toString()));
			logger.debug("Initiated flow, waiting for authorization code ...");
			accessToken = acfwpkce.fetchAccessTokenAfterAuthorizationCode(gson, tokenEndpoint, clientSecret);
			StatusUtil.logStatus("Terminating", logger, accessToken, true, true);
			uiSynchronizer.asyncExec(() -> okPressed());

		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			logger.warn("Aborting Authorization Flow", e);
			acfwpkce.abort();
			uiSynchronizer.asyncExec(() -> browser.setText("<HTML>" + e.getMessage() + "</HTML>"));
		}
	};

}
