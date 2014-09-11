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
package ch.elexis.core.console.application;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.console.application.test.ApplicationTestCode;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

/**
 * The main application class as referenced by the org.eclipse.core.runtime.applications extension
 * point.
 */
public class Application implements IApplication {
	
	private Logger log = LoggerFactory.getLogger(Application.class);
	
	@Override
	public Object start(IApplicationContext context) throws Exception{
		// register ElexisEvent and MessageEvent listeners
		log.debug("Registering " + CoreEventListenerRegistrar.class.getName());
		new CoreEventListenerRegistrar();
		
		// connect to the database
		try {
			if (PersistentObject.connect(CoreHub.localCfg) == false)
				log.error(PersistentObject.class.getName() + " initialization failed.");
		} catch (PersistenceException pe) {
			log.error("Initialization error", pe);
			pe.printStackTrace();
			System.exit(1);
		}
		
		// check connection by logging number of contact entries
		Query<Kontakt> qbe = new Query<>(Kontakt.class);
		log.debug("Number of contacts in DB: " + qbe.execute().size());
		
		// log-in
		String username = System.getProperty(ElexisSystemPropertyConstants.LOGIN_USERNAME);
		String password = System.getProperty(ElexisSystemPropertyConstants.LOGIN_PASSWORD);
		log.debug("Starting Login as " + username);
		if (username != null && password != null) {
			if (!Anwender.login(username, password)) {
				log.error("Authentication failed. Exiting.");
				System.exit(1);
			}
		} else {
			log.error("Does not support interactive log-in, please use system properties");
			System.exit(1);
		}
		
		// check if there is a valid user
		if ((CoreHub.actUser == null) || !CoreHub.actUser.isValid()) {
			// no valid user, exit (don't consider this as an error)
			log.warn("Exit because no valid user logged-in");
			PersistentObject.disconnect();
			System.exit(0);
		}
		
		// call the static test method to perform tasks, after return program
		// exits
		ApplicationTestCode.performApplicationTest();
		
		log.debug("Exiting");
		return null;
	}
	
	@Override
	public void stop(){}
}
