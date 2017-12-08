/*******************************************************************************
 * Copyright (c) 2007-2010, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import ch.elexis.data.PersistentObject;

/**
 * An ICodeSelectorTarget can receive objects from CodeSelector lists when they are selected.
 * GlobalEvents allows to register exactly one ICodeSelectorTarget at a time. This one receives the
 * selected target.
 * 
 * A ICodeSelectorTarget should register itself to GlobalEvents and then activate the appropriate
 * view for code selection. It should unregister itself as soon as possible. Since the code
 * selection itself is an asynchronous process, it's difficult to say which is the right place for
 * this. A good idea would be when the view is hidden and when the view gets the focus.
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 */
public interface ICodeSelectorTarget {
	/**
	 * Return a human readable name of the target, e. g. the name of a View. This name is presented
	 * to the user.
	 * 
	 * @return the name of the target
	 */
	public String getName();
	
	/**
	 * Callback to send the selected object to the ICodeSelectorTarget.
	 * 
	 * @param obj
	 *            the selected object
	 */
	public void codeSelected(PersistentObject obj);
	
	/**
	 * Callback to tell the ICodeSelectorTarget that it has been registered or unregistered. The
	 * receiver should highlight the corresponding area accordingly.
	 * 
	 */
	public void registered(boolean registered);
}
