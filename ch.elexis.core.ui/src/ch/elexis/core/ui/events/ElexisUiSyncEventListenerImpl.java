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
package ch.elexis.core.ui.events;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.ui.UiDesk;

/**
 * Extends {@link ElexisEventListenerImpl} to realize UI events. The events in this listener are
 * executed synchronous.
 * 
 * @since 3.2.0
 */
public class ElexisUiSyncEventListenerImpl extends ElexisEventListenerImpl {
	
	public ElexisUiSyncEventListenerImpl(Class<?> clazz){
		super(clazz);
	}
	
	public ElexisUiSyncEventListenerImpl(Class<?> clazz, int mode){
		super(clazz, mode);
	}
	
	@Override
	public void catchElexisEvent(final ElexisEvent ev){
		if (!isStopped()) {
			UiDesk.syncExec(new Runnable() {
				public void run(){
					runInUi(ev);
				}
			});
		}
	}
	
	/**
	 * to override
	 * 
	 * @param ev
	 */
	public void runInUi(ElexisEvent ev){}
}
