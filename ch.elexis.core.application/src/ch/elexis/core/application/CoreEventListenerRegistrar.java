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
package ch.elexis.core.application;

import ch.elexis.core.application.listeners.ElexisStatusEventEventListener;
import ch.elexis.core.application.listeners.MessageEventListener;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;

public class CoreEventListenerRegistrar {

	private ElexisEventListener eeli_messageEvent;
	private ElexisEventListener eeli_elexisEvent;
	
	public CoreEventListenerRegistrar() {
		eeli_messageEvent = new MessageEventListener();
		eeli_elexisEvent = new ElexisStatusEventEventListener();
				
		ElexisEventDispatcher.getInstance().addListeners(eeli_messageEvent, eeli_elexisEvent);
	}
}
