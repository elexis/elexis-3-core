/*******************************************************************************
 * Copyright (c) 2013-2019 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.application;

import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.application.advisors.ApplicationWorkbenchAdvisor;
import ch.elexis.core.common.DBConnection;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.PersistentObject;
import ch.rgw.io.FileTool;

public class Desk implements IApplication {

	private Logger log = LoggerFactory.getLogger(Desk.class);
	private static Map<String, String> args = null;

	protected static ICoreOperationAdvisor cod = null;

	/**
	 * @since 3.0.0 log-in has been moved from ApplicationWorkbenchAdvisor to this
	 *        method
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// register ElexisEvent and MessageEvent listeners
		log.debug("Registering " + CoreEventListenerRegistrar.class.getName());
		new CoreEventListenerRegistrar();

		// Check if we "are complete" - throws Error if not
		cod = CoreOperationAdvisorHolder.get();

		if (System.getProperty(ElexisSystemPropertyConstants.OPEN_DB_WIZARD) != null) {
			cod.requestDatabaseConnectionConfiguration();
		}

		// connect to the database
		Optional<IElexisDataSource> datasource = ElexisDatasourceHolder.get();
		Optional<DBConnection> connection = CoreUtil.getDBConnection(CoreHub.localCfg);
		try {
			if (PersistentObject.connect(CoreHub.localCfg) == false) {
				log.error(PersistentObject.class.getName() + " po connect failed.");
			}

			if (datasource.isPresent() && connection.isPresent()) {
				IStatus setDBConnection = datasource.get().setDBConnection(connection.get());

				if (!setDBConnection.isOK()) {
					log.error("Error setting db connection", setDBConnection.getMessage());
				} else if (!PersistentObject.legacyPostInitDB()) {
					log.error(PersistentObject.class.getName() + " po data initialization failed.");
				}
			} else {
				String connstring = (connection.isPresent()) ? connection.get().connectionString : StringUtils.EMPTY;
				log.error("Can not connect to database, datasource or connection configuration missing. Datasource ["
						+ datasource + "] Connection [" + connstring + "]");
			}
		} catch (Throwable pe) {
			// error in database connection, we have to exit
			log.error("Database connection error", pe);
			pe.printStackTrace();

			Shell shell = PlatformUI.createDisplay().getActiveShell();
			StringBuilder sb = new StringBuilder();
			sb.append("Could not open database connection. Quitting Elexis.\n\n");
			sb.append("Message: " + pe.getMessage() + "\n\n");
			while (pe.getCause() != null) {
				pe = pe.getCause();
				sb.append("Reason: " + pe.getMessage() + StringUtils.LF);
			}
			sb.append("\n\nWould you like to re-configure the database connection?");
			boolean retVal = MessageDialog.openQuestion(shell, "Error in database connection", sb.toString());

			if (retVal) {
				cod.requestDatabaseConnectionConfiguration();
			}

			return IApplication.EXIT_OK;
		}

		String poConnectString = PersistentObject.getConnection().getConnectString();
		if (connection != null && connection.isPresent()) {
			String noPoConnectString = connection.get().connectionString;
			if (!poConnectString.equalsIgnoreCase(noPoConnectString)) {
				String msg = String.format("Connection string differ po [%s] nopo [%s]", poConnectString,
						noPoConnectString);
				log.error(msg);
				System.err.println(msg);
				return IApplication.EXIT_OK;
			}
		}
		// check for initialization parameters
		args = context.getArguments();
		if (args.containsKey("--clean-all")) { //$NON-NLS-1$
			String p = CorePreferenceInitializer.getDefaultDBPath();
			FileTool.deltree(p);
			CoreHub.localCfg.clear();
			CoreHub.localCfg.flush();
		}

		// make sure identifiers are initialized
		initIdentifiers();

		// close splash
		context.applicationRunning();

		Optional<IElexisEntityManager> entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class,
				"(id=default)");
		if (entityManager.isPresent()) {
			if (!entityManager.get().isUpdateSuccess()) {
				cod.openInformation("DB Update Fehler", "Beim Datenbank Update ist ein Fehler aufgetreten.\n"
						+ "Ihre Datenbank wurde nicht aktualisiert.\n" + "Details dazu finden Sie in der log Datei.");
			}
			OsgiServiceUtil.ungetService(entityManager.get());
		}

		// perform login
		cod.performLogin(new Shell(UiDesk.getDisplay()));
		if ((CoreHub.getLoggedInContact() == null) || !CoreHub.getLoggedInContact().isValid()) {
			// no valid user, exit (don't consider this as an error)
			log.warn("Exit because no valid user logged-in"); //$NON-NLS-1$
			PersistentObject.disconnect();
			System.exit(0);
		}

		// start the workbench
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(UiDesk.getDisplay(), new ApplicationWorkbenchAdvisor());
			// Die Funktion kehrt erst beim Programmende zurück.
			CoreHub.heart.suspend();
			CoreHub.localCfg.flush();
			if (CoreHub.globalCfg != null) {
				CoreHub.globalCfg.flush();
			}
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} catch (Exception ex) {
			log.error("Exception caught", ex);
			ex.printStackTrace();
			return -1;
		} finally {
			ElexisEventDispatcher.getInstance().shutDown();
			// give ElexisEventDispatcher time to shut down
			Thread.sleep(100);
			UiDesk.getDisplay().dispose();
		}
	}

	protected void initIdentifiers() {
		int waiting = 0;
		while (!ConfigServiceHolder.isPresent()) {
			try {
				// max 5 sek
				if (waiting++ > 50) {
					log.warn("No ConfigService available after 5 sec. skipping identifier init");
					return;
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// continue waiting
			}
		}

		if (ConfigServiceHolder.getGlobal(Preferences.INSTALLATION_TIMESTAMP, null) == null) {
			LocalLock localLock = new LocalLock("initInstallationTimestamp");
			if (localLock.tryLock()) {
				ConfigServiceHolder.setGlobal(Preferences.INSTALLATION_TIMESTAMP,
						Long.toString(System.currentTimeMillis()));
			}
			localLock.unlock();
		}
		// TODO add elexis OID if available
		CoreHub.localCfg.set(ch.elexis.core.constants.Preferences.SOFTWARE_OID, StringUtils.EMPTY);
		CoreHub.localCfg.flush();
	}

	@Override
	public void stop() {
	}
}
