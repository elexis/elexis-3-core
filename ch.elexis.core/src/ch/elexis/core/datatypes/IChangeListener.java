/********************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.datatypes;

/**
 * The IChangeListener will be informed whenever the bound property changes its value or when the
 * Object gets disposed.
 * 
 * @author gerry
 * 
 */
public interface IChangeListener {
	/**
	 * Value of the bound property is changing. Note: It is not defined whether the change happened
	 * already or is about to happen.
	 * 
	 * @param object
	 *            Object whose property changes
	 * @param field
	 *            name of the changing property
	 * @param oldValue
	 *            previous value of the property
	 * @param newValue
	 *            new value of the property
	 */
	public void valueChanged(IPersistentObject object, String field, Object oldValue,
		Object newValue);
	
	/**
	 * The tracked Object gets disposed. Disposal will happen immediately after this method returns.
	 * 
	 * @param object
	 *            The object that is about to dispose
	 */
	public void objectDisposing(IPersistentObject object);
}
