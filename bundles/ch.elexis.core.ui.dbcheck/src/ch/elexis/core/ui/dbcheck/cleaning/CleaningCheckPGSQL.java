package ch.elexis.core.ui.dbcheck.cleaning;

import ch.elexis.core.ui.dbcheck.CheckExec;
import ch.elexis.core.ui.dbcheck.model.DBModel;
import ch.elexis.core.ui.dbcheck.model.TableDescriptor;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

public class CleaningCheckPGSQL extends CleaningCheck {
	
	public CleaningCheckPGSQL(){
		oklog = new StringBuilder();
		errlog = new StringBuilder();
	}
	
	@Override
	public String cleanCoreTables(JdbcLink j){
		String version = CheckExec.getDBVersion();
		String[] tables = DBModel.getTableModel(version);
		// Iterate Tables
		for (int i = 0; i < tables.length; i++) {
			TableDescriptor tableDetail = DBModel.getTableDescription(tables[i]);
			String[] cleaningScripts = tableDetail.getCleaningSQLforPostgresSQL(version);
			if (!(cleaningScripts == null) && cleaningScripts.length > 0) {
				for (int k = 0; k < cleaningScripts.length; k = k + 2) {
					String description = cleaningScripts[k];
					String query = cleaningScripts[k + 1];
					Stm stm = j.getStatement();
					int rsNo = stm.exec(query);
					if (rsNo > 1)
						errlog.append(tables[i] + ": " + description + " - # affected rows: "
							+ rsNo);
					oklog.append(tables[i] + ": " + description + " - # affected rows: " + rsNo);
				}
			}
		}
		return oklog.toString();
	}
	
}
