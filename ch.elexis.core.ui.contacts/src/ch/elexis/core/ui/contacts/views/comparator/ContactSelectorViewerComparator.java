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
package ch.elexis.core.ui.contacts.views.comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ch.elexis.core.model.IContact;
import ch.elexis.core.ui.contacts.views.filter.KontaktAnzeigeTypViewerFilter;

public class ContactSelectorViewerComparator extends ViewerComparator {
	
	public static enum sorter {
		SORT_BY_FIRSTNAME("Vorname"), SORT_BY_FAMILYNAME("Nachname");
		
		public String label;
		
		private sorter(String label){
			this.label = label;
		}
	}
	
	private static sorter selectedSorter = sorter.SORT_BY_FAMILYNAME;
	
	public ContactSelectorViewerComparator(Viewer viewer){}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		IContact c1 = (IContact) e1;
		IContact c2 = (IContact) e2;
		
		switch (selectedSorter) {
		case SORT_BY_FIRSTNAME:
			if (c1.getDescription1() == null || c2.getDescription1() == null)
				return 0;
			return c1.getDescription1().compareToIgnoreCase(c2.getDescription1());
		case SORT_BY_FAMILYNAME:
			if (c1.getDescription2() == null || c2.getDescription2() == null)
				return 0;
			return c1.getDescription2().compareToIgnoreCase(c2.getDescription2());
		default:
			if (c1.getDescription2() == null || c2.getDescription2() == null)
				return 0;
			return c1.getDescription2().compareToIgnoreCase(c2.getDescription2());
		}
	}
	
	public static void setSelectedSorter(sorter selectedSorter){
		ContactSelectorViewerComparator.selectedSorter = selectedSorter;
		// We already have an implementation to refresh the viewer, so we simply use this one ...
		KontaktAnzeigeTypViewerFilter.refreshViewer();
	}
	
	public static sorter getSelectedSorter(){
		return selectedSorter;
	}
}
