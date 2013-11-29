/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.selectors;

import java.util.List;

import ch.elexis.data.PersistentObject;

/**
 * Link an input or display field to the database
 * 
 * @author Gerry
 * 
 */
public interface IObjectLink<T extends PersistentObject> {
	
	public String getValueFromObject(T t, String fieldname);
	
	public void setValueToObject(T t, String fieldname);
	
	public List<T> getObjectsForValue(String fieldname, String value, boolean bMatchExact);
}
