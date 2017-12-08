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
package ch.elexis.core.ui.contacts.views.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

import ch.elexis.core.model.IContact;

public class ContactSelectorDragListener implements DragSourceListener {
	
	private final TableViewer viewer;
	
	public ContactSelectorDragListener(TableViewer viewer){
		this.viewer = viewer;
	}
	
	@Override
	public void dragStart(DragSourceEvent event){}
	
	@Override
	public void dragSetData(DragSourceEvent event){
		// Here you do the convertion to the type which is expected.
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		IContact firstElement = (IContact) selection.getFirstElement();
		
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = firstElement.getId();
		}
	}
	
	@Override
	public void dragFinished(DragSourceEvent event){}
	
}
