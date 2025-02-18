/*******************************************************************************
 * Copyright (c) 2014 OPCoach.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Manumitting Technologies : Brian de Alwis for initial API and implementation
 *     OPCoach  : O.Prouvost fix bugs on hierarchy
 *******************************************************************************/
/* Handler to open up a configured preferences dialog.
* Written by Brian de Alwis, Manumitting Technologies.
* Placed in the public domain.
* This code comes from : http://www.eclipse.org/forums/index.php/fa/4347/
* and was referenced in the thread : http://www.eclipse.org/forums/index.php/m/750139/
*/
package ch.elexis.core.ui.e4.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ac.ConfigurationScope;
import jakarta.inject.Named;

/**
 * Open a PreferenceDialog for {@link ConfigurationScope#GLOBAL}
 * 
 * @see _com.opcoach.e4.preferences.handlers.E4PreferencesHandler
 *
 */
public class E4GlobalPreferencesHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, PreferenceManager pm, MApplication appli) {
		// Manage the possible null pm (case of pure E4 application. With E3 it
		// will be initialized by org.eclipse.ui.internal.WorkbenchPlugin
		// see line 1536
//		if (pm == null) {
//			pm = new E4PrefManager();
//			E4PreferenceRegistry registry = new E4PreferenceRegistry();
//			IEclipseContext appliContext = appli.getContext();
//			registry.populatePrefManagerWithE4Extensions(pm, appliContext);
//			appliContext.set(PreferenceManager.class, pm);
//		}

		// remove the non-global pages
		IPreferenceNode systemConfigurationLocal = pm.remove("system-configuration.local");
		IPreferenceNode userConfigurationLocal = pm.remove("system-configuration.user");

		PreferenceDialog dialog = new PreferenceDialog(shell, pm);

		dialog.create();
		dialog.getTreeViewer().setComparator(new ViewerComparator());
		dialog.getTreeViewer().expandAll();
		dialog.open();

		// re-add the removed pages
		pm.addToRoot(systemConfigurationLocal);
		pm.addToRoot(userConfigurationLocal);
	}

}
