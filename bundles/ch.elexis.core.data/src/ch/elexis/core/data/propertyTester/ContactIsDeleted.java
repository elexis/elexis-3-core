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
package ch.elexis.core.data.propertyTester;

import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.model.IContact;

public class ContactIsDeleted extends PropertyTester {
	
	public ContactIsDeleted(){}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		IContact c = (IContact) receiver;
		if (c.isDeleted()) {
			return true;
		}
		return false;
	}
	
}
