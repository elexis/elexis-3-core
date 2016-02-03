/*******************************************************************************
 * Copyright (c) 2007-2015, G. Weirich and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - final implementation
 *    G. Weirich adapted for ACE and AutoAdapt
 * 	  M. Descher - adapted for RoleBasedAccessControl #2112
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import org.eclipse.jface.action.Action;

import ch.elexis.admin.ACE;
import ch.elexis.core.data.activator.CoreHub;

/**
 * Special class for actions requiring special access rights.
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
	
	public RestrictedAction(ACE necessaryRight){
		super();
		this.necessaryRight = necessaryRight;
		reflectRight();
	}
	
	public RestrictedAction(ACE necessaryRight, String text, int style){
		super(text, style);
		this.necessaryRight = necessaryRight;
		reflectRight();
	}
	
	public RestrictedAction(ACE necessaryRight, String text){
		super(text);
		this.necessaryRight = necessaryRight;
		reflectRight();
	}
	
	/**
	 * Sets the enabled status of this action according to the required right. Unchecks the action
	 * if the required right is not available.
	 */
	public void reflectRight(){
		setEnabled(CoreHub.acl.request(necessaryRight));
	}
	
	/**
	 * Checks the required access rights and then calls doRun().
	 */
	final public void run(){
		if (CoreHub.acl.request(necessaryRight)) {
			doRun();
		}
	}
	
	/**
	 * Called by RestrictedAction.run() after access rights check.
	 * 
	 * Classes extending RestrictedAction must implement this method instead of run().
	 */
	abstract public void doRun();
	
}
