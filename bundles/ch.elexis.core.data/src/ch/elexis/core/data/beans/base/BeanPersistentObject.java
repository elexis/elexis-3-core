/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.beans.base;

import org.eclipse.core.runtime.Assert;

public class BeanPersistentObject<E> extends BasePropertyChangeSupport {
	protected E entity;
	
	/**
	 * @param entity
	 *            the entity this property support is bound to
	 */
	public BeanPersistentObject(E entity){
		this.entity = entity;
		
		// TODO: Check if entity really exists
		Assert.isNotNull(entity);
	}
	
	public E getContainedEntity(){
		return entity;
	}
	
	@Override
	protected void updateCache(){
		// TODO Auto-generated method stub
	}
	
}
