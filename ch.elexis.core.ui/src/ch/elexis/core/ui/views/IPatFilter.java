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
package ch.elexis.core.ui.views;

import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;

/**
 * @since 3.0.0
 */
public interface IPatFilter {
	/** The Patient is not selected with the filter object */
	public static final int REJECT = -1;
	/** The Patient is selected with the filter objct */
	public static final int ACCEPT = 1;
	/** We do not handle this type of filter object */
	public static final int DONT_HANDLE = 0;
	/** We encountered an error while trying to filter */
	public static final int FILTER_FAULT = -2;
	
	/**
	 * Will the Patient be accepted for the Filter depending on the Object?
	 * 
	 * @param p
	 *            The Patient to consider
	 * @param o
	 *            The Object to check
	 * @return one of REJECT, ACCEPT, DONT_HANDLE
	 * @throws Exception
	 */
	public int accept(Patient p, PersistentObject o);
	
	public boolean aboutToStart(PersistentObject o);
	
	public boolean finished(PersistentObject o);
}
