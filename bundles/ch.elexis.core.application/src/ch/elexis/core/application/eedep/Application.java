package ch.elexis.core.application.eedep;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.application.advisors.ApplicationWorkbenchAdvisor;
import ch.elexis.core.common.DBConnection;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.oauth2.AccessTokenUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.e4.dialog.EEDependentLoginDialog;
import jakarta.inject.Inject;

public class Application implements IApplication {

	private Logger logger = LoggerFactory.getLogger(Application.class);

	private String eeHostname;

	@Inject
	IElexisDataSource elexisDataSource;

	@Inject
	IContextService contextService;

	@Inject
	HttpClient httpClient;

	public Object start(IApplicationContext context) {

		// picked up in ch.elexis.core.jpa.entitymanager.ElexisEntityManager
		System.setProperty(ElexisSystemPropertyConstants.CONN_SKIP_LIQUIBASE, Boolean.TRUE.toString());

		try {

			assertRequirements();

			context.applicationRunning();

			// (1) Determine EE to connect against and user to login
			// if ElexisSystemPropertyConstants.EE_HOSTNAME fixed bound (in Medelexis set
			// via license)
			// else user may add and select

			EEDependentLoginDialog eeDependentLoginDialog = new EEDependentLoginDialog(UiDesk.getTopShell(),
					eeHostname);
			int result = eeDependentLoginDialog.open();
			if (IDialogConstants.CANCEL_ID == result) {
				logger.info("User cancelled EEDependentLoginDialog. Exiting.");
				System.exit(0);
			}

			AccessToken accessToken = eeDependentLoginDialog.getAccessToken();
			IUser user = AccessTokenUtil.validateCreateIUser(accessToken);

			// requires clientSecret for elexis-rcp-openid via license or manual add
			// eleixs-rcp-openid should not work from outside too

			// (2) Use Token to load database connection info and start the
			// persistence layer
			DBConnection dbConnection = loadDBConnectionSettings(accessToken);

//		new Thread(() -> {

			// we initialize the persistence layer while the ui alredy starts up

			IStatus status = elexisDataSource.setDBConnection(dbConnection);
			if (!status.isOK()) {
				logger.error("Error connecting to database: " + status.getMessage());
				System.exit(-1);
			}

			// accessToken is required during activation of user
			contextService.setTyped(accessToken);
			contextService.setActiveUser(user);

//		}).start();
//
			// (2) Activate IElexisEnvironmentService ?

			// (3) Set user and token in context

			// Context should be active already

			// (4) Load DB Connection settings

			// FIXME Deactivate Logoff

			// start the workbench

			int returnCode = PlatformUI.createAndRunWorkbench(UiDesk.getDisplay(), new ApplicationWorkbenchAdvisor());
			// Die Funktion kehrt erst beim Programmende zurück.
			CoreHub.heart.suspend();
			LocalConfigService.flush();
			if (CoreHub.globalCfg != null) {
				CoreHub.globalCfg.flush();
			}
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} catch (Exception ex) {
			logger.error("Exception caught", ex); //$NON-NLS-1$
			MessageDialog.openError(UiDesk.getTopShell(), "Error", ex.getLocalizedMessage());
			return -1;
		}
	}

	@Override
	public void stop() {
		// nothing
	}

	private DBConnection loadDBConnectionSettings(AccessToken accessToken) {
		var request = HttpRequest
				.newBuilder(URI.create("https://" + eeHostname + "/api/v1/ops/elexis-rcp/dbconnection.json"))
				.header("accept", "application/json").header("Authorization", "Bearer " + accessToken.getToken())
				.build();
		try {
			String body = httpClient.send(request, BodyHandlers.ofString()).body();
			return new Gson().fromJson(body, DBConnection.class);
		} catch (Exception e) {
			throw new IllegalStateException("Can not fetch database connection settings", e);
		}
	}

	private void assertRequirements() {
		String clientSecret = System.getProperty(ElexisSystemPropertyConstants.EE_CLIENTSECRET);
		if (StringUtils.isBlank(clientSecret)) {
			throw new IllegalStateException("Missing property " + ElexisSystemPropertyConstants.EE_CLIENTSECRET);
		}
		eeHostname = System.getProperty(ElexisSystemPropertyConstants.EE_HOSTNAME);
		if (StringUtils.isBlank(eeHostname)) {
			throw new IllegalStateException("Missing property " + ElexisSystemPropertyConstants.EE_HOSTNAME);
		}

	}

}
