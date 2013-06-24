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

import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.PersistentObject;

/**
 * @since 3.0.0
 */
public abstract class AbstractCoreOperationAdvisor {

	/**
	 * Configure the database connection, originally done by DBConnectWizard.
	 * Has to CoreHub.localCfg.set(PersistentObject.CFG_FOLDED_CONNECTION, conn);
	 * 
	 * Elexis will shutdown after this connection is configured and will have to
	 * be restarted. Called by
	 * {@link PersistentObject#connect(ch.rgw.io.Settings)}
	 */
	public abstract void requestDatabaseConnectionConfiguration();

	/**
	 * Has to create the initial mandator for the system. This was originally
	 * done in ErsterMandantDialog. Has to set the required access control.
	 * 
	 * Elexis will continue operation after the initialization. Called by
	 * {@link PersistentObject#connect(ch.rgw.tools.JdbcLink)}
	 */
	public abstract void requestInitialMandatorConfiguration();

	/**
	 * Adapt the context to the change of a user login. That is e.g.
	 * de-/activate menus according to user rights etc. This was originally done
	 * in GlobalActions#adaptForUser()
	 * 
	 * Called within {@link Anwender#login(String, String)}
	 */
	public abstract void adaptForUser();

	/**
	 * Ask the user a question to be answered with yes or no. This is analogous
	 * to org.eclipse.jface.MessageDialog#openQuestion
	 * 
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>true</code> if the user said yes, else <code>false</code>
	 */
	public abstract boolean openQuestion(String title, String message);
}
