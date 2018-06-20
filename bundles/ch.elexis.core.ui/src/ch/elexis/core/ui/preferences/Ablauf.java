/*******************************************************************************
 * Copyright (c) 2005-2017, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT - https://redmine.medelexis.ch/issues/7833
 *    
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.core.runtime.Platform;
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
import ch.rgw.tools.StringTool;

public class Ablauf extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public Ablauf(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		String msg = Messages.Ablauf_0 + StringTool.lf + Messages.LogbackConfigDetails;
		setDescription(msg);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new RadioGroupFieldEditor(Preferences.ABL_LANGUAGE, Messages.Ablauf_preferredLang,
			1, new String[][] {
				{
					Messages.Ablauf_german, "d" //$NON-NLS-1$
				}, {
					Messages.Ablauf_french, "f" //$NON-NLS-1$
				}, {
					Messages.Ablauf_italian, Messages.Ablauf_24
				}
			}, getFieldEditorParent()));
			
		addField(new IntegerFieldEditor(Preferences.ABL_CACHELIFETIME,
			Messages.Ablauf_cachelifetime, getFieldEditorParent()));
		
		addField(new IntegerFieldEditor(Preferences.ABL_HEARTRATE, Messages.Ablauf_heartrate,
			getFieldEditorParent()));
		
	}
	
	@Override
	public void init(final IWorkbench workbench){
		
	}
	
	static class EmptyFileFieldEditor extends FileFieldEditor {
		public EmptyFileFieldEditor(final String abl_logfile, final String string,
			final Composite fieldEditorParent){
			super(abl_logfile, string, fieldEditorParent);
		}
		
		@Override
		protected boolean checkState(){
			return true;
		}
	}
	
	@Override
	public boolean performOk(){
		if (super.performOk()) {
			CoreHub.localCfg.flush();
			return true;
		}
		return false;
	}
	
}
