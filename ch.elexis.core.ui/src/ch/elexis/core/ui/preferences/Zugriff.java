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

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.inputs.ACLPreferenceTree;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;

/** Einstellungen für die Zugriffsregelung. anwender, Passworte usw. */

public class Zugriff extends PreferencePage implements IWorkbenchPreferencePage {
	ACLPreferenceTree apt;
	
	public Zugriff(){
		super(Messages.Zugriff_AccessRights);
	}
	
	@Override
	protected Control createContents(Composite parent){
		if (CoreHub.acl.request(AccessControlDefaults.ACL_USERS)) {
			List<ACE> lAcls = ACE.getAllDefinedACElements();
			apt = new ACLPreferenceTree(parent,lAcls.toArray(new ACE[lAcls.size()]));
			return apt;
		} else {
			return new PrefAccessDenied(parent);
		}
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean performOk(){
		if (apt != null) {
			apt.flush();
		}
		return super.performOk();
	}
	
	@Override
	protected void performDefaults(){
		if (apt != null) {
			apt.reload();
		}
	}
	
}
