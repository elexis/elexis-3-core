package ch.elexis.core.model.billable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Result;

public abstract class AbstractNoObligationOptifier<T extends IBillable>
		extends AbstractOptifier<T> {
	
	private IConfigService configService;
	private IContextService contextService;
	
	public AbstractNoObligationOptifier(IModelService coreModelService,
		IConfigService configService, IContextService contextService){
		super(coreModelService);
		this.configService = configService;
		this.contextService = contextService;
	}
	
	@Override
	public Result<IBilled> add(T billable, IEncounter encounter, double amount, boolean save){
		if (isNoObligation(billable)) {
			String law = encounter.getCoverage().getBillingSystem().getLaw().name();
			
			boolean forceObligation =
				configService.getActiveUserContact(Preferences.LEISTUNGSCODES_OBLIGATION, false);
			
			if (forceObligation && "KVG".equalsIgnoreCase(law)) {
				IEncounter noOblEncounter = null;
				contextService.getRootContext().setNamed("SelectFallNoObligationDialog.coverage",
					encounter.getCoverage());
				contextService.getRootContext().setNamed("SelectFallNoObligationDialog.billable",
					billable);
				@SuppressWarnings("unchecked")
				Optional<ICoverage> selectedCoverage =
					(Optional<ICoverage>) contextService.getNamed("SelectFallNoObligationDialog");
				if (selectedCoverage.isPresent()) {
					noOblEncounter =
						getEncounterByDate(selectedCoverage.get(), encounter.getDate())
							.orElse(null);
					// create new Konsultation if there is none matching
					if (noOblEncounter == null) {
						noOblEncounter =
							new IEncounterBuilder(coreModelService, selectedCoverage.get(),
								contextService.getActiveMandator().orElse(null)).build();
						// transfer diagnoses to the encounter
						List<IDiagnosisReference> diagnoses = encounter.getDiagnoses();
						for (IDiagnosisReference diag : diagnoses) {
							noOblEncounter.addDiagnosis(diag);
						}
						coreModelService.save(noOblEncounter);
					}
				}
				contextService.getRootContext()
					.setNamed("SelectFallNoObligationDialog.coverage", null);
				contextService.getRootContext()
					.setNamed("SelectFallNoObligationDialog.billable", null);
				if (noOblEncounter != null) {
					encounter = noOblEncounter;
				} else {
					return new Result<IBilled>(Result.SEVERITY.WARNING, 0,
						"Auf diesen Fall können nur Pflichtleistungen verrechnet werden. Bitte einen separaten Fall für Nichtpflichtleistungen anlegen.",
						null, false);
				}
			}
		}
		return super.add(billable, encounter, amount);
	}
	
	private Optional<IEncounter> getEncounterByDate(ICoverage coverage, LocalDate date){
		return coverage.getEncounters().stream().filter(e -> e.getDate().equals(date)).findFirst();
	}
	
	protected abstract boolean isNoObligation(T billable);
}
