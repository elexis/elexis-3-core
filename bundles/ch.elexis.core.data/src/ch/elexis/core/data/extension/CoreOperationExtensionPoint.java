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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.constants.ExtensionPointConstantsData;

/**
 * @since 3.0.0
 */
public class CoreOperationExtensionPoint {
	private static Logger log = LoggerFactory
		.getLogger(CoreOperationExtensionPoint.class.getName());
	
	private AbstractCoreOperationAdvisor coa = null;
	private static CoreOperationExtensionPoint instance = null;
	
	private CoreOperationExtensionPoint(){
		IConfigurationElement[] config =
			Platform.getExtensionRegistry().getConfigurationElementsFor(
				ExtensionPointConstantsData.CORE_OPERATION_ADVISOR);
		
		if (config.length != 1) {
			// we must have exactly one CoreOperationAdvisor extension contribution
			for (IConfigurationElement ic : config) {
				log.error("CoreOperationExtensionPoint contribution in "
					+ ic.getNamespaceIdentifier() + "." + ic.getName());
			}
			throw new Error(
				"Error at CoreOperationExtensionPoint initialization, not exactly one, but "
					+ config.length
					+ " extension point contributions found. See log file. Exiting.");
		}
		
		try {
			IConfigurationElement e = ((IConfigurationElement) config[0]);
			final Object o = e.createExecutableExtension("advisor");
			if (o instanceof AbstractCoreOperationAdvisor) {
				coa = (AbstractCoreOperationAdvisor) o;
				log.debug("CoreOperationExtensionPoint found @ " + e.getContributor().getName()
					+ ": " + o.getClass().getName());
			}
			return;
		} catch (CoreException ex) {
			log.error("Error at CoreOperationExtensionPoint extension initialization", ex);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static AbstractCoreOperationAdvisor getCoreOperationAdvisor(){
		if (instance == null) {
			instance = new CoreOperationExtensionPoint();
		}
		if (instance.coa == null)
			throw new Error(
				"Error at CoreOperationExtensionPoint initialization, no extension point found. Exiting.");
		return instance.coa;
	}
}
