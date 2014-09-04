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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;

import ch.elexis.core.model.IContact;

public class ContactSelectorDropListener extends ViewerDropAdapter {
	
	private int dropLocation;
	private IContact targetContact;
	
	public ContactSelectorDropListener(Viewer viewer){
		super(viewer);
	}
	
	@Override
	public void drop(DropTargetEvent event){
		dropLocation = determineLocation(event);
		targetContact = (IContact) determineTarget(event);
		super.drop(event);
	}
	
	// This method performs the actual drop
	// We simply add the String we receive to the model and trigger a refresh of
	// the viewer by calling its setInput method.
	@Override
	public boolean performDrop(Object data){
		if (dropLocation != LOCATION_ON)
			return false;
		String contactId = (String) data;
		System.out.println("TODO");
		// IContact sourceContact = IContactFactory.eINSTANCE.findById(contactId);
		//
		// BezugsKontaktAuswahl bza = new BezugsKontaktAuswahl();
		// if (bza.open() == Dialog.OK) {
		// String relationshipDescription = bza.getResult();
		// IRelationshipFactory.eINSTANCE.createRelationship(sourceContact,
		// targetContact, relationshipDescription);
		// }
		
		return true;
	}
	
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType){
		if (TextTransfer.getInstance().isSupportedType(transferType)) {
			return true;
		}
		return false;
	}
	
}
