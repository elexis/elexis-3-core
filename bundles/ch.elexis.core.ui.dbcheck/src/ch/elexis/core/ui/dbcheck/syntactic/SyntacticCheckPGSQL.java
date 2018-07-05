package ch.elexis.core.ui.dbcheck.syntactic;

import java.sql.Connection;
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

public class SyntacticCheckPGSQL extends SyntacticCheck {
	public SyntacticCheckPGSQL(){
		oklog = new StringBuilder();
		errlog = new StringBuilder();
	}
	
	/**
	 * 
	 * @param j
	 * @return
	 */
	public String checkCoreTables(JdbcLink j, IProgressMonitor monitor){
		String version = CheckExec.getDBVersion();
		String[] tables = DBModel.getTableModel(version);
		Connection conn = j.getConnection();
		
		try {
			// Iterate Tables
			for (int i = 0; i < tables.length; i++) {
				String status = "Überprüfe Tabelle " + tables[i];
				if (monitor != null)
					monitor.subTask(status);
				oklog.append(status + ":\n");
				
				// Iterate Fields in Tables
				TableDescriptor tableDetail = DBModel.getTableDescription(tables[i]);
				String[] fields = tableDetail.getFields(version);
				String[] fieldType = tableDetail.getFieldTypes(version);
				for (int k = 0; k < fields.length; k++) {
					boolean ok = false;
					oklog.append(tables[i] + ": Erwarte " + fields[k] + " " + fieldType[k] + "...");
					
					ResultSet rs =
						conn.getMetaData().getColumns(conn.getCatalog(), "%",
							tables[i].toLowerCase(), fields[k].toLowerCase());
					while (rs.next()) {
						ok = true;
						String dataType = rs.getString(6) + "(" + rs.getString(7) + ")";
						if (rs.getString(4).equalsIgnoreCase(fields[k])
							&& isCompatible(dataType, fieldType[k])) {
							oklog.append(" OK\n");
						} else {
							oklog.append(" erhalte " + rs.getString(4) + " " + dataType + "\n");
							errlog.append(tables[i] + ": SynErr: FeldTyp " + rs.getString(4) + " "
								+ dataType + " inkorrekt, erwarte " + fields[k] + " "
								+ fieldType[k] + "\n");
						}
						
					}
					if (!ok) {
						oklog.append(" nicht gefunden\n");
						errlog.append(tables[i] + ": SynErr: Feld " + fields[k] + " "
							+ fieldType[k] + " nicht gefunden!\n");
					}
					
					rs.close();
				}
				
			}
		} catch (SQLException e) {
			Status status =
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
			StatusManager.getManager().handle(status, StatusManager.SHOW);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// ignore
			}
		}
		return oklog.toString();
	}
}
