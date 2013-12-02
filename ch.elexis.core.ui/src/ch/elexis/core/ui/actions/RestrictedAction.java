/*******************************************************************************
 * Copyright (c) 2007-2010, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - final implementation
 *    G. Weirich adapted for ACE and AutoAdapt
 * 
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;

import ch.elexis.admin.ACE;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Anwender;

/**
 * Special class for actiosn requiring special access rights.
 * 
 * The run() method of this class checks the required access rights and runs doRun() if access is
 * granted.
 * 
 * Classes extending this class must implement the method doRun() instead of run().
 * 
 * Users of this class may register a listener to get informed about missing rights during execution
 * of the action. (Usually, this is not required, because the action is disabled if the user has not
 * the required rights. See setEnabled())
 * 
 */
abstract public class RestrictedAction extends Action {
	protected ACE necessaryRight;
	private final List<RestrictionListener> listeners = new ArrayList<RestrictionListener>();
	private static Pool pool = new Pool();
	
	/**
	 * If AutoAdapt is enabled, the Action will reflect is visual representation according to the
	 * current user's rights automatically. If AutoAdapt is enabled, then disableAutoAdapt MUST be
	 * called on Disposal
	 */
	public void enableAutoAdapt(){
		pool.add(this);
		reflectRight();
	}
	
	/**
	 * Remove this RestrcitedAction from the AutoAdapt queue
	 */
	public void disableAutoAdapt(){
		pool.remove(this);
	}
	
	public RestrictedAction(ACE necessaryRight){
		this.necessaryRight = necessaryRight;
	}
	
	public RestrictedAction(ACE necessaryRight, String text, int style){
		super(text, style);
		this.necessaryRight = necessaryRight;
	}
	
	public RestrictedAction(ACE necessaryRight, String text){
		super(text);
		this.necessaryRight = necessaryRight;
	}
	
	/**
	 * Sets the enabled status of this action according to the required right. Unchecks the action
	 * if the required right is not available.
	 */
	public void reflectRight(){
		if (CoreHub.acl.request(necessaryRight)) {
			setEnabled(true);
		} else {
			setEnabled(false);
			// setChecked(false);
		}
	}
	
	/**
	 * Checks the required access rights and then calls doRun().
	 */
	public void run(){
		if (CoreHub.acl.request(necessaryRight)) {
			doRun();
		} else {
			RestrictionEvent event = new RestrictionEvent(necessaryRight);
			fireRestrictionEvent(event);
		}
	}
	
	private void fireRestrictionEvent(RestrictionEvent event){
		for (RestrictionListener listener : listeners) {
			listener.restricted(event);
		}
	}
	
	/**
	 * Called by RestrictedAction.run() after access rights check.
	 * 
	 * Classes extending RestrictedAction must implement this method instead of run().
	 */
	abstract public void doRun();
	
	/**
	 * Register a listener to get informed about missing rights.
	 * 
	 * @param listener
	 *            the listener to register.
	 */
	public void addRestrictionListener(RestrictionListener listener){
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a previously registered listener.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removeRestrictionListener(RestrictionListener listener){
		listeners.remove(listener);
	}
	
	/**
	 * Users of this class can register a RestrictionListener to get informed about missing rights
	 * during execution of the action.
	 * 
	 * @author danlutz
	 */
	public interface RestrictionListener {
		/**
		 * The action has been executed with missing rights.
		 * 
		 * @param event
		 *            a RestrictionEvent containing the required right.
		 */
		public void restricted(RestrictionEvent event);
	}
	
	/**
	 * Event containing the required right.
	 * 
	 * @author danlutz
	 */
	public static class RestrictionEvent {
		public ACE necessaryRight;
		
		public RestrictionEvent(ACE necessaryRight){
			this.necessaryRight = necessaryRight;
		}
	}
	
	static class Pool extends ArrayList<RestrictedAction> implements ElexisEventListener {
		private final ElexisEvent eetmpl = new ElexisEvent(null, Anwender.class,
			ElexisEvent.EVENT_USER_CHANGED);
		
		Pool(){
			ElexisEventDispatcher.getInstance().addListeners(this);
		}
		
		public void catchElexisEvent(ElexisEvent ev){
			final ArrayList<RestrictedAction> copy = new ArrayList<RestrictedAction>(this);
			System.out.println("ElexisEvent " + ev.getType() + " " + ev.getGenericObject());
			UiDesk.asyncExec(new Runnable() {
				public void run(){
					for (RestrictedAction ra : copy) {
						ra.reflectRight();
					}
				}
			});
		}
		
		public ElexisEvent getElexisEventFilter(){
			return eetmpl;
		}
	}
}
