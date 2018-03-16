/*******************************************************************************
 * Copyright (c) 2009-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  MEDEVIT <office@medevit.at> - major changes in 3.0
 *******************************************************************************/

package ch.elexis.core.data.events;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.server.ServerEventMapper;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Anwender;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;

/**
 * The Elexis event dispatcher system manages and distributes the information of changing, creating,
 * deleting and selecting PersistentObjects. An event is fired when such an action occures. This
 * might be due to a user interaction or to an non-interactive job.
 * 
 * A view that handles user selection of PersistentObjects MUST fire an appropriate Event through
 * ElexisEventdispatcher.getinstance().fire(ElexisEvent ee) Notification of deletion, modification
 * and creation of PeristentObjects occurs transparently via the PersistentObject base class.
 * 
 * A client that wishes to be informed on such events must register an ElexisEventListener. The
 * catchElexisEvent() Method of this listener is called in a non-UI-thread an should be finished as
 * fast as possible. If lengthy operations are neccessary, these must be sheduled in a separate
 * thread, The Listener can specify objects, classes and event types it wants to be informed. If no
 * such filter is given, it will be informed about all events.
 * 
 * @since 3.0.0 major changes, switch to {@link ElexisContext}
 * @since 3.4 switched listeners to {@link ListenerList}
 */
public final class ElexisEventDispatcher extends Job {
	private static Logger log = LoggerFactory.getLogger(ElexisEventDispatcher.class);
	
	private final ListenerList<ElexisEventListener> listeners;
	private static ElexisEventDispatcher theInstance;
	private final PriorityQueue<ElexisEvent> eventQueue;
	private final ArrayList<ElexisEvent> eventCopy;
	private transient boolean bStop = false;
	
	private final ElexisContext elexisUIContext;
	
	private List<Integer> blockEventTypes;
	
	private volatile IPerformanceStatisticHandler performanceStatisticHandler;
	
	public static synchronized ElexisEventDispatcher getInstance(){
		if (theInstance == null) {
			theInstance = new ElexisEventDispatcher();
			theInstance.schedule();
		}
		return theInstance;
	}
	
	private ElexisEventDispatcher(){
		super(ElexisEventDispatcher.class.getName());
		setSystem(true);
		setUser(false);
		setPriority(Job.SHORT);
		listeners = new ListenerList<ElexisEventListener>();
		eventQueue = new PriorityQueue<ElexisEvent>(50);
		eventCopy = new ArrayList<ElexisEvent>(50);
		
		elexisUIContext = new ElexisContext();
	}
	
	/**
	 * Add listeners for ElexisEvents. The listener tells the system via its getElexisEventFilter
	 * method, what classes it will catch. If a dispatcher for that class was registered, the call
	 * will be routed to that dispatcher.
	 * 
	 * @param el
	 *            one ore more ElexisEventListeners that have to return valid values on
	 *            el.getElexisEventFilter()
	 */
	public void addListeners(final ElexisEventListener... els){
		for (ElexisEventListener el : els) {
			listeners.add(el);
		}
	}
	
	/**
	 * remove listeners. If a listener was added before, it will be removed. Otherwise nothing will
	 * happen
	 * 
	 * @param el
	 *            The Listener to remove
	 */
	public void removeListeners(ElexisEventListener... els){
		for (ElexisEventListener el : els) {
			listeners.remove(el);
		}
	}
	
	/**
	 * 
	 * @param me
	 * @since 3.0.0
	 */
	public void fireMessageEvent(MessageEvent me){
		ElexisEvent ev =
			new ElexisEvent(me, MessageEvent.class, ElexisEvent.EVENT_NOTIFICATION,
				ElexisEvent.PRIORITY_SYNC);
		fire(ev);
	}
	
	/**
	 * Set a list of {@link ElexisEvent} types that will be skipped when fired. Is used for example
	 * on import when many new objects are created, and we are not interest in calling the event
	 * listeners. Set to null to reset.
	 * 
	 * @param blockEventTypes
	 */
	public synchronized void setBlockEventTypes(List<Integer> blockEventTypes){
		this.blockEventTypes = blockEventTypes;
	}
	
	/**
	 * Fire an ElexisEvent. The class concerned is named in ee.getObjectClass. If a dispatcher for
	 * that class was registered, the event will be forwarded to that dispatcher. Otherwise, it will
	 * be sent to all registered listeners. The call to the dispatcher or the listener will always
	 * be in a separate thread and not in the UI thread.So care has to be taken if the callee has to
	 * change the UI Note: Only one Event is dispatched at a given time. If more events arrive, they
	 * will be pushed into a FIFO-Queue. If more than one equivalent event is pushed into the queue,
	 * only the last entered will be dispatched.
	 * 
	 * @param ee
	 *            the event to fire.
	 */
	public void fire(final ElexisEvent... ees){
		for (ElexisEvent ee : ees) {
			if (blockEventTypes != null && blockEventTypes.contains(ee.getType())) {
				continue;
			}
			// Those are single events
			if (ee.getPriority() == ElexisEvent.PRIORITY_SYNC
				&& ee.getType() != ElexisEvent.EVENT_SELECTED) {
				doDispatch(ee);
				continue;
			}
			
			int eventType = ee.getType();
			if (eventType == ElexisEvent.EVENT_SELECTED
				|| eventType == ElexisEvent.EVENT_DESELECTED) {
				
				List<ElexisEvent> eventsToThrow = null;
				eventsToThrow =
					elexisUIContext.setSelection(ee.getObjectClass(),
						(eventType == ElexisEvent.EVENT_SELECTED) ? ee.getObject() : null);
				
				for (ElexisEvent elexisEvent : eventsToThrow) {
					synchronized (eventQueue) {
						eventQueue.offer(elexisEvent);
					}
					
				}
				continue;
			} else if (eventType == ElexisEvent.EVENT_MANDATOR_CHANGED) {
				elexisUIContext.setSelection(Mandant.class, ee.getObject());
			} else if (eventType == ElexisEvent.EVENT_USER_CHANGED) {
				elexisUIContext.setSelection(Anwender.class, ee.getObject());
			}
			
			synchronized (eventQueue) {
				eventQueue.offer(ee);
			}
			
			if (CoreHub.getElexisServerEventService().deliversRemoteEvents()) {
				ch.elexis.core.common.ElexisEvent mapEvent = ServerEventMapper.mapEvent(ee);
				if(mapEvent!=null) {
					CoreHub.getElexisServerEventService().postEvent(mapEvent);
				}
			}
		}
	}
	
	/**
	 * Synchronously fire an {@link ElexisStatus} event.
	 * 
	 * @param es
	 *            an {@link ElexisStatus} describing the problem
	 * @since 3.0.0
	 */
	public static void fireElexisStatusEvent(ElexisStatus es){
		ElexisEvent statusEvent =
			new ElexisEvent(es, ElexisStatus.class, ElexisEvent.EVENT_ELEXIS_STATUS,
				ElexisEvent.PRIORITY_SYNC);
		getInstance().doDispatch(statusEvent);
	}
	
	/**
	 * find the last selected object of a given type
	 * 
	 * @param template
	 *            tha class defining the object to find
	 * @return the last object of the given type or null if no such object is selected
	 */
	public static IPersistentObject getSelected(final Class<?> template){
		return getInstance().elexisUIContext.getSelected(template);
	}
	
	/**
	 * inform the system that an object has been selected
	 * 
	 * @param po
	 *            the object that is selected now
	 */
	public static void fireSelectionEvent(PersistentObject po){
		if (po != null) {
			getInstance().fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_SELECTED));
		}
	}
	
	/**
	 * inform the system, that several objects have been selected
	 * 
	 * @param objects
	 */
	public static void fireSelectionEvents(PersistentObject... objects){
		if (objects != null) {
			ElexisEvent[] ees = new ElexisEvent[objects.length];
			for (int i = 0; i < objects.length; i++) {
				ees[i] =
					new ElexisEvent(objects[i], objects[i].getClass(), ElexisEvent.EVENT_SELECTED);
			}
			getInstance().fire(ees);
		}
	}
	
	/**
	 * inform the system, that no object of the specified type is selected anymore
	 * 
	 * @param clazz
	 *            the class of which selection was removed
	 */
	public static void clearSelection(Class<?> clazz){
		if (clazz != null) {
			getInstance().fire(new ElexisEvent(null, clazz, ElexisEvent.EVENT_DESELECTED));
		}
	}
	
	/**
	 * inform the system, that all object of a specified class have to be reloaded from storage
	 * 
	 * @param clazz
	 *            the clazz whose objects are invalidated
	 */
	public static void reload(Class<?> clazz){
		if (clazz != null) {
			getInstance().fire(new ElexisEvent(null, clazz, ElexisEvent.EVENT_RELOAD));
		}
	}
	
	/**
	 * inform the system, that the specified object has changed some values or properties
	 * 
	 * @param po
	 *            the object that was modified
	 */
	public static void update(PersistentObject po){
		if (po != null) {
			getInstance().fire(new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_UPDATE));
		}
	}
	
	/**
	 * @return the currently selected {@link Patient}
	 */
	public @Nullable static Patient getSelectedPatient(){
		return (Patient) getSelected(Patient.class);
	}
	
	/**
	 * 
	 * @return the currently selected {@link Mandant}
	 * @since 3.1
	 */
	public @Nullable static Mandant getSelectedMandator() {
		return (Mandant) getSelected(Mandant.class);
	}
	
	/**
	 * Cancel rescheduling of the EventDispatcher. Events already in the event queue will still be
	 * dispatched.
	 */
	public void shutDown(){
		bStop = true;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor){
		// copy all events so doDispatch is outside of synchronization,
		// and event handling can run code in display thread without deadlock
		synchronized (eventQueue) {
			while (!eventQueue.isEmpty()) {
				eventCopy.add(eventQueue.poll());
			}
			eventQueue.notifyAll();
		}
		
		for (ElexisEvent event : eventCopy) {
			doDispatch(event);
		}
		eventCopy.clear();
		
		if (!bStop) {
			this.schedule(30);
		}
		return Status.OK_STATUS;
	}
	
	private void doDispatch(final ElexisEvent ee){
		if (ee != null) {
			for (ElexisEventListener l : listeners) {
				if (ee.matches(l.getElexisEventFilter())) {
					// handle performance statistics if necessary
					if (performanceStatisticHandler != null) {
						startStatistics(ee, l);
					}
					
					try {
						l.catchElexisEvent(ee);
					} catch (Exception e) {
						log.error(ee.toString(), e);
						throw e;
					}
					
					// handle performance statistics if necessary
					if (performanceStatisticHandler != null) {
						endStatistics(ee, l);
					}
				}
			}
		}
	}
	
	private void endStatistics(ElexisEvent ee, ElexisEventListener l){
		if (!(l instanceof ElexisEventListenerImpl)) {
			performanceStatisticHandler.endCatchEvent(ee, l);
		}
	}
	
	private void startStatistics(ElexisEvent ee, ElexisEventListener l){
		if (l instanceof ElexisEventListenerImpl) {
			((ElexisEventListenerImpl) l)
				.setPerformanceStatisticHandler(performanceStatisticHandler);
		} else {
			performanceStatisticHandler.startCatchEvent(ee, l);
		}
	}
	
	/**
	 * Let the dispatcher Thread empty the queue. If the queue is empty, this method returns
	 * immediately. Otherwise, the current thread waits until it is empty or the provided wasit time
	 * has expired.
	 * 
	 * @param millis
	 *            The time to wait bevor returning
	 * @return false if waiting was interrupted
	 */
	public boolean waitUntilEventQueueIsEmpty(long millis){
		synchronized (eventQueue) {
			if (!eventQueue.isEmpty()) {
				try {
					eventQueue.wait(millis);
					return true;
				} catch (InterruptedException e) {
					// janusode
				}
			}
		}
		return false;
	}
	
	public void dump(){
		StringBuilder sb = new StringBuilder();
		sb.append("ElexisEventDispatcher dump: \n");
		for (ElexisEventListener el : listeners) {
			ElexisEvent filter = el.getElexisEventFilter();
			sb.append(el.getClass().getName()).append(": ");
			if (filter != null && filter.getObjectClass() != null
				&& filter.getObjectClass().getName() != null) {
				sb.append(filter.type).append(" / ").append(filter.getObjectClass().getName());
			}
			sb.append("\n");
			
		}
		sb.append("\n--------------\n");
		log.debug(sb.toString());
	}
	
	/**
	 * Method to set a {@link IPerformanceStatisticHandler} implementation. Setting null, will
	 * disable calling the statistic handler.
	 * 
	 * @param handler
	 */
	public void setPerformanceStatisticHandler(IPerformanceStatisticHandler handler){
		this.performanceStatisticHandler = handler;
	}
	
	/**
	 * Statistics handler interface.
	 * 
	 */
	public interface IPerformanceStatisticHandler {
		void startCatchEvent(ElexisEvent ee, ElexisEventListener listener);
		
		void endCatchEvent(ElexisEvent ee, ElexisEventListener listener);
	}
}
