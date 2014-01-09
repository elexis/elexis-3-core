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
package ch.elexis.core.ui.contacts.decorator;

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import ch.elexis.core.model.IContact;
import ch.elexis.core.ui.icons.Images;

public class ContactTypeDecorator implements ILightweightLabelDecorator {
	
	@Override
	public void addListener(ILabelProviderListener listener){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isLabelProperty(Object element, String property){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void removeListener(ILabelProviderListener listener){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void decorate(Object element, IDecoration decoration){
		IContact contact = (IContact) element;
		if (contact.isDeleted()) {
			ImageDescriptor deleted = Images.IMG_DELETE.getImageDescriptor();
			decoration.addOverlay(deleted, IDecoration.TOP_LEFT);
		}
		
		if (contact.isMandator()) {
			ImageDescriptor vip = Images.IMG_VIP_OVERLAY.getImageDescriptor();
			decoration.addOverlay(vip, IDecoration.BOTTOM_RIGHT);
		}
		if (contact.isUser()) {
			FieldDecoration info =
				FieldDecorationRegistry.getDefault().getFieldDecoration(
					FieldDecorationRegistry.DEC_INFORMATION);
			ImageDescriptor infoD = ImageDescriptor.createFromImage(info.getImage());
			decoration.addOverlay(infoD, IDecoration.BOTTOM_LEFT);
		}
	}
	
}
