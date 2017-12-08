/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.dynamic;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.core.ui.contacts.views.comparator.ContactSelectorViewerComparator;

public class ContactSorterSwitcher extends ContributionItem {
	
	public ContactSorterSwitcher(){}
	
	public ContactSorterSwitcher(String id){
		super(id);
	}
	
	@Override
	public void fill(Menu menu, int index){
		for (final ContactSelectorViewerComparator.sorter sortMethod : ContactSelectorViewerComparator.sorter
			.values()) {
			MenuItem temp = new MenuItem(menu, SWT.CHECK, index);
			temp.setData(sortMethod);
			temp.setText(sortMethod.label);
			temp.setSelection(ContactSelectorViewerComparator.getSelectedSorter()
				.equals(sortMethod));
			temp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					ContactSelectorViewerComparator.setSelectedSorter(sortMethod);
				}
			});
		}
	}
	
	@Override
	public boolean isDynamic(){
		return true;
	}
}
