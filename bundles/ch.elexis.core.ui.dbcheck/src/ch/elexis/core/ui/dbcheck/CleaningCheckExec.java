/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/
package ch.elexis.core.ui.dbcheck;

import ch.elexis.core.ui.dbcheck.cleaning.CleaningCheck;
import ch.elexis.core.ui.dbcheck.cleaning.CleaningCheckPGSQL;

public class CleaningCheckExec extends CheckExec {
	/**
	 * Execute cleaning scripts
	 * 
	 * @return
	 */
	public static String doCleaning(){
		
		if (sqlDriver.equalsIgnoreCase(MYSQL_DB)) {
			return "";
			
		}
		if (sqlDriver.equalsIgnoreCase(POSTG_DB)) {
			CleaningCheck sc = new CleaningCheckPGSQL();
			sc.cleanCoreTables(j);
			System.out.println(sc.getOutputLog());
			return sc.getErrorLog();
		}
		return "Nicht unterstützer Datenbanktyp; Unterstüztung derzeit für MySQL und PostgreSQL";
	}
}
