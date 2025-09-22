package ch.elexis.core.ui.dialogs.eedep;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ee.json.OpenIdConfiguration;
import ch.elexis.core.ee.json.WellKnownEE;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.services.oauth2.OAuth2Service;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.ui.ILoginNews;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.ExHandler;
import jakarta.inject.Inject;

public class EEDependentLoginDialog extends TitleAreaDialog {

	@Inject
	private HttpClient httpClient;

	@Inject
	private Gson gson;

	private Logger logger;

	private String eeHostname;

	private Text txtUsername;
	private Text txtPassword;

	private WellKnownEE wellKnownEE;
	private OpenIdConfiguration openidConfiguration;
	private AccessToken accessToken;
	private Button btnOidcLogin;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public EEDependentLoginDialog(Shell parentShell, String eeHostname) {
		super(parentShell);
		logger = LoggerFactory.getLogger(getClass());
		this.eeHostname = eeHostname;
		CoreUiUtil.injectServices(this);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitleImage(Images.IMG_EE_TITLE_BANNER.getImage(ImageSize._75x66_TitleDialogIconSize));
		setTitle("Elexis-Environment Anmeldung");
		setMessage("Überprüfe EE Verbindung ...");

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		txtUsername = new Text(container, SWT.BORDER);
		txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtUsername.setMessage("Benutzername");
		txtUsername.setEnabled(false);

		btnOidcLogin = new Button(container, SWT.FLAT);
		btnOidcLogin.setText("OIDC Login");
		btnOidcLogin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
		btnOidcLogin.setImage(Images.IMG_OIDC.getImage(ImageSize._75x66_TitleDialogIconSize));
		btnOidcLogin.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new OidcBrowserLoginDialog(getShell(), openidConfiguration.authorizationEndpoint).open();
			}
		});

		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		txtPassword.setMessage("Passwort");
		txtPassword.setEnabled(false);

		loadWellKnownEEInfo();

		@SuppressWarnings("unchecked")
		List<ILoginNews> newsModules = Extensions.getClasses(ExtensionPointConstantsUi.LOGIN_NEWS, "class"); //$NON-NLS-1$

		if (!newsModules.isEmpty()) {
			Composite cNews = new Composite(area, SWT.NONE);
			cNews.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
			cNews.setLayout(new GridLayout());
			for (ILoginNews lm : newsModules) {
				try {
					Composite comp = lm.getComposite(cNews);
					comp.setLayoutData(SWTHelper.getFillGridData());
				} catch (Exception ex) {
					// Note: This is NOT a fatal error. It just means, that the Newsmodule could not
					// load. Maybe we are offline.
					ExHandler.handle(ex);
				}
			}
		}

		return area;
	}

	private void loadWellKnownEEInfo() {
		new Thread(() -> {
			var request = HttpRequest
					.newBuilder(URI.create("https://" + eeHostname + "/.well-known/elexis-environment"))
					.header("accept", "application/json").build();

			try {
				String body = httpClient.send(request, BodyHandlers.ofString()).body();
				wellKnownEE = gson.fromJson(body, WellKnownEE.class);

				String organisationName = wellKnownEE.ee.config.organisationName.replaceAll("__", " ");

				request = HttpRequest.newBuilder(URI.create(wellKnownEE.openidConfiguration))
						.header("accept", "application/json").build();
				body = httpClient.send(request, BodyHandlers.ofString()).body();
				openidConfiguration = gson.fromJson(body, OpenIdConfiguration.class);

				getShell().getDisplay().syncExec(() -> {
					setErrorMessage(null);
					setMessage(organisationName + " (" + eeHostname + ")");
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					txtUsername.setEnabled(true);
					txtPassword.setEnabled(true);
					txtUsername.setFocus();
				});
			} catch (Exception e) {
				getShell().getDisplay().syncExec(() -> {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					setErrorMessage("Fehler bei EE Verbindung");
					logger.error("Cannot load .well-known/elexis-environment", e);
				});
			}
		}).start();
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Beenden", false);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	@Override
	protected void okPressed() {
		ObjectStatus<AccessToken> status = performDirectOAuthLogin();
		if (status.isOK()) {
			// FIXME handle access token
			accessToken = status.get();
			super.okPressed();
		} else {

			accessToken = null;
			setErrorMessage(txtUsername.getText() + ": " + status.getMessage());
		}
	}

	private ObjectStatus<AccessToken> performDirectOAuthLogin() {
		String clientSecret = System.getProperty(ElexisSystemPropertyConstants.EE_CLIENTSECRET);

		ObjectStatus<AccessToken> accessToken = new OAuth2Service().performDirectAccessGrantFlow(
				URI.create(openidConfiguration.tokenEndpoint), "elexis-rcp-openid", clientSecret, txtUsername.getText(),
				txtPassword.getText().toCharArray());
		if (accessToken.isOK()) {
			logger.info("Loaded access-token for [{}], valid until [{}], refresh until [{}]",
					accessToken.getObject().getUsername(),
					TimeUtil.toLocalDateTime(accessToken.getObject().getAccessTokenExpiration()),
					TimeUtil.toLocalDateTime(accessToken.getObject().refreshTokenExpiration()));
		} else {
			logger.warn("Could not load accessToken: " + accessToken.getMessage());
		}
		return accessToken;
	}

	@Override
	protected void cancelPressed() {
		wellKnownEE = null;
		accessToken = null;
		super.cancelPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	public WellKnownEE getWellKnownEE() {
		return wellKnownEE;
	}

	public AccessToken getAccessToken() {
		return accessToken;
	}

}
