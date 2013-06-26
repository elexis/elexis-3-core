/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.application.advisors;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.ScannerEvents;

/**
 * Hier können Funktionen aufgerufen werden, die unmittelbar vor dem öffnen des Hauptfensters
 * erfolgen sollen. Im Wesentlichen werden hier die Menue und Toolbars gesetzt
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer){
		super(configurer);
	}
	
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer){
		return new ApplicationActionBarAdvisor(configurer);
	}
	
	/**
	 * Diese Methode wird jeweils unmittelbar vor dem öffnen des Anwendungsfensters ausgeführt.
	 */
	public void preWindowOpen(){
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		// configurer.setInitialSize(new Point(900, 700));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setTitle(Hub.APPLICATION_NAME);
		configurer.setShowFastViewBars(true);
		if (CoreHub.localCfg.get(Preferences.SHOWPERSPECTIVESELECTOR, Boolean.toString(false))
			.equals(Boolean.toString(true))) {
			configurer.setShowPerspectiveBar(true);
		} else {
			configurer.setShowPerspectiveBar(false);
		}
		// Wir wollen die schicken runden Tabs von Eclipse 3.x
		PlatformUI.getPreferenceStore().setValue(
			IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
		// Aber die Animationen sind eher nervend, nicht?
		PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
			false);
		
	}
	
	@Override
	public void postWindowOpen(){
		
	}
	
	@Override
	public boolean preWindowShellClose(){
		return true;
	}
	
	@Override
	public void createWindowContents(Shell shell){
		super.createWindowContents(shell);
		ScannerEvents.addListenerToDisplay(shell.getDisplay());
	}
	
}
