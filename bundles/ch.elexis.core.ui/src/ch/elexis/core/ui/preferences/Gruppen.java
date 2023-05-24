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

public class Gruppen extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Gruppen() {
//		super(GRID);
//		setPreferenceStore(new ConfigServicePreferenceStore(Scope.GLOBAL));
//		setDescription(Messages.Gruppen_GruppenUndRechte);
		setDescription("Bitte konfigurieren Sie die Daten in den entsprechenden Unterpunkten");
	}

	public void init(IWorkbench workbench) {
//		String groups = ConfigServiceHolder.getGlobal(Preferences.ACC_GROUPS, null);
//		if (groups == null) {
//			ConfigServiceHolder.setGlobal(Preferences.ACC_GROUPS, StringConstants.ROLES_DEFAULT);
//		}

	}

	@Override
	protected void createFieldEditors() {
//		if (AccessControlServiceHolder.get().request(AccessControlDefaults.ACL_USERS)) {
//			addField(new StringListFieldEditor(Preferences.ACC_GROUPS,
//				StringConstants.ROLES_NAMING, Messages.Gruppen_BitteGebenSieNameEin,
//				Messages.Core_Groups, getFieldEditorParent()));
//		} else {
//			new PrefAccessDenied(getFieldEditorParent());
//		}

	}

}
