/*******************************************************************************
 * Copyright (c) 2013-2014 MEDEVIT <office@medevit.at>.
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

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.application.advisors.ApplicationWorkbenchAdvisor;
import ch.elexis.core.application.advisors.Messages;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.AbstractCoreOperationAdvisor;
import ch.elexis.core.data.extension.CoreOperationExtensionPoint;
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.PersistentObject;
import ch.rgw.io.FileTool;

public class Desk implements IApplication {
	
	private Logger log = LoggerFactory.getLogger(Desk.class);
	private static Map<String, String> args = null;
	
	protected static AbstractCoreOperationAdvisor cod = null;
	
	/**
	 * @since 3.0.0 log-in has been moved from ApplicationWorkbenchAdvisor to this method
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception{
		// register ElexisEvent and MessageEvent listeners
		log.debug("Registering " + CoreEventListenerRegistrar.class.getName());
		new CoreEventListenerRegistrar();
		
		// Check if we "are complete" - throws Error if not
		cod = CoreOperationExtensionPoint.getCoreOperationAdvisor();
		
		if(System.getProperty(ElexisSystemPropertyConstants.OPEN_DB_WIZARD)!=null) {
			cod.requestDatabaseConnectionConfiguration();
		}
		
		// connect to the database
		try {
			if (PersistentObject.connect(CoreHub.localCfg) == false)
				log.error(PersistentObject.class.getName() + " initialization failed.");
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
				sb.append("Reason: " + pe.getMessage() + "\n");
			}
			sb.append("\n\nWould you like to re-configure the database connection?");
			boolean retVal = MessageDialog.openQuestion(shell,
				"Error in database connection",
				sb.toString());
			
			if(retVal) {
				cod.requestDatabaseConnectionConfiguration();
			}
			
			return IApplication.EXIT_OK;
		}
		
		// check for initialization parameters
		args = context.getArguments();
		if (args.containsKey("--clean-all")) { //$NON-NLS-1$
			String p = CorePreferenceInitializer.getDefaultDBPath();
			FileTool.deltree(p);
			CoreHub.localCfg.clear();
			CoreHub.localCfg.flush();
		}
		
		// check if we should warn of too many instances
		if (CoreHub.isTooManyInstances()) {
			MessageDialog.openWarning(UiDesk.getDisplay().getActiveShell(),
				Messages.Warning_tooManyTitle,
				Messages.Warning_tooManyMessage + CoreHub.getWritableUserDir().getAbsolutePath());
		}
		
		// care for log-in
		WorkbenchPlugin.unsetSplashShell(UiDesk.getDisplay());
		cod.performLogin(UiDesk.getDisplay().getActiveShell());
		if ((CoreHub.actUser == null) || !CoreHub.actUser.isValid()) {
			// no valid user, exit (don't consider this as an error)
			log.warn("Exit because no valid user logged-in"); //$NON-NLS-1$
			PersistentObject.disconnect();
			System.exit(0);
		}
		// make sure identifiers are initialized
		initIdentifiers();
		
		// start the workbench
		try {
			int returnCode =
				PlatformUI.createAndRunWorkbench(UiDesk.getDisplay(),
					new ApplicationWorkbenchAdvisor());
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
		
	protected void initIdentifiers(){
		if (CoreHub.globalCfg.get(Preferences.INSTALLATION_TIMESTAMP, null) == null) {
			LocalLock localLock = new LocalLock("initInstallationTimestamp");
			if (localLock.tryLock()) {
				CoreHub.globalCfg.set(Preferences.INSTALLATION_TIMESTAMP,
					Long.toString(System.currentTimeMillis()));
				CoreHub.globalCfg.flush();
			}
			localLock.unlock();
		}
		// TODO add elexis OID if available
		CoreHub.localCfg.set(ch.elexis.core.constants.Preferences.SOFTWARE_OID, "");
		CoreHub.localCfg.flush();
	}
	
	@Override
	public void stop(){}
}
