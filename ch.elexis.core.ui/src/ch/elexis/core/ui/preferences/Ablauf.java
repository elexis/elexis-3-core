/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.ui.preferences.inputs.ComboFieldEditor;
import ch.elexis.core.logging.LogbackUtils;

/**
 * Einstellungen für den Programmablauf. Logstufen etc.
 * 
 * @author Gerry
 */
public class Ablauf extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static String Logging_Default_Level = "logging/level"; //$NON-NLS-1$

	public Ablauf() {
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		String logbackInfo = "";
		String logbackPlace =
			System.getProperty(ElexisSystemPropertyConstants.LOGBACK_CONFIG_FILE, null);
		Logger logger = LoggerFactory.getLogger(this.getClass().getName());
		if (logbackPlace == null) {
			logbackInfo = Messages.LogbackConfigDefault;
		} else {
			logger.debug("Checking: " + logbackPlace + " -> " + Paths.get(logbackPlace).toAbsolutePath().toString());//$NON-NLS-1$
			File f = new File(Paths.get(logbackPlace).toAbsolutePath()
					.toString());
			if (f.exists() && !f.isDirectory()) {
				logbackInfo = String.format(Messages.LogbackConfigXmlExists,
						logbackPlace);
			} else {
				logbackInfo = String.format(Messages.LogbackConfigXmlMissing,
						logbackPlace);
			}
		}
		String msg = Messages.Ablauf_0 + "\n\n" //$NON-NLS-1$
				+ Messages.LogbackConfigDetails + "\n" //$NON-NLS-1$
				+ logbackInfo;
		setDescription(msg);
	}

	@Override
	protected void createFieldEditors() {
		String[] levels = new String[7];
		levels[0] = "OFF"; //$NON-NLS-1$
		levels[1] = "ERROR"; //$NON-NLS-1$
		levels[2] = "WARN"; //$NON-NLS-1$
		levels[3] = "INFO"; //$NON-NLS-1$
		levels[4] = "DEBUG"; //$NON-NLS-1$
		levels[5] = "TRACE"; //$NON-NLS-1$
		levels[6] = "ALL"; //$NON-NLS-1$

		addField(new ComboFieldEditor(Logging_Default_Level, "Logging-Level",
				levels, getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(Preferences.ABL_LANGUAGE,
				Messages.Ablauf_preferredLang, 1, new String[][] {
						{ Messages.Ablauf_german, "d" //$NON-NLS-1$
						}, { Messages.Ablauf_french, "f" //$NON-NLS-1$
						}, { Messages.Ablauf_italian, Messages.Ablauf_24 } },
				getFieldEditorParent()));

		addField(new IntegerFieldEditor(Preferences.ABL_CACHELIFETIME,
				Messages.Ablauf_cachelifetime, getFieldEditorParent()));

		addField(new IntegerFieldEditor(Preferences.ABL_HEARTRATE,
				Messages.Ablauf_heartrate, getFieldEditorParent()));

	}

	public void init(final IWorkbench workbench) {

	}

	static class EmptyFileFieldEditor extends FileFieldEditor {
		public EmptyFileFieldEditor(final String abl_logfile,
				final String string, final Composite fieldEditorParent) {
			super(abl_logfile, string, fieldEditorParent);
		}

		@Override
		protected boolean checkState() {
			return true;
		}
	}

	@Override
	public boolean performOk() {
		if (super.performOk()) {
			CoreHub.localCfg.flush();
			Logger logger = LoggerFactory.getLogger(this.getClass().getName());
			String level = CoreHub.localCfg.get(Logging_Default_Level, "DEBUG");//$NON-NLS-1$
			logger.warn("Switching log level to " + level);//$NON-NLS-1$
			LogbackUtils.setLogLevel(null, level);
			return true;
		}
		return false;
	}

}
