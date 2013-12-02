/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.propertyTester;

import java.lang.reflect.Field;

import org.eclipse.core.expressions.PropertyTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;

public class ACETester extends PropertyTester {
	
	private static Logger log = LoggerFactory.getLogger(ACETester.class.getName());
	
	public ACETester(){}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("ACE".equals(property)) {
			if (args.length > 0) {
				String right = ((String) args[0]);
				try {
					Field ACEfield = AccessControlDefaults.class.getField(right);
					if (ACEfield.getType().equals(ACE.class)) {
						return CoreHub.acl.request((ACE) ACEfield.get(null));
					}
				} catch (SecurityException e) {
					log.error("Security Exception on right " + right, e);
					return false;
				} catch (NoSuchFieldException e) {
					log.error("NoSuchFieldException on right " + right, e);
					return false;
				} catch (IllegalArgumentException e) {
					log.error("IllegalArgumentException on right " + right, e);
					return false;
				} catch (IllegalAccessException e) {
					log.error("IllegalAccessException on right " + right, e);
					return false;
				}
			}
		}
		
		return false;
	}
	
}
