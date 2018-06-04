package ch.elexis.core.ui.dbcheck.syntactic;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.ui.dbcheck.Activator;
import ch.elexis.core.ui.dbcheck.CheckExec;
import ch.elexis.core.ui.dbcheck.model.DBModel;
import ch.elexis.core.ui.dbcheck.model.TableDescriptor;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.JdbcLinkSyntaxException;
import ch.rgw.tools.Log;

public class SyntacticCheckMySQL extends SyntacticCheck {
	
	private static Log logger = Log.get(SyntacticCheckMySQL.class.getName());
	
	public SyntacticCheckMySQL(){
		oklog = new StringBuilder();
		errlog = new StringBuilder();
		fixScript = new StringBuilder();
	}
	
	/**
	 * 
	 * @param j
	 * @return
	 */
	public String checkCoreTables(JdbcLink j, IProgressMonitor monitor){
		String version = CheckExec.getDBVersion();
		String[] tables = DBModel.getTableModel(version);
		
		try {
			// Collation test
			Stm stmColl = j.getStatement();
			for (int l = 0; l < tables.length; l++) {
				String status = "Überprüfe textvergleich (collation) auf Tabelle " + tables[l];
				ResultSet rsColl = null;
				
				oklog.append(tables[l] + ": Prüfe textvergleich (collation) ...");
				
				try {
					rsColl =
						stmColl
							.query("SELECT table_collation FROM information_schema.tables WHERE UPPER(table_name) = \""
								+ tables[l].toUpperCase() + "\"");
				} catch (JdbcLinkSyntaxException je) {
					errlog.append(tables[l] + ": SynErr: Tabelle nicht gefunden!\n");
					continue;
				}
				if (!rsColl.next()) {
					errlog.append(tables[l] + " Error in selecting table_collation\n");
					continue;
				}
				String collation = rsColl.getString(1);
				if (!collation.equalsIgnoreCase("utf8_general_ci")) {
					oklog.append(" " + collation + " inkorrekt, erwarte utf8_general_ci\n");
					errlog.append(tables[l] + ": Collation " + collation
						+ " inkorrekt, erwarte utf8_general_ci\n");
					fixScript.append("ALTER TABLE " + tables[l]
						+ " CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;\n");
				} else {
					oklog.append(" OK\n");
				}
			}
			j.releaseStatement(stmColl);
			
			// Iterate Tables syntactic check
			for (int i = 0; i < tables.length; i++) {
				String status = "Überprüfe Tabelle " + tables[i];
				if (monitor != null)
					monitor.subTask(status);
				oklog.append(status + ":\n");
				
				// Iterate Fields in Tables
				TableDescriptor tableDetail = DBModel.getTableDescription(tables[i]);
				String[] fields = tableDetail.getFields(version);
				String[] fieldType = tableDetail.getFieldTypes(version);
				
				// Data types test
				for (int k = 0; k < fields.length; k++) {
					boolean ok = false;
					
					oklog.append(tables[i] + ": Erwarte " + fields[k] + " " + fieldType[k] + "...");
					
					Stm stm = j.getStatement();
					ResultSet rs = null;
					
					try {
						rs = stm.query("DESCRIBE " + tables[i].toLowerCase() + " " + fields[k]);
						
					} catch (JdbcLinkSyntaxException je) {
						// We have no lowercase here, but uppercase!
						try {
							rs = stm.query("DESCRIBE " + tables[i] + " " + fields[k]);
						} catch (JdbcLinkSyntaxException je2) {
							// We still did not find the table, assume its missing!
							continue;
						}
					}
					
					while (rs.next()) {
						ok = true;
						if (rs.getString(1).equalsIgnoreCase(fields[k])
							&& isCompatible(rs.getString(2), fieldType[k])) {
							oklog.append(" OK\n");
						} else {
							oklog.append(" erhalte " + rs.getString(1) + " " + rs.getString(2)
								+ "\n");
							errlog.append(tables[i] + ": SynErr: FeldTyp " + rs.getString(1) + " "
								+ rs.getString(2) + " inkorrekt, erwarte " + fields[k] + " "
								+ fieldType[k] + "\n");
							fixScript.append("ALTER TABLE " + tables[i] + " MODIFY " + fields[k]
								+ " " + fieldType[k] + ";\n");
						}
						
					}
					if (!ok) {
						oklog.append(" not found\n");
						errlog.append(tables[i] + ": SynErr: Feld " + fields[k] + " "
							+ fieldType[k] + " nicht gefunden!\n");
						fixScript.append("ALTER TABLE " + tables[i] + " ADD " + fields[k] + " "
							+ fieldType[k]);
					}
					
					j.releaseStatement(stm);
					rs.close();
				}
			}
		} catch (SQLException e) {
			Status status =
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
			StatusManager.getManager().handle(status, StatusManager.BLOCK);
		}
		
		String fixScriptOut = fixScript.toString();
		if (fixScriptOut.length() > 0) {
			logger.log("\n================ Database Check Tool ====================\n"
				+ "-- MySQL fix script -- apply on your database! ----------\n"
				+ "-- K E I N E GARANTIE, CODE VOR ANWENDUNG PRÜFEN --------\n"
				+ "================== START OF DATA ========================\n" + fixScriptOut
				+ "==================== END OF DATA ========================\n", Log.ERRORS);
		}
		
		return oklog.toString();
		
	}
	
}
