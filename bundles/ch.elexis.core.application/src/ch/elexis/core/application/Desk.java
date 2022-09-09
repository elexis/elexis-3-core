/*******************************************************************************
 * Copyright (c) 2013-2022 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.application;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.StatusDialog;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.PersistentObject;
import ch.rgw.io.FileTool;

public class Desk implements IApplication {

	private Logger log = LoggerFactory.getLogger(Desk.class);
	private Map<String, String> args = null;

	/**
	 * @since 3.0.0 log-in has been moved from ApplicationWorkbenchAdvisor to this
	 *        method
	 * @since 3.10 major refactorings to persistence startup
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// register ElexisEvent and MessageEvent listeners
		log.debug("Registering " + CoreEventListenerRegistrar.class.getName()); //$NON-NLS-1$
		new CoreEventListenerRegistrar();

		// Check if we "are complete" - throws Error if not
		ICoreOperationAdvisor cod = CoreOperationAdvisorHolder.get();

		IElexisDataSource elexisDataSource = OsgiServiceUtil.getService(IElexisDataSource.class).orElseThrow();
		ObjectStatus connectionStatus = elexisDataSource.getCurrentConnectionStatus();
		if (connectionStatus != null && !connectionStatus.isOK()) {
			StatusDialog.show(connectionStatus);
			return IApplication.EXIT_OK;
		}

		if (System.getProperty(ElexisSystemPropertyConstants.OPEN_DB_WIZARD) != null) {
			if (connectionStatus != null) {
				// TODO already connected, so your settings won't have any effect
				// what source is the connection from?
			}
			cod.requestDatabaseConnectionConfiguration();
		}

		// connect to the database, will set IElexisEntityManager and
		// activate PersistentObjectActivator
		Optional<DBConnection> connection = CoreUtil.getDBConnection(CoreHub.localCfg);
		elexisDataSource.setDBConnection(connection.get());
		OsgiServiceUtil.ungetService(elexisDataSource);

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
			log.error("Exception caught", ex); //$NON-NLS-1$
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
					log.error("No ConfigService available after 5 sec. skipping identifier init"); //$NON-NLS-1$
					MessageDialog.openError(UiDesk.getTopShell(), "Init error",
							"No ConfigService available after 5 sec. skipping identifier init");
					return;
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// continue waiting
			}
		}

		if (ConfigServiceHolder.getGlobal(Preferences.INSTALLATION_TIMESTAMP, null) == null) {
			LocalLock localLock = new LocalLock("initInstallationTimestamp"); //$NON-NLS-1$
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
