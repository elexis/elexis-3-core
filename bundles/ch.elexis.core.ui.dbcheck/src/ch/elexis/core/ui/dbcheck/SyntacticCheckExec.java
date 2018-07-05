/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/

package ch.elexis.core.ui.dbcheck;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.syntactic.SyntacticCheck;
import ch.elexis.core.ui.dbcheck.syntactic.SyntacticCheckMySQL;
import ch.elexis.core.ui.dbcheck.syntactic.SyntacticCheckPGSQL;

public class SyntacticCheckExec extends CheckExec {
	
	public static SyntacticCheck sc = null;
	
	/**
	 * Syntactic Check on the current state of the database
	 * 
	 * @param monitor
	 * 
	 * @return
	 */
	public static String doSyntacticCheckOffCore(IProgressMonitor monitor){
		
		if (sqlDriver.equalsIgnoreCase(MYSQL_DB)) {
			sc = new SyntacticCheckMySQL();
			sc.checkCoreTables(j, monitor);
			return sc.getErrorLog();
			
		}
		if (sqlDriver.equalsIgnoreCase(POSTG_DB)) {
			sc = new SyntacticCheckPGSQL();
			sc.checkCoreTables(j, monitor);
			return sc.getErrorLog();
		}
		return "Nicht unterstützer Datenbanktyp; Unterstüztung derzeit für MySQL und PostgreSQL";
	}
	
	public static String getOutputLog(){
		if (sc != null)
			return sc.getOutputLog();
		return "";
	}
	
	public static String getErrorLog(){
		if (sc != null)
			return sc.getErrorLog();
		return "";
	}
}
