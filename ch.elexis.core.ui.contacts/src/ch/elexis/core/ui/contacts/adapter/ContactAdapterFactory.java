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
package ch.elexis.core.ui.contacts.adapter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import ch.elexis.core.data.beans.ContactBean;

public class ContactAdapterFactory implements IAdapterFactory {
	
	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes")
	Class adapterType){
		if (adapterType == IPropertySource.class && adaptableObject instanceof ContactBean) {
			return new ContactPropertyAdapter((ContactBean) adaptableObject);
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class[] getAdapterList(){
		return new Class[] {
			IPropertySource.class
		};
	}
	
}
