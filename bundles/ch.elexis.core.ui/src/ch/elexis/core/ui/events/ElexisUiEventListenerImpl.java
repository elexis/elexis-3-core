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

import org.eclipse.swt.widgets.Control;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.ui.UiDesk;

/**
 * Extends {@link ElexisEventListenerImpl} to realize UI events.
 * 
 * @since 3.0.0
 */
public class ElexisUiEventListenerImpl extends ElexisEventListenerImpl {
	
	public ElexisUiEventListenerImpl(Class<?> clazz){
		super(clazz);
	}
	
	public ElexisUiEventListenerImpl(Class<?> clazz, int mode){
		super(clazz, mode);
	}
	
	@Override
	public void catchElexisEvent(final ElexisEvent ev){
		if (!isStopped()) {
			UiDesk.asyncExec(new Runnable() {
				public void run(){
					if (performanceStatisticHandler != null) {
						performanceStatisticHandler.startCatchEvent(ev,
							ElexisUiEventListenerImpl.this);
					}
					runInUi(ev);
					if (performanceStatisticHandler != null) {
						performanceStatisticHandler.endCatchEvent(ev,
							ElexisUiEventListenerImpl.this);
					}
				}
			});
		}
	}
	
	/**
	 * Test if the control is not disposed and visible.
	 * 
	 * @param control
	 * @return
	 */
	protected boolean isActiveControl(Control control) {
		return control != null && !control.isDisposed() && control.isVisible();
	}
	
	/**
	 * to override
	 * 
	 * @param ev
	 */
	public void runInUi(ElexisEvent ev){}
}
