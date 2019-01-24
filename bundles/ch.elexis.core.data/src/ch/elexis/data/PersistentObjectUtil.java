/*******************************************************************************
 * Copyright (c) 2019, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Niklaus Giger <niklaus.giger@member.fsf.org>
 *    Marco Descher <descher@medevit.at>
 *******************************************************************************/
package ch.elexis.data;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.rgw.io.SqlSettings;
import ch.rgw.tools.TimeTool;
/**
 * Contains some utility procedure to make PersitentObject more readable$
 * @since 3.8
 * @author Niklaus Giger <niklaus.giger@member.fsf.org>
 */
class PersistentObjectUtil {

	/**
	 * New DB initialization: init Global Config table
	 * 
	 * @param connection
	 */
	static void initializeGlobalCfg(DBConnection connection) {
		CoreHub.globalCfg = new SqlSettings(connection.getJdbcLink(), "CONFIG"); //$NON-NLS-1$
		CoreHub.globalCfg.undo();
		CoreHub.globalCfg.set("created", new TimeTool().toString(TimeTool.FULL_GER)); //$NON-NLS-1$
	}

	/**
	 * When running from scratch or given 3 system properties for name, email and password
	 * we create a first mandator. This is needed for unit tests (runFromScratch) or
	 * GUI-tests. In both cases we want to startup Elexis with a clean, almost empty
	 * database which contains only default settings, eg. user rights$
	 * @since 3.8
	 * @param fromScratch Whether the database is running from Scratch
	 * @return mandant or null (meaning, please pop up FirstMandandDialog)
	 */
	static Mandant autoCreateFirstMandant(boolean fromScratch) {
		Mandant m = null;
		if (fromScratch) {
			String clientEmail = System.getProperty(ElexisSystemPropertyConstants.CLIENT_EMAIL);
			if (clientEmail == null) {
				clientEmail = "james@bond.invalid"; //$NON-NLS-1$
			}
			m = new Mandant("007", "topsecret", clientEmail); //$NON-NLS-1$ //$NON-NLS-2$
			m.set(new String[] { Person.NAME, Person.FIRSTNAME, Person.TITLE, Person.SEX, Person.FLD_PHONE1,
					Person.FLD_FAX, Kontakt.FLD_STREET, Kontakt.FLD_ZIP, Kontakt.FLD_PLACE }, "Bond", "James", //$NON-NLS-1$ //$NON-NLS-2$
					"Dr. med.", Person.MALE, //$NON-NLS-1$
					"0061 555 55 55", //$NON-NLS-1$
					"0061 555 55 56", "10, Baker Street", "9999", "Elexikon"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} else {
			String firstMandantName = System.getProperty(ElexisSystemPropertyConstants.FIRST_MANDANT_NAME, ""); //$NON-NLS-1$
			String firstMandantEmail = System.getProperty(ElexisSystemPropertyConstants.FIRST_MANDANT_EMAIL, ""); //$NON-NLS-1$
			String firstMandantPassword = System.getProperty(ElexisSystemPropertyConstants.FIRST_MANDANT_PASSWORD, ""); //$NON-NLS-1$
			// The FirstMandantDialog requires
			// * a name
			// * a password
			// * a email address containing a '@'
			// Therefore we apply the same tests here
			if (firstMandantEmail.contains("@") && !firstMandantName.isEmpty() //$NON-NLS-1$
					&& !firstMandantPassword.isEmpty()) {
				m = new Mandant(firstMandantName, firstMandantPassword, firstMandantEmail);
				m.set(Person.NAME, firstMandantName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		return m;
	}
}
