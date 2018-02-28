/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    A. Kaufmann - Allow extraction of arbitrary fields
 *    
 *******************************************************************************/

package ch.elexis.core.data.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class ScriptUtil {
	
	/**
	 * Get a data type from a plugin that implements IDataAccess and plugs into the EP DatAccess
	 * 
	 * @param connector
	 *            the string describing the desired data. the connector string follows the general
	 *            form "plugin:dependent_object:all|date|last:data-name[.Field]:parameters"
	 * @return the result of the
	 */
	public static Object loadDataFromPlugin(final String connector){
		String[] adr = connector.split(":");
		if (adr.length < 4) {
			MessageEvent.fireError("Datenzugriff-Fehler", "Das Datenfeld " + connector
				+ " wird falsch angesprochen");
			return null;
		}
		String plugin = adr[0];
		String dependendObject = adr[1];
		String dates = adr[2];
		String desc = adr[3];
		String[] params = null;
		if (adr.length == 5) {
			params = adr[4].split("\\.");
		}
		
		PersistentObject ref = null;
		if (dependendObject.equals("Patient")) {
			ref = ElexisEventDispatcher.getSelectedPatient();
		} else if (dependendObject.equals("Konsultation")) {
			ref = Konsultation.getAktuelleKons();
		}
		for (IConfigurationElement ic : Extensions
			.getExtensions(ExtensionPointConstantsData.DATA_ACCESS, "DataAccess")) {
			String icName = ic.getAttribute("name");
			if (icName.equals(plugin)) {
				IDataAccess ida;
				try {
					ida = (IDataAccess) ic.createExecutableExtension("class");
					Result<Object> ret = ida.getObject(desc, ref, dates, params);
					if (ret.isOK()) {
						return ret.get();
					} else {
						ResultAdapter.displayResult(ret, "Fehler beim  Einsetzen von Feldern");
					}
				} catch (CoreException e) {
					ExHandler.handle(e);
				}
				
			}
		}
		return null;
	}
}
