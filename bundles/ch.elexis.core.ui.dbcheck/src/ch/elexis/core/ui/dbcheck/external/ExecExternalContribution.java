/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/
package ch.elexis.core.ui.dbcheck.external;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import ch.elexis.core.ui.dbcheck.CheckExec;
import ch.elexis.data.PersistentObject;

public class ExecExternalContribution implements IRunnableWithProgress {
	
	ExternalMaintenance em;
	String output;
	
	public ExecExternalContribution(ExternalMaintenance o){
		this.em = o;
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
		InterruptedException{
		StringBuilder sb = new StringBuilder();
		CheckExec.setJDBCLink(PersistentObject.getConnection());
		try {
			sb.append(em.executeMaintenance(monitor, CheckExec.getDBVersion()));
		} catch (Exception e) {
			sb.append("------------ EXCEPTION ------------\n");
			sb.append("-- Please contact your sysadmin ---\n");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			sb.append(sw.toString());
		}
		this.output = sb.toString();
	}
	
	public String getOutput(){
		return output;
	}
}
