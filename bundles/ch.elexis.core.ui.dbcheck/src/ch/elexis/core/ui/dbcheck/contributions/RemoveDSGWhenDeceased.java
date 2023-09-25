package ch.elexis.core.ui.dbcheck.contributions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.DBConnection;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink.Stm;

public class RemoveDSGWhenDeceased extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {

		String stickerId = null;
		int numDeletedSticker = 0;
		List<String> lPatients = new ArrayList<String>();
		
		IQuery<IPatient> queryPatient = CoreModelServiceHolder.get().getQuery(IPatient.class);
		queryPatient.and(ModelPackage.Literals.ICONTACT__DECEASED, COMPARATOR.EQUALS, true);
		List<IPatient> lDeceasedPatients = queryPatient.execute();
		for (IPatient p : lDeceasedPatients) {
			lPatients.add(p.getId());
		}

		IQuery<ISticker> querySticker = CoreModelServiceHolder.get().getQuery(ISticker.class);
		querySticker.and(ModelPackage.Literals.ISTICKER__NAME, COMPARATOR.EQUALS, "Verstorben");
		List<ISticker> listSticker = querySticker.execute();
		for (ISticker sticker : listSticker) {
			stickerId = sticker.getId();
		}

		DBConnection connection = PersistentObject.getDefaultConnection();
		Stm stmGetObject = connection.getStatement();
		ResultSet resultObject = stmGetObject
				.query("SELECT * FROM etiketten_object_link WHERE ETIKETTE = '" + stickerId + "'");

		try {
			while (resultObject.next()) {
				lPatients.add(resultObject.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Stm deleteDSG = connection.getStatement();
		for (int i = 0; i < lPatients.size(); i++) {
			int resultDeleted = deleteDSG
					.exec("DELETE FROM etiketten_object_link WHERE ETIKETTE = 'missing_dsg_consent_v1' AND OBJ ='"
							+ lPatients.get(i) + "';");
			numDeletedSticker = numDeletedSticker + resultDeleted;
		}

		return numDeletedSticker + " DSG Sticker wurden erfolgreich gelöscht.";
	}

	@Override
	public String getMaintenanceDescription() {
		return "DSG Sticker entfernen, wenn der Patient verstorben ist";
	}

}
