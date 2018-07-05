/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.extension;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.IRunnableWithProgress;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;

/**
 * @since 3.0.0
 */
public abstract class AbstractCoreOperationAdvisor {
	
	/**
	 * Configure the database connection, originally done by DBConnectWizard. Has to
	 * CoreHub.localCfg.set(PersistentObject.CFG_FOLDED_CONNECTION, conn);
	 * 
	 * Elexis will shutdown after this connection is configured and will have to be restarted.
	 * Called by {@link PersistentObject#connect(ch.rgw.io.Settings)}
	 */
	public abstract void requestDatabaseConnectionConfiguration();
	
	/**
	 * Has to create the initial mandator for the system. This was originally done in
	 * ErsterMandantDialog. Has to set the required access control.
	 * 
	 * Elexis will continue operation after the initialization. Called by
	 * {@link PersistentObject#connect(ch.rgw.tools.JdbcLink)}
	 */
	public abstract void requestInitialMandatorConfiguration();
	
	/**
	 * Adapt the context to the change of a user login. That is e.g. de-/activate menus according to
	 * user rights etc. This was originally done in GlobalActions#adaptForUser()
	 * 
	 * Called within {@link Anwender#login(String, String)}
	 */
	public abstract void adaptForUser();
	
	/**
	 * Ask the user a question to be answered with yes or no. This is analogous to
	 * org.eclipse.jface.MessageDialog#openQuestion
	 * 
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>true</code> if the user said yes, else <code>false</code>
	 */
	public abstract boolean openQuestion(String title, String message);
	
	/**
	 * Present an information to the user. This is analogous to
	 * org.eclipse.jface.MessageDialog#openInfo
	 * 
	 * @param title
	 * @param message
	 * @since 3.6
	 */
	public abstract void openInformation(String title, String message);
	
	/**
	 * Perform the login. May use {@link Anwender#login(String, String)} to initialize the log-in.
	 * Required Post-Condition: {@link CoreHub#actUser} and {@link CoreHub#actMandant} have to
	 * contain valid elements.
	 * 
	 * UI-useage: Presents either the user a dialog prompting for username/password or uses the
	 * System.properties ch.elexis.username and ch.elexis.password to bypass the login dialog. The
	 * second is needed for automated GUI tests.
	 * 
	 * @param shell
	 *            and object castable to org.eclipse.swt.widgets.Shell
	 */
	public abstract void performLogin(Object shell);
	
	/**
	 * UI only
	 * 
	 * @return the initial perspective to be opened to the user
	 */
	public abstract String getInitialPerspective();
	
	/**
	 * Perform the database update.
	 * 
	 * @param array
	 *            the array of SQL commands to execute
	 * @param pluginId
	 *            the plugin id requesting the update
	 * @return <code>true</code> if the update was successful, in this case the global variable
	 *         {@link CoreHub#DBVersion} will be updated
	 */
	public abstract boolean performDatabaseUpdate(String[] array, String pluginId);
	
	/**
	 * Provide progress information to the user
	 * 
	 * @param irwp
	 * @param taskName
	 * @since 3.6 added taskName
	 */
	public abstract void showProgress(IRunnableWithProgress irwp, String taskName);
}
