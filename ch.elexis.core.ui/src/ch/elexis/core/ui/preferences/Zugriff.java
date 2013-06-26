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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.ACE;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.admin.IACLContributor;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.preferences.inputs.ACLPreferenceTree;
import ch.elexis.core.ui.preferences.inputs.PrefAccessDenied;

/** Einstellungen für die Zugriffsregelung. anwender, Passworte usw. */

public class Zugriff extends PreferencePage implements IWorkbenchPreferencePage {
	ACLPreferenceTree apt;
	
	public Zugriff(){
		super(Messages.Zugriff_AccessRights);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createContents(Composite parent){
		CoreHub.acl.load();
		if (CoreHub.acl.request(AccessControlDefaults.ACL_USERS)) {
			List<IACLContributor> acls =
				Extensions.getClasses("ch.elexis.ACLContribution", "ACLContributor"); //$NON-NLS-1$ //$NON-NLS-2$
			ArrayList<ACE> lAcls = new ArrayList<ACE>(100);
			for (IACLContributor acl : acls) {
				for (ACE s : acl.getACL()) {
					lAcls.add(s);
					// TODO collision detection
				}
			}
			
			apt = new ACLPreferenceTree(parent, (ACE[]) lAcls.toArray(new ACE[0]));
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
