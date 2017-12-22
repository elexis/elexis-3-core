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
package ch.elexis.core.data.activator;

import java.io.File;

import org.osgi.framework.Bundle;

import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.rgw.io.Settings;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * 
 * @since 3.0.0
 */
public class CoreHubHelper {
	/**
	 * Revisionsnummer und Erstellungsdatum dieser Instanz ermitteln. Dazu wird die beim letzten
	 * Commit von Subversion geänderte Variable LastChangedRevision untersucht, und fürs Datum das
	 * von ANT beim build eingetragene Datum gesucht. Wenn diese Instanz nicht von ANT erstellt
	 * wurde, handelt es sich um eine Entwicklerversion, welche unter Eclipse-Kontrolle abläuft.
	 * 
	 * Note: Obsoleted with change to mercurial
	 * 
	 * @param plugin
	 */
	public static String getRevision(final boolean withDate, CoreHub plugin){
		StringBuilder sb = new StringBuilder();
		Bundle bundle = plugin.getBundle();
		org.osgi.framework.Version v = bundle.getVersion();
		sb.append("[Bundle info: ").append(v.toString());
		String check = System.getProperty("inEclipse"); //$NON-NLS-1$
		if (check != null && check.equals("true")) { //$NON-NLS-1$
			sb.append(" (developer version)");
		}
		if (withDate) {
			long lastModify = bundle.getLastModified();
			TimeTool tt = new TimeTool(lastModify);
			sb.append("; ").append(tt.toString(TimeTool.DATE_ISO));
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * return a directory suitable for plugin specific configuration data. If no such dir exists, it
	 * will be created. If it could not be created, the application will refuse to start.
	 * 
	 * @return a directory that exists always and is always writable and readable for plugins of the
	 *         currently running elexis instance. Caution: this directory is not necessarily shared
	 *         among different OS-Users. In Windows it is normally %USERPROFILE%\elexis, in Linux
	 *         ~./elexis
	 */
	public static File getWritableUserDir(){
		if (CoreHub.userDir == null) {
			String userhome = null;
			
			if (CoreHub.localCfg != null) {
				userhome = CoreHub.localCfg.get("elexis-userDir", null); //$NON-NLS-1$
			}
			if (userhome == null) {
				userhome = System.getProperty("user.home"); //$NON-NLS-1$
			}
			if (StringTool.isNothing(userhome)) {
				userhome = System.getProperty("java.io.tempdir"); //$NON-NLS-1$
			}
			CoreHub.userDir = new File(userhome, "elexis"); //$NON-NLS-1$
		}
		if (!CoreHub.userDir.exists()) {
			if (!CoreHub.userDir.mkdirs()) {
				System.err.print("fatal: could not create Userdir"); //$NON-NLS-1$
				MessageEvent.fireLoggedError("Panic exit", "could not create userdir " //$NON-NLS-1$ //$NON-NLS-2$
					+ CoreHub.userDir.getAbsolutePath());
				System.exit(-5);
			}
		}
		return CoreHub.userDir;
	}
	
	/**
	 * Workaround for bug https://redmine.medelexis.ch/issues/9501. Migrates old key values to new
	 * key values. Only lokal and global settings are supported.
	 * 
	 * @param oldKey
	 * @param newKey
	 * @param isGlobal
	 */
	public static void transformConfigKey(String oldKey, String newKey,
		boolean isGlobal){
		Settings settings = isGlobal ? CoreHub.globalCfg : CoreHub.localCfg;
		if (settings.get(oldKey, null) != null && settings.get(newKey, null) == null) {
			settings.set(newKey, settings.get(oldKey, null));
		}
	}
	
}
