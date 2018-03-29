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
package ch.elexis.core.console.application.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.interfaces.events.MessageEvent;

/**
 * Listens to generic MessageEvents thrown by the core that should be presented to the user.
 */
public class MessageEventListener extends ElexisEventListenerImpl {
	
	private Logger log = LoggerFactory.getLogger(MessageEventListener.class.getName());
	
	public MessageEventListener(){
		super(null, MessageEvent.class, ElexisEvent.EVENT_NOTIFICATION, ElexisEvent.PRIORITY_SYNC);
	}
	
	@Override
	public void run(ElexisEvent ev){
		MessageEvent me = (MessageEvent) ev.getGenericObject();
		log.debug("MessageEvent [" + me.mt + "]  [" + me.title + "] [" + me.message + "]");
	}
}
