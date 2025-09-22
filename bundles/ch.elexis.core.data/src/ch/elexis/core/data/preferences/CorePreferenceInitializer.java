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
package ch.elexis.core.data.preferences;

import static ch.elexis.core.constants.Preferences.SETTINGS_PREFERENCE_STORE_DEFAULT;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.data.DBConnection;

/**
 * Move to ch.elexis.core
 */
@Deprecated
public class CorePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {

		// Datenbank
		CoreHub.localCfg.set(Preferences.DB_NAME + SETTINGS_PREFERENCE_STORE_DEFAULT, "h2");
		String base = getDefaultDBPath();

		CoreHub.localCfg.set(Preferences.DB_CONNECT + SETTINGS_PREFERENCE_STORE_DEFAULT,
				"jdbc:h2:" + base + "/db;MODE=MySQL"); //$NON-NLS-1$ //$NON-NLS-2$
		CoreHub.localCfg.set(Preferences.DB_USERNAME + SETTINGS_PREFERENCE_STORE_DEFAULT, "sa"); //$NON-NLS-1$
		CoreHub.localCfg.set(Preferences.DB_PWD + SETTINGS_PREFERENCE_STORE_DEFAULT, StringUtils.EMPTY);
		CoreHub.localCfg.set(Preferences.DB_TYP + SETTINGS_PREFERENCE_STORE_DEFAULT, "mysql"); //$NON-NLS-1$
		// Ablauf
		File userhome = CoreUtil.getWritableUserDir();
		if (!userhome.exists()) {
			userhome.mkdirs();
		}

		CoreHub.localCfg.set(Preferences.ABL_LOGALERT + SETTINGS_PREFERENCE_STORE_DEFAULT, 1);
		CoreHub.localCfg.set(Preferences.ABL_LOGLEVEL + SETTINGS_PREFERENCE_STORE_DEFAULT, 2);
		CoreHub.localCfg.set(Preferences.ABL_BASEPATH + SETTINGS_PREFERENCE_STORE_DEFAULT, userhome.getAbsolutePath());
		CoreHub.localCfg.set(Preferences.ABL_CACHELIFETIME + SETTINGS_PREFERENCE_STORE_DEFAULT,
				DBConnection.CACHE_DEFAULT_LIFETIME);
		CoreHub.localCfg.set(Preferences.ABL_HEARTRATE + SETTINGS_PREFERENCE_STORE_DEFAULT, 30);
		CoreHub.localCfg.set(Preferences.ABL_BASEPATH + SETTINGS_PREFERENCE_STORE_DEFAULT, userhome.getAbsolutePath());

		String string = CoreHub.localCfg.get(Preferences.STATION_IDENT_ID, "null");
		if ("null".equals(string)) {
			CoreHub.localCfg.set(Preferences.STATION_IDENT_ID,
					Long.toString(Timestamp.valueOf(LocalDateTime.now()).toInstant().toEpochMilli()));
		}

		// default text module
		if (CoreHub.localCfg.get(Preferences.P_TEXTMODUL, null) == null) {
			CoreHub.localCfg.set(Preferences.P_TEXTMODUL, Preferences.P_TEXTMODUL_DEFAULT); // $NON-NLS-1$
		}

		CoreHub.localCfg.flush();
	}

	public static String getDefaultDBPath() {
		String base;
		File f = new File(CoreHub.getBasePath() + "/rsc/demodata"); //$NON-NLS-1$
		if (f.exists() && f.canWrite()) {
			base = f.getAbsolutePath();
		} else {
			base = System.getenv("TEMP"); //$NON-NLS-1$
			if (base == null) {
				base = System.getenv("TMP"); //$NON-NLS-1$
				if (base == null) {
					base = System.getProperty("user.home"); //$NON-NLS-1$
				}
			}
			base += "/elexisdata"; //$NON-NLS-1$
			f = new File(base);
			if (!f.exists()) {
				f.mkdirs();
			}
		}
		return base;
	}

}
