/*******************************************************************************
 * Copyright (c) 2007-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  MEDEVIT <office@medevit.at> - changed to reflective rights parsing
 *******************************************************************************/
package ch.elexis.admin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contribution of the basic system's ACLs
 * 
 * @author gerry
 * @since 3.1 changed behaviour to fetch access rights in {@link AccessControlDefaults} via reflection
 */
public class ACLContributor implements IACLContributor {
	private static Logger log = LoggerFactory.getLogger(ACLContributor.class);
	
	public ACE[] getACL(){
		try {
			return findAllRightsThroughReflection();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			log.error("Error reflecting access rights", e);
			return null;
		}
	}
	
	public ACE[] reject(final ACE[] acl){
		// TODO Management of collisions
		return null;
	}
	
	/**
	 * we collect all access rights as defined in {@link AccessControlDefaults} via reflection
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @since 3.1
	 */
	private ACE[] findAllRightsThroughReflection() throws IllegalArgumentException,
		IllegalAccessException{
		List<ACE> list = new ArrayList<ACE>();
		Field[] declaredFields = AccessControlDefaults.class.getFields();
		for (Field field : declaredFields) {
			int modifiers = field.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
				&& Modifier.isFinal(modifiers)) {
				if (field.getType().equals(ACE.class)) {
					list.add((ACE) field.get(null));
				}
			}
		}
		return list.toArray(new ACE[list.size()]);
	}
}
