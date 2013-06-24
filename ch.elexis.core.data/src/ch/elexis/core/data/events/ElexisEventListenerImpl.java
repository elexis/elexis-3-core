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

import ch.elexis.core.data.PersistentObject;

/**
 * An implementation of the most common uses of ElexisSventListeners. Subclasses
 * must override one of catchElexisEvent (non ui thread) or runInUi (the event
 * is forwarded in an async UI thread)
 * 
 * @author gerry
 * 
 */
public class ElexisEventListenerImpl implements ElexisEventListener {
	private final ElexisEvent template;
	private boolean bStopped = false;

	public ElexisEventListenerImpl(final Class<?> clazz) {
		template = new ElexisEvent(null, clazz, ElexisEvent.EVENT_SELECTED
				| ElexisEvent.EVENT_DESELECTED);
	}

	public ElexisEventListenerImpl(final Class<?> clazz, int mode) {
		template = new ElexisEvent(null, clazz, mode);
	}

	public ElexisEventListenerImpl(final PersistentObject obj,
			final Class<?> clazz, final int mode) {
		template = new ElexisEvent(obj, clazz, mode);
	}

	/**
	 * @since 3.0.0
	 */
	public ElexisEventListenerImpl(Object object, @SuppressWarnings("rawtypes") Class class1,
			int eventNotification, int prioritySync) {
		template = new ElexisEvent(object, class1, eventNotification, prioritySync);
	}

	public ElexisEvent getElexisEventFilter() {
		return template;
	}

	/**
	 * This catches the Event from the EventDispatcher, which is in a Non-UI
	 * Thread by definition
	 */
	public void catchElexisEvent(final ElexisEvent ev) {
		if (!bStopped) {
			run(ev);
		}
	}

	/**
	 * This runs the event in an UI Thread
	 * 
	 * @param ev
	 */
	public void run(ElexisEvent ev) {
	}

	public void stop() {
		bStopped = true;
	}

	public void start() {
		bStopped = false;
	}

	public boolean isStopped() {
		return bStopped;
	}

}
