package ch.elexis.core.findings.fhir.po.text;

import java.util.Optional;

import ch.elexis.core.data.interfaces.text.ITextResolver;
import ch.elexis.core.findings.fhir.po.dataaccess.FindingsDataAccessor;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.data.Patient;

public class FamAnamnesisResolver extends AbstractTextResolver implements ITextResolver {

	@Override
	public Optional<String> resolve(Object object) {
		if (object instanceof Patient) {
			if (ConfigServiceHolder.getGlobal(IMigratorService.FAMANAM_SETTINGS_USE_STRUCTURED, false)) {
				return getFindingsText(object, FindingsDataAccessor.FINDINGS_PATIENT_FAMANAM);
			}
		}
		return Optional.empty();
	}
}
