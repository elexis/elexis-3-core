/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;


public class FontPreference extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public FontPreference(){
		super(Messages.FontPreference_schriftarten, GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.userCfg));
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new FontFieldEditor(Preferences.USR_DEFAULTFONT,
			Messages.FontPreference_standardschriftart, "Elexis", getFieldEditorParent())); //$NON-NLS-1$
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean performOk(){
		boolean ret = super.performOk();
		UiDesk.updateFont(Preferences.USR_DEFAULTFONT);
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(CoreHub.actUser, Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
		return ret;
	}
	
}
