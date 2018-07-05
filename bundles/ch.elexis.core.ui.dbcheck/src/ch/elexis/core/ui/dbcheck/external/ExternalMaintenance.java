/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/
package ch.elexis.core.ui.dbcheck.external;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class ExternalMaintenance {
	
	/**
	 * Execute the respective maintenance task and provide information to the user
	 * 
	 * @param pm
	 *            the Progress Monitor, please provide the user some information about the
	 *            happenings
	 * @param DBVersion
	 *            the version string of the current database
	 * @return a String of the output you created, will be shown within the text field
	 */
	public abstract String executeMaintenance(IProgressMonitor pm, String DBVersion);
	
	/**
	 * Label shown in the combo viewer; this describes the maintenance task to the user
	 * 
	 * @return String containing a short description of the executed task
	 */
	public abstract String getMaintenanceDescription();
}