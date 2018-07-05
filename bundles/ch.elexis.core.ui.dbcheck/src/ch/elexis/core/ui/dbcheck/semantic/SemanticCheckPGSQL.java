package ch.elexis.core.ui.dbcheck.semantic;

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

public class SemanticCheckPGSQL extends SemanticCheck {
	
	public SemanticCheckPGSQL(){
		oklog = new StringBuilder();
		errlog = new StringBuilder();
	}
	
	@Override
	public String checkSemanticStateCoreTables(JdbcLink j, IProgressMonitor monitor){
		String version = CheckExec.getDBVersion();
		String[] tables = DBModel.getTableModel(version);
		
		try {
			// Iterate Tables
			for (int i = 0; i < tables.length; i++) {
				String status = "Überprüfe Tabelle " + tables[i];
				monitor.subTask(status);
				oklog.append(status + ":\n");
				
				TableDescriptor tableDetail = DBModel.getTableDescription(tables[i]);
				String[] invalidStates = tableDetail.getInvalidStates(version);
				if (!(invalidStates == null) && invalidStates.length > 0) {
					for (int k = 0; k < invalidStates.length; k++) {
						String query = invalidStates[k];
						Stm stm = j.getStatement();
						ResultSet rs = stm.query("SELECT * FROM " + tables[i] + " WHERE " + query);
						while (rs.next()) {
							errlog.append(tables[i] + ": Semantischer Fehler bei Query <<" + query
								+ ">> auf ID " + rs.getString(1) + "\n");
						}
					}
				}
			}
			
		} catch (SQLException e) {
			Status status =
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
			StatusManager.getManager().handle(status, StatusManager.SHOW);
		}
		return oklog.toString();
	}
}
