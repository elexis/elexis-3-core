package ch.elexis.core.ui.dbcheck.contributions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.DBConnection;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink.Stm;

public class RemoveDSGWhenDeceased extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		int numDeletedSticker = 0;
		List<IPatient> lIPatients = new ArrayList<>();
		final String STICKER_DECEASED = "Verstorben"; //$NON-NLS-1$
		final String STICKER_DSG = "missing_dsg_consent_v1"; //$NON-NLS-1$
		
		IQuery<IPatient> queryPatient = CoreModelServiceHolder.get().getQuery(IPatient.class);
		queryPatient.and(ModelPackage.Literals.ICONTACT__DECEASED, COMPARATOR.EQUALS, true);
		List<IPatient> lDeceasedPatients = queryPatient.execute();
		for (IPatient p : lDeceasedPatients) {
			lIPatients.add(p);
		}
		
		IQuery<ISticker> querySticker = CoreModelServiceHolder.get().getQuery(ISticker.class);
		querySticker.and("ID", COMPARATOR.EQUALS, STICKER_DSG);
		List<ISticker> listSticker = querySticker.execute();
		if (listSticker.isEmpty()) {
			return Messages.RemoveDSGWhenDeceased_no_dsg_found;
		}
		ISticker stickerToBeDeleted = listSticker.get(0);

		IQuery<ISticker> queryStickerDeceased = CoreModelServiceHolder.get().getQuery(ISticker.class);
		queryStickerDeceased.and(ModelPackage.Literals.ISTICKER__NAME, COMPARATOR.EQUALS, STICKER_DECEASED);
		List<ISticker> listStickerDeceased = queryStickerDeceased.execute();
		if (listStickerDeceased.isEmpty()) {
			return Messages.RemoveDSGWhenDeceased_no_deceased_found;
		}
		ISticker deceasedStcker = listStickerDeceased.get(0);

		DBConnection connection = PersistentObject.getDefaultConnection();
		Stm stmGetObject = connection.getStatement();
		if (stickerToBeDeleted != null) {
		ResultSet resultObject = stmGetObject
					.query("SELECT OBJ FROM etiketten_object_link WHERE ETIKETTE = '" + deceasedStcker.getId()
							+ "'");
		try {
			while (resultObject.next()) {
				IPatient patient = CoreModelServiceHolder.get().load(resultObject.getString(1), IPatient.class)
						.orElse(null);
				if (patient != null && !lIPatients.contains(patient)) {
					lIPatients.add(patient);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		IStickerService stickerService = StickerServiceHolder.get();
		for (IPatient pat : lIPatients) {
			if (stickerService.hasSticker(pat, stickerToBeDeleted)) {
				stickerService.removeSticker(stickerToBeDeleted, pat);
				numDeletedSticker++;
			}
		}
	}

	return numDeletedSticker + " " + Messages.RemoveDSGWhenDeceased_deleted_successfully;
	}

	@Override
	public String getMaintenanceDescription() {
		return Messages.RemoveDSGWhenDeceased_description;
	}

}
