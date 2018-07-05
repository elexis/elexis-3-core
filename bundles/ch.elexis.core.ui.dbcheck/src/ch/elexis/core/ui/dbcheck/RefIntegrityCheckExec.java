package ch.elexis.core.ui.dbcheck;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.refintegrity.RefIntegrityCheck;
import ch.elexis.core.ui.dbcheck.refintegrity.RefIntegrityCheckMySQL;
import ch.elexis.core.ui.dbcheck.refintegrity.RefIntegrityCheckPGSQL;

public class RefIntegrityCheckExec extends CheckExec {
	
	public static RefIntegrityCheck ric = null;
	
	/**
	 * Referential Integrity Check on the current state of the database
	 * 
	 * @param monitor
	 * 
	 * @return
	 */
	public static String doReferentialIntegrityCheckOffCore(IProgressMonitor monitor){
		
		if (sqlDriver.equalsIgnoreCase(MYSQL_DB)) {
			ric = new RefIntegrityCheckMySQL();
			ric.checkReferentialIntegrityStateCoreTables(j, monitor);
			System.out.println(ric.getOutputLog());
			return ric.getErrorLog();
		}
		if (sqlDriver.equalsIgnoreCase(POSTG_DB)) {
			ric = new RefIntegrityCheckPGSQL();
			ric.checkReferentialIntegrityStateCoreTables(j, monitor);
			System.out.println(ric.getOutputLog());
			return ric.getErrorLog();
		}
		return "Nicht unterstützer Datenbanktyp; Unterstüztung derzeit für MySQL und PostgreSQL";
	}
	
	public static String getOutputLog(){
		if (ric != null)
			return ric.getOutputLog();
		return "";
	}
	
	public static String getErrorLog(){
		if (ric != null)
			return ric.getErrorLog();
		return "";
	}
}
