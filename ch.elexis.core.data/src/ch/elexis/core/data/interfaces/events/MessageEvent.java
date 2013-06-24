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
package ch.elexis.core.data.interfaces.events;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.data.events.ElexisEventDispatcher;

/**
 * 
 * @since 3.0.0
 */
public class MessageEvent {

	public enum MessageType {
		INFO, WARN, ERROR
	}

	final public String title;
	final public String message;
	final public MessageType mt;
	final public IStatus status;

	public MessageEvent(MessageType mt, String title, String message) {
		this(mt, title, message, null);
	}

	public MessageEvent(MessageType mt, String title, String message,
			IStatus status) {
		this.title = title;
		this.message = message;
		this.mt = mt;
		this.status = status;
	}

	/**
	 * Fire this message
	 */
	public void fire() {
		ElexisEventDispatcher.getInstance().fireMessageEvent(this);
	}

	public static void fireError(String title, String message) {
		fire(MessageType.ERROR, title, message, null, null, false);
	}

	public static void fireError(String title, String message, Exception ex) {
		fire(MessageType.ERROR, title, message, null, ex, false);
	}
	
	public static void fireLoggedError(String title, String message) {
		fire(MessageType.ERROR, title, message, null, null, true);		
	}

	public static void fireLoggedError(String title, String message, Exception ex) {
		fire(MessageType.ERROR, title, message, null, ex, true);
	}
	
	public static void fireInformation(String title, String message) {
		fire(MessageType.INFO, title, message, null, null, true);
	}

	
	private static void fire(MessageType mt, String title, String message, IStatus status, Exception ex, boolean log) {
		if(log) {
			// get the callers class name and log under this name
		}
		new MessageEvent(mt, title, message, status).fire();	
	}
}
