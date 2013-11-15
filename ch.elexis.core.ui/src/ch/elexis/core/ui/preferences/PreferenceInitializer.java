/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;

/**
 * Vorgabewerte setzen, wo nötig. Bitte in den drei Funktionen dieser Klasse alle notwendigen
 * Voreinstellungen eintragen.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	
	/**
	 * Diese Funktion wird nach dem Erstellen des Display aufgerufen und dient zum Initialiseren
	 * früh benötigter Einstellungen, die bereits ein Display benötigen
	 * 
	 */
	public void initializeDisplayPreferences(Display display){
		UiDesk.getColorRegistry().put(UiDesk.COL_RED, new RGB(255, 0, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREEN, new RGB(0, 255, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_BLUE, new RGB(0, 0, 255));
		UiDesk.getColorRegistry().put(UiDesk.COL_SKYBLUE, new RGB(135, 206, 250));
		UiDesk.getColorRegistry().put(UiDesk.COL_LIGHTBLUE, new RGB(0, 191, 255));
		UiDesk.getColorRegistry().put(UiDesk.COL_BLACK, new RGB(0, 0, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREY, new RGB(0x60, 0x60, 0x60));
		UiDesk.getColorRegistry().put(UiDesk.COL_WHITE, new RGB(255, 255, 255));
		UiDesk.getColorRegistry().put(UiDesk.COL_DARKGREY, new RGB(50, 50, 50));
		UiDesk.getColorRegistry().put(UiDesk.COL_LIGHTGREY, new RGB(180, 180, 180));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREY60, new RGB(153, 153, 153));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREY20, new RGB(51, 51, 51));
		
		FontData[] small = new FontData[] {
			new FontData("Helvetica", 7, SWT.NORMAL)}; //$NON-NLS-1$
		CoreHub.userCfg
			.set(
				Preferences.USR_SMALLFONT + "_default", PreferenceConverter.getStoredRepresentation(small)); //$NON-NLS-1$
	}
	
	/**
	 * Diese Funktion wird nach erstem Erstellen der Datenbank (d.h. nur ein einziges Mal)
	 * aufgerufen und belegt globale Voreinstellungen. Hier alle im ganzen Netzwerk und für alle
	 * Benutzer gültigen Voreinstellungen eintragen
	 * 
	 */
	public void initializeGlobalPreferences(){
		IPreferenceStore global = new SettingsPreferenceStore(CoreHub.globalCfg);
		global.setDefault(Preferences.ABL_TRACE, "none"); //$NON-NLS-1$
		CoreHub.globalCfg.flush();
	}
	
	@Override
	public void initializeDefaultPreferences(){
		// TODO Auto-generated method stub
		
	}
}
