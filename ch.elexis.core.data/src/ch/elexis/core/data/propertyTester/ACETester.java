/*******************************************************************************
 * Copyright (c) 2007-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 *     M. Descher - adapted to RBAC
 ******************************************************************************/
package ch.elexis.core.data.propertyTester;

import org.eclipse.core.expressions.PropertyTester;

import ch.elexis.core.data.activator.CoreHub;

public class ACETester extends PropertyTester {
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("ACE".equals(property)) {
			if (args.length > 0) {
				String right = (String) args[0];
				return CoreHub.acl.request(right);
			}
		}
		
		return false;
	}
	
}
