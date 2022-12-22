
package ch.elexis.core.ui.e4.handlers;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IBillingSystemService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.e4.dialog.UserDialog;

/**
 * e4 port of GlobalActions.neueKonsAction
 *
 * @since 3.8
 */
public class EncounterCreateHandler extends RestrictedHandler {

	public EncounterCreateHandler() {
		super(AccessControlDefaults.KONS_CREATE);
	}

	@Execute
	public void execute(IContextService contextService, IConfigService configService,
			IEncounterService encounterService, IBillingSystemService billingSystemService) {

		// determine coverage
		ICoverage coverage = contextService.getActiveCoverage().orElse(null);
		if (coverage == null) {
			IPatient patient = contextService.getActivePatient().orElse(null);
			if (patient == null) {
				UserDialog.error(Messages.GlobalActions_CantCreateKons, Messages.GlobalActions_DoSelectCase);
				return;
			}

			IEncounter latestEncounter = encounterService.getLatestEncounter(patient).orElse(null);
			if (latestEncounter != null) {
				coverage = latestEncounter.getCoverage();
			} else {
				List<ICoverage> coverages = patient.getCoverages();
				if (!coverages.isEmpty()) {
					coverage = coverages.get(0);
				} else {
					coverage = new ICoverageBuilder(CoreModelServiceHolder.get(), configService, billingSystemService,
							patient).buildAndSave();
				}
			}

		} else {
			// fall does not belong to actPatient?
			// if (!actFall.getPatient().equals(actPatient)) {
			// if (actPatient != null) {
			// Konsultation lk = actPatient.getLetzteKons(false);
			// if (lk != null) {
			// actFall = lk.getFall();
			// }
			// } else {
			// MessageEvent.fireError(Messages.GlobalActions_CantCreateKons,
			// Messages.GlobalActions_DoSelectCase);
			// return;
			// }
			// }
		}

		// validate coverage
		if (!coverage.isOpen()) {
			UserDialog.error(Messages.GlobalActions_casclosed, Messages.GlobalActions_caseclosedexplanation);
			return;
		}

		// does there already exist an encounter for today on the given coverage?
		List<IEncounter> encounters = coverage.getEncounters();
		for (IEncounter iEncounter : encounters) {
			if (LocalDate.now().equals(iEncounter.getDate())) {
				if (UserDialog.question(Messages.GlobalActions_SecondForToday,
						Messages.GlobalActions_SecondForTodayQuestion) == false) {
					return;
				}
			}
		}

		IMandator mandator = contextService.getActiveMandator().orElse(null);
		if (mandator == null) {
			UserDialog.error("No mandator selected", "Encounter creation requires a mandator");
			return;
		}

		IEncounter enounter = new IEncounterBuilder(CoreModelServiceHolder.get(), coverage, mandator).buildAndSave();
		encounterService.addDefaultDiagnosis(enounter);
		contextService.getRootContext().setNamed(ContextServiceHolder.SELECTIONFALLBACK, enounter);
	}

}