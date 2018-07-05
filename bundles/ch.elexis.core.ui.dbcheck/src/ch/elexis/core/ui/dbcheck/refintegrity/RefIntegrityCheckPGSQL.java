package ch.elexis.core.ui.dbcheck.refintegrity;

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

public class RefIntegrityCheckPGSQL extends RefIntegrityCheck {
	
	public RefIntegrityCheckPGSQL(){
		oklog = new StringBuilder();
		errlog = new StringBuilder();
	}
	
	@Override
	public String checkReferentialIntegrityStateCoreTables(JdbcLink j, IProgressMonitor monitor){
		String version = CheckExec.getDBVersion();
		String[] tables = DBModel.getTableModel(version);
		
		try {
			// Iterate Tables
			for (int i = 0; i < tables.length; i++) {
				String status = "Überprüfe Tabelle " + tables[i];
				monitor.subTask(status);
				oklog.append(status + ":\n");
				
				TableDescriptor tableDetail = DBModel.getTableDescription(tables[i]);
				String[] refIntErrors = tableDetail.getReferentialIntegrityCheck(version);
				if (!(refIntErrors == null) && refIntErrors.length > 0) {
					for (int k = 0; k < refIntErrors.length; k = k + 2) {
						String description = refIntErrors[k];
						String query = refIntErrors[k + 1];
						Stm stm = j.getStatement();
						ResultSet rs = stm.query(query);
						while (rs.next()) {
							errlog.append(tables[i] + ": " + rs.getString(1) + " " + description
								+ "\n");
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
