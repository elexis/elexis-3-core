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
package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;
import ch.elexis.core.ui.preferences.inputs.StringListFieldEditor;

public class Gruppen extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public Gruppen(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		setDescription(Messages.Gruppen_GruppenUndRechte);
	}
	
	public void init(IWorkbench workbench){
		String groups = CoreHub.globalCfg.get(Preferences.ACC_GROUPS, null);
		if (groups == null) {
			CoreHub.globalCfg.set(Preferences.ACC_GROUPS, StringConstants.ROLES_DEFAULT);
		}
		
	}
	
	@Override
	protected void createFieldEditors(){
		if (CoreHub.acl.request(AccessControlDefaults.ACL_USERS)) {
			addField(new StringListFieldEditor(Preferences.ACC_GROUPS,
				StringConstants.ROLES_NAMING, Messages.Gruppen_BitteGebenSieNameEin,
				Messages.Gruppen_Gruppen, getFieldEditorParent()));
		} else {
			new PrefAccessDenied(getFieldEditorParent());
		}
		
	}
	
}
