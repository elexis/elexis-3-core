/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.data.events;


public interface ElexisEventListener {
	/**
	 * An Event was fired
	 * 
	 * @param ev
	 *            the Event
	 */
	public void catchElexisEvent(final ElexisEvent ev);
	
	/**
	 * Filter the events this listener wants to be informed. Note: This call should complete as fast
	 * as possible, because it will be called with every event before dispatching
	 * 
	 * @return An ElexisEvent with matching
	 *         <ul>
	 *         <li>object: Only events of this object will be sent</li>
	 *         <li>class: Only events of this class will be sent</li>
	 *         <li>type: Only eevnts matching to one ore more flags in type will be sent</li>
	 *         </ul>
	 */
	public ElexisEvent getElexisEventFilter();
}
