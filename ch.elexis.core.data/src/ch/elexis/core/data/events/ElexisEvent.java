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

import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.activator.CoreHub;

/**
 * A universal event object. Can optionally created with a priority
 * 
 * Note: Note: this class has a natural ordering that is inconsistent with equals
 * 
 * @author gerry
 * 
 */
public final class ElexisEvent implements Comparable<ElexisEvent> {
	/** The Object was newly created */
	public static final int EVENT_CREATE = 0x0001;
	/** The object is about to be deleted */
	public static final int EVENT_DELETE = 0x0002;
	/** The object has changed some of its properties */
	public static final int EVENT_UPDATE = 0x0004;
	/** All Objects of this class have been reloaded */
	public static final int EVENT_RELOAD = 0x0008;
	/** The Object has been selected */
	public static final int EVENT_SELECTED = 0x0010;
	/** All Objects of this type have been deselected */
	public static final int EVENT_DESELECTED = 0x0020;
	/** a user logged out or logged in */
	public static final int EVENT_USER_CHANGED = 0x0040;
	/** the mandator changed */
	public static final int EVENT_MANDATOR_CHANGED = 0x0080;
	/** a status event */
	public static final int EVENT_ELEXIS_STATUS = 0x0100;
	/** an operation progress event */
	public static final int EVENT_OPERATION_PROGRESS = 0x0200;
	/** notification event */
	public static final int EVENT_NOTIFICATION = 0x0400;

	/**
	 * highest priority, will not be put into the queue but directly
	 * fired.
	 */
	public static final int PRIORITY_SYNC = 1;
	public static final int PRIORITY_HIGH = 1000;
	public static final int PRIORITY_NORMAL = 10000;
	
	
	private final PersistentObject obj;
	private final Object genericObject;
	private final Class<?> objClass;
	final int type;
	private final int priority;
	
	/**
	 * Create an ElexisEvent with default priority
	 * 
	 * @param o
	 *            the PersistentObject that sources the event
	 * @param c
	 *            The object classs of the event source
	 * @param type
	 *            the type of Event. One of the EVENT_ constants
	 */
	public ElexisEvent(final PersistentObject o, final Class<?> c, final int type){
		this(o, c, type, PRIORITY_NORMAL);
	}
	
	/**
	 * Create an ElexisEvent with explicitely set priority
	 * 
	 * @param o
	 *            the PersistentObject that sources the event
	 * @param c
	 *            The object classs of the event source
	 * @param type
	 *            the type of Event. One of the EVENT_ constants
	 * @param priority
	 *            the priority for this event. One of the PRIORITY_ Constants or any other int
	 *            value. An Event will be fired before all other events with same or lower priority.
	 */
	public ElexisEvent(final PersistentObject o, Class<?> c, int type, int priority){
		obj = o;
		objClass = c;
		genericObject = null;
		this.type = type;
		this.priority = priority;
	}
	
	/**
	 * Create an {@link ElexisEvent} carrying a generic object. This event is
	 * of high priority and should not be lightly used. Usage examples are
	 * communication of ElexisStatus events within the system.
	 * 
	 * @param genericObject
	 * @param c
	 * @param type
	 * @pram priority
	 * @since 3.0.0
	 */
	public ElexisEvent(final Object genericObject, Class<?> c, int type, int priority) {
		obj = null;
		objClass = c;
		this.genericObject = genericObject;
		this.type = type;
		this.priority = priority;
	}
	
	/**
	 * Retrieve the object this event is about.
	 * 
	 * @return the object that might be null (if the event concern a class)
	 */
	public PersistentObject getObject(){
		return obj;
	}
	
	/**
	 * Retrieve the class this event is about
	 * 
	 * @return the class (that might be null)
	 */
	public Class<?> getObjectClass(){
		if (objClass == null) {
			if (obj != null) {
				return obj.getClass();
			}
		}
		return objClass;
	}
	
	/**
	 * @return the generic object, may be <code>null</code>
	 * @since 3.0.0
	 */
	public Object getGenericObject()  {
		return genericObject;
	}
	
	/**
	 * Retrieve the event type
	 * 
	 * @return one ore more of the oabove EVENT_xxx flags
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * Check whether this event matches a template event. this method is only used internally by the
	 * framework and not intended to be called or overridden by clients
	 * 
	 * @param event
	 *            the template
	 * @return true on match
	 */
	
	boolean matches(final ElexisEvent event){
		if (getObject() != null && event.getObject() != null) {
			if (!getObject().getId().equals(event.getObject().getId())) {
				return false;
			}
		}
		if (getObjectClass() != null && event.getObjectClass() != null) {
			if (!getObjectClass().equals(event.getObjectClass())) {
				return false;
			}
		}
		if (event.getType() != 0) {
			if ((type & event.getType()) == 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Decide whether an Event is logically identically to an other (i.e. describes the same
	 * operation on the same (type of) object)
	 * 
	 * @param other
	 *            the other event to compare
	 * @return true if both events are logically identical
	 */
	boolean isSame(ElexisEvent other){
		if (other == null) {
			return false;
		}
		if (other.obj == null) {
			if (this.obj == null) {
				if (other.objClass != null) {
					if (other.objClass.equals(this.objClass)) {
						if (other.type == this.type) {
							return true;
						}
					}
				}
			}
		} else {
			if (other.obj.equals(this.obj)) {
				if (other.type == this.type) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Shortcut to create a "User Changed" event
	 * 
	 * @return
	 */
	public static ElexisEvent createUserEvent(){
		return new ElexisEvent(CoreHub.actUser, Anwender.class, EVENT_USER_CHANGED);
	}
	
	/**
	 * Shortcut to create a "Patient changed" event
	 * 
	 * @return
	 */
	public static ElexisEvent createPatientEvent(){
		return new ElexisEvent(ElexisEventDispatcher.getSelectedPatient(), Patient.class,
			EVENT_SELECTED);
	}
	
	@Override
	public int compareTo(ElexisEvent o){
		return priority - o.priority;
	}
	
	/**
	 * @return
	 * @since 3.0.0
	 */
	public int getPriority() {
		return priority;
	}
}
