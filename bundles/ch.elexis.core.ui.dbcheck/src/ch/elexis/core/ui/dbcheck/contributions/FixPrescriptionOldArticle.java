package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;

public class FixPrescriptionOldArticle extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		int nonExisting = 0;
		int nonEan = 0;
		int nonFound = 0;
		int fixed = 0;

		ICodeElementServiceContribution artikelstammContribution = CodeElementServiceHolder.get()
				.getContribution(CodeElementTyp.ARTICLE, "Artikelstamm")
				.orElseThrow(() -> new IllegalStateException("No Artikelstamm available"));

		Query<Prescription> query = new Query<>(Prescription.class);
		List<Prescription> prescriptions = query.execute();
		pm.beginTask("Fixing prescriptions", prescriptions.size());
		for (Prescription prescription : prescriptions) {
			if (StringUtils.isEmpty(prescription.get(Prescription.FLD_ARTICLE))
					&& StringUtils.isNotEmpty(prescription.get(Prescription.FLD_ARTICLE_ID))) {
				Optional<IArticle> loadedArtikel = CoreModelServiceHolder.get()
						.load(prescription.get(Prescription.FLD_ARTICLE_ID), IArticle.class);
				if (loadedArtikel.isPresent()) {
					if (StringUtils.isNotEmpty(loadedArtikel.get().getGtin())) {
						Optional<ICodeElement> loaded = artikelstammContribution
								.loadFromCode(loadedArtikel.get().getGtin());
						if (loaded.isPresent()) {
							prescription.set(Prescription.FLD_ARTICLE,
									StoreToStringServiceHolder.getStoreToString(loaded.get()));
							fixed++;
						} else {
							nonFound++;
						}
					} else {
						nonEan++;
					}
				} else {
					nonExisting++;
				}
			}
			pm.worked(1);
		}
		return "Prescriptions fixed " + fixed + ", article ean not found " + nonFound + ", article no ean " + nonEan
				+ ", article not existing " + nonExisting;
	}

	@Override
	public String getMaintenanceDescription() {
		return "Fix prescription entries with id references, with Artikelstamm EAN lookup";
	}

}
