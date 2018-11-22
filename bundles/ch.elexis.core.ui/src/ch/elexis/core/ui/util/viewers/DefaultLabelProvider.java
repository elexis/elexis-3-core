/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util.viewers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.data.PersistentObject;

/**
 * Defaultimplementation des Labelproviders. Verwendet die getLabel() Methode von PersistentObject.
 * 
 * @author Gerry
 */

public class DefaultLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	public Image getColumnImage(Object element, int columnIndex){
		return null;
	}
	
	public String getColumnText(Object element, int columnIndex){
		if (element instanceof PersistentObject) {
			PersistentObject po = (PersistentObject) element;
			return po.getLabel();
		}
		return element.toString();
	}
	
	@Override
	public String getText(Object element){
		if (element instanceof PersistentObject) {
			PersistentObject po = (PersistentObject) element;
			return po.getLabel();
		}
		return element.toString();
	}
	
}