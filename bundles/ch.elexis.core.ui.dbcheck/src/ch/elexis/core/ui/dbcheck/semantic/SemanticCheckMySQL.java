package ch.elexis.core.ui.dbcheck.semantic;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.CheckExec;
import ch.elexis.core.ui.dbcheck.model.DBModel;
import ch.elexis.core.ui.dbcheck.model.TableDescriptor;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLinkSyntaxException;
import ch.rgw.tools.JdbcLink.Stm;

public class SemanticCheckMySQL extends SemanticCheck {
	
	public SemanticCheckMySQL(){
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
						
						ResultSet rs = null;
						try {
							rs =
								stm.query("SELECT * FROM " + tables[i].toLowerCase() + " WHERE "
									+ query.toLowerCase());
						} catch (JdbcLinkSyntaxException je) {
							// We have no lowercase here, but uppercase!
							try {
								rs =
									stm.query("SELECT * FROM " + tables[i] + " WHERE "
										+ query.toLowerCase());
							} catch (JdbcLinkSyntaxException je2) {
								// We still did not find the table, assume its missing!
								errlog.append(tables[i] + ": Semantischer Fehler bei Query <<"
									+ query + ">> auf ID " + ((rs!=null) ? rs.getString(1) : rs) + "\n");
								continue;
							}
						}
					}
				}
			}
			
		} catch (SQLException e) {
			e.getMessage();
		}
		return oklog.toString();
		
	}
	
}
