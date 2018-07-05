/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/

package ch.elexis.core.ui.dbcheck;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.semantic.SemanticCheck;
import ch.elexis.core.ui.dbcheck.semantic.SemanticCheckMySQL;
import ch.elexis.core.ui.dbcheck.semantic.SemanticCheckPGSQL;

public class SemanticCheckExec extends CheckExec {
	
	public static SemanticCheck sc = null;
	
	/**
	 * Semantic Check on the current state of the database
	 * 
	 * @param monitor
	 * 
	 * @return
	 */
	public static String doSemanticCheckOffCore(IProgressMonitor monitor){
		
		if (sqlDriver.equalsIgnoreCase(MYSQL_DB)) {
			sc = new SemanticCheckMySQL();
			sc.checkSemanticStateCoreTables(j, monitor);
			return sc.getErrorLog();
		}
		if (sqlDriver.equalsIgnoreCase(POSTG_DB)) {
			sc = new SemanticCheckPGSQL();
			sc.checkSemanticStateCoreTables(j, monitor);
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
