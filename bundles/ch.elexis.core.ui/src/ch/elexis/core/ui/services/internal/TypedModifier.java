package ch.elexis.core.ui.services.internal;

import java.util.Optional;

import javax.inject.Inject;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class TypedModifier {

	@Inject
	private IEncounterService encounterService;

	@Inject
	private ICoverageService coverageService;

	private Context context;

	public TypedModifier(Context context) {
		this.context = context;
	}

	public void modifyFor(Object object) {
		if (object instanceof IPatient) {
			Optional<IEncounter> latestEncounter = getEncounterService().getLatestEncounter((IPatient) object);
			if (latestEncounter.isPresent()) {
				context.setTyped(latestEncounter.get(), true);
				context.setTyped(latestEncounter.get().getCoverage(), true);
			} else {
				context.removeTyped(IEncounter.class);
				context.removeTyped(ICoverage.class);
			}
		}
		if (object instanceof ICoverage) {
			Optional<IEncounter> latestEncounter = getCoverageService().getLatestEncounter((ICoverage) object);
			if (latestEncounter.isPresent()) {
				context.setTyped(latestEncounter.get(), true);
			} else {
				context.removeTyped(IEncounter.class);
			}
		}
		if (object instanceof IEncounter) {
			context.setTyped(((IEncounter) object).getCoverage(), true);
		}
	}

	private IEncounterService getEncounterService() {
		if (encounterService == null) {
			CoreUiUtil.injectServicesWithContext(this);
		}
		return encounterService;
	}

	private ICoverageService getCoverageService() {
		if (coverageService == null) {
			CoreUiUtil.injectServicesWithContext(this);
		}
		return coverageService;
	}
}
