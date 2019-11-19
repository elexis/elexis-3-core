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
package ch.elexis.core.ui.contacts.views.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.model.IContact;

import ch.rgw.tools.StringTool;
public class KontaktAnzeigeTextFieldViewerFilter extends ViewerFilter {
	
	private String searchString;
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		if (searchString == null)
			return true;
		IContact k = (IContact) element;
		
		if (searchString.startsWith("$")) {
			// Nothing to do here, we have a formula evaluation
			return true;
		} else if (searchString.startsWith("#")) {
			// direct patient number lookup
			if (k.isPatient()) {
				String patNr = k.getCode();
				if (patNr.toLowerCase().equalsIgnoreCase(searchString.substring(1).trim()))
					return true;
			}
		} else {
			String desc1 = (k.getDescription1() != null) ? k.getDescription1().toLowerCase() : StringTool.leer;
			String desc2 = (k.getDescription2() != null) ? k.getDescription2().toLowerCase() : StringTool.leer;
			
			String[] searchListComma = searchString.split(",");
			for (String string : searchListComma) {
				if (string.contains(StringTool.space)) {
					String searchA = desc1 + StringTool.space + desc2;
					String searchB = desc2 + StringTool.space + desc1;
					if (searchA.matches(".*" + string + ".*")
						|| searchB.matches(".*" + string + ".*"))
						return true;
				} else if (desc1.matches(".*" + string + ".*")
					|| desc2.matches(".*" + string + ".*")) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void setSearchText(String s){
		if (s == null || s.length() == 0 || s.startsWith("#"))
			searchString = s;
		else
			searchString = s.toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
		// filter "dirty" characters
		if (searchString != null)
			searchString = searchString.replaceAll("[^#$, a-zA-Z0-9]", StringTool.leer);
	}
	
}
