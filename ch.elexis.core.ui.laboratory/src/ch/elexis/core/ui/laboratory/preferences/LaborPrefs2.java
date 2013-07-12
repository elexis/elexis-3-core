/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.laboratory.preferences;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.LabItem;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.preferences.Messages;

public class LaborPrefs2 extends PreferencePage implements IWorkbenchPreferencePage {
	private final HashMap<String, List<LabItem>> groups = new HashMap<String, List<LabItem>>();
	
	public LaborPrefs2(){
		super(Messages.LaborPrefs2_LabItemsAndGroups);
	}
	
	@Override
	protected Control createContents(Composite parent){
		
		for (LabItem item : new Query<LabItem>(LabItem.class).execute()) {
			String groupname = item.getGroup();
			List<LabItem> group = groups.get(groupname);
			if (group == null) {
				group = new LinkedList<LabItem>();
			}
			group.add(item);
		}
		return null;
	}
	
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
}
