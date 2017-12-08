package ch.elexis.core.findings.fhir.po.text;

import java.util.Optional;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.text.ITextResolver;
import ch.elexis.core.findings.fhir.po.dataaccess.FindingsDataAccessor;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.data.Patient;

public class RiskResolver extends AbstractTextResolver implements ITextResolver {
	
	@Override
	public Optional<String> resolve(Object object){
		if (object instanceof Patient) {
			if (CoreHub.globalCfg.get(IMigratorService.RISKFACTOR_SETTINGS_USE_STRUCTURED, false)) {
				return getFindingsText(object, FindingsDataAccessor.FINDINGS_PATIENT_RISK);
			}
		}
		return Optional.empty();
	}
}
