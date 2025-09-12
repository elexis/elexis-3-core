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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.application.advisors.ApplicationWorkbenchAdvisor;
import ch.elexis.core.common.DBConnection;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.events.MessageEvent;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.StatusDialog;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectDataSourceActivator;
import ch.rgw.io.FileTool;

public class Desk implements IApplication {

	private Logger log = LoggerFactory.getLogger(Desk.class);

	/**
	 * @since 3.0.0 log-in has been moved from ApplicationWorkbenchAdvisor to this
	 *        method
	 * @since 3.10 major refactorings to persistence startup
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// Check if we "are complete" - throws Error if not
		ICoreOperationAdvisor cod = CoreOperationAdvisorHolder.get();

		IElexisDataSource elexisDataSource = OsgiServiceUtil.getService(IElexisDataSource.class, "(id=default)")
				.orElseThrow();
		ObjectStatus connectionStatus = elexisDataSource.getCurrentConnectionStatus();
		if (connectionStatus != null && !connectionStatus.isOK()) {
			StatusDialog.show(connectionStatus);
			return IApplication.EXIT_OK;
		}

		if (System.getProperty(ElexisSystemPropertyConstants.OPEN_DB_WIZARD) != null) {
			if (connectionStatus != null) {
				cod.openInformation("Database connection overriden",
						"You requested to configure the database,\n"
								+ "but there is already a connection provided by a given setting.\n"
								+ "You're setting won't have an effect:\n" + StatusUtil.printStatus(connectionStatus));
			}
			cod.requestDatabaseConnectionConfiguration();
		}

		if (connectionStatus == null) {
			// no connection provided by DataSource - use the connection
			// configure in CoreHub.localCfg
			Optional<DBConnection> connection = CoreUtil.getDBConnection(CoreHub.localCfg);
			if (!connection.isPresent()) {
				// none found in CoreHub.localCfg - need to configure
				CoreOperationAdvisorHolder.get().requestDatabaseConnectionConfiguration();
				MessageEvent.fireInformation("Datenbankverbindung geändert", "Bitte starten Sie Elexis erneut");
				System.exit(0);
			}

			elexisDataSource.setDBConnection(connection.get());
			OsgiServiceUtil.ungetService(elexisDataSource);
		}

		// check for initialization parameters
		@SuppressWarnings("unchecked")
		Map<String, String> args = context.getArguments();
		if (args.containsKey("--clean-all")) { //$NON-NLS-1$
			String p = CoreUtil.getDefaultDBPath();
			FileTool.deltree(p);
			LocalConfigService.clear();
			LocalConfigService.flush();
		}

		// make sure identifiers are initialized
		initIdentifiers();

		// wait for persistent object to be ready
		PersistentObjectDataSourceActivator service = OsgiServiceUtil
				.getServiceWait(PersistentObjectDataSourceActivator.class, 5000).orElseThrow();
		OsgiServiceUtil.ungetService(service);

		// close splash
		context.applicationRunning();

		if (isNoMandator()) {
			cod.requestInitialMandatorConfiguration();
			MessageEvent.fireInformation("Neue Datenbank", //$NON-NLS-1$
					"Es wurde eine neue Datenbank angelegt."); //$NON-NLS-1$
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
			LocalConfigService.flush();
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
			UiDesk.getDisplay().dispose();
		}
	}

	private boolean isNoMandator() {
		List<IMandator> existing = new ArrayList<IMandator>();
		IAccessControlService accessControlService = OsgiServiceUtil.getServiceWait(IAccessControlService.class, 5000)
				.orElseThrow();
		accessControlService.doPrivileged(() -> {
			IModelService modelService = OsgiServiceUtil
					.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
					.orElseThrow();
			existing.addAll(modelService.getQuery(IMandator.class).execute());
			OsgiServiceUtil.ungetService(modelService);
		});
		OsgiServiceUtil.ungetService(accessControlService);
		return existing.isEmpty();
	}

	protected void initIdentifiers() {
		IAccessControlService accessControlService = OsgiServiceUtil.getServiceWait(IAccessControlService.class, 5000).orElseThrow();
		accessControlService.doPrivileged(() -> {
			IConfigService configService = OsgiServiceUtil.getServiceWait(IConfigService.class, 5000).orElseThrow();
			if (configService.get(Preferences.INSTALLATION_TIMESTAMP, null) == null) {
				LocalLock localLock = new LocalLock("initInstallationTimestamp"); //$NON-NLS-1$
				if (localLock.tryLock()) {
					configService.set(Preferences.INSTALLATION_TIMESTAMP, Long.toString(System.currentTimeMillis()));
				}
				localLock.unlock();
			}
			configService.setLocal(ch.elexis.core.constants.Preferences.SOFTWARE_OID, StringUtils.EMPTY);
			OsgiServiceUtil.ungetService(configService);
		});
		OsgiServiceUtil.ungetService(accessControlService);
	}

	@Override
	public void stop() {
	}
}
