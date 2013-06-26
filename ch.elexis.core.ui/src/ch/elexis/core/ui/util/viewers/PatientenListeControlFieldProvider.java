/*******************************************************************************
 * Copyright (c) 2006-2009, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util.viewers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.data.Query;
import ch.rgw.tools.StringTool;

/**
 * Variante des DefaultControlFieldProviders. Falls im ersten Feld ein Leerzeichen eingegeben wird,
 * wird der Text vor dem Leerzeichen fürs erste Feld, der Text nach dem Leerzeichen fürs zweite Feld
 * verwendet. Die restlichen Felder werden ignoriert. Ohne Leerzeichen verhält sich diese Klasse
 * gleich wie der DefaultControlFieldProvider.
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 */
public class PatientenListeControlFieldProvider extends DefaultControlFieldProvider {
	public PatientenListeControlFieldProvider(CommonViewer viewer, String[] flds){
		super(viewer, flds);
	}
	
	public void setQuery(Query q){
		// specially handle search string with space in the first field.
		// if the first field contains a space, we consider the value as
		// a combination of the first field and the second field.
		
		String field0 = null;
		String field1 = null;
		
		if (lastFiltered.length >= 2 && lastFiltered[0].contains(" ")) {
			Pattern pattern = Pattern.compile("^(\\S+) +(.*)$");
			Matcher matcher = pattern.matcher(lastFiltered[0]);
			if (matcher.matches()) {
				field0 = matcher.group(1);
				field1 = matcher.group(2);
			}
		}
		
		if (field0 != null && field1 != null) {
			q.add(dbFields[0], "LIKE", field0 + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			q.and();
			q.add(dbFields[1], "LIKE", field1 + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			q.and();
			
			// remaining fields
			for (int i = 2; i < fields.length; i++) {
				if (!lastFiltered[i].equals(StringTool.leer)) {
					q.add(dbFields[i], "LIKE", lastFiltered[i] + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
					q.and();
				}
			}
			
			q.insertTrue();
		} else {
			// no space, normal behaviour
			super.setQuery(q);
		}
	}
}
