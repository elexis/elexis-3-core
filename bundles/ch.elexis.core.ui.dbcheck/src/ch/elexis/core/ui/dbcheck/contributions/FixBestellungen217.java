package ch.elexis.core.ui.dbcheck.contributions;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.NamedBlob2;
import ch.elexis.data.PersistentObject;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.JdbcLink.Stm;

public class FixBestellungen217 extends ExternalMaintenance {
	
	@Override
	public String executeMaintenance(final IProgressMonitor pm, String DBVersion){
		StringBuilder output = new StringBuilder();
		pm.beginTask("Bitte warten, Bestellungen werden reparieren ...", IProgressMonitor.UNKNOWN);
		// get all bestellungen form HEAP2 Table
		Stm stm = null;
		ResultSet resultSet = null;
		int oldCnt = 0;
		int updateCnt = 0;
		int invalidArt = 0;
		
		try {
			stm = PersistentObject.getConnection().getStatement();
			resultSet = stm.query("SELECT * FROM HEAP2");
			
			while (resultSet.next()) {
				pm.worked(1);
				
				String ID = resultSet.getString(NamedBlob2.FLD_ID);
				// test if ID looks like Bestellung ID
				String[] entry = ID.split(":");
				if (entry.length == 3) {
					oldCnt++;
					// update the data of the bestellung if it has been created previously
					Bestellung existingBestellung = Bestellung.load(ID);
					// if it does not exist yet, create it
					if (existingBestellung.exists()) {
						byte[] compressed = resultSet.getBytes(NamedBlob2.FLD_CONTENTS);
						byte[] decompressed = CompEx.expand(compressed);
						if (decompressed != null) {
							String content = new String(decompressed, "utf-8");
							if (!content.isEmpty()) {
								if (!testLoad(content)) {
									invalidArt++;
									continue;
								}
								
								existingBestellung.set("Liste", content);
								updateCnt++;
							} else {
								existingBestellung.set("Liste", "");
								updateCnt++;
							}
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {}
			}
			if (stm != null)
				PersistentObject.getConnection().releaseStatement(stm);
		}
		
		output.append(oldCnt + " alte Bestellungen gefunden.\n");
		output.append(invalidArt + " alte Bestellungen mit nicht mehr existenten Artikeln.\n");
		output.append(updateCnt + " neue Bestellungen repariert.\n");
		
		pm.done();
		
		return output.toString();
	}
	
	private boolean testLoad(String items){
		String[] it = items.split(";"); //$NON-NLS-1$
		
		for (String i : it) {
			String[] fld = i.split(","); //$NON-NLS-1$
			if (fld.length == 2) {
				Artikel art = Artikel.load(fld[0]);
				if (art == null)
					return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Bestellungen ohne Inhalt nach upgrade auf 2.1.7 reparieren";
	}
	
}
