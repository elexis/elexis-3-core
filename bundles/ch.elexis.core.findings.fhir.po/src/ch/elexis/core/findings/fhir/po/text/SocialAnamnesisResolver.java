package ch.elexis.core.findings.fhir.po.text;

import java.util.Optional;

import ch.elexis.core.data.interfaces.text.ITextResolver;
import ch.elexis.core.findings.fhir.po.dataaccess.FindingsDataAccessor;
import ch.elexis.data.Patient;

public class SocialAnamnesisResolver extends AbstractTextResolver implements ITextResolver {

	@Override
	public Optional<String> resolve(Object object) {
		if (object instanceof Patient) {
			return getFindingsText(object, FindingsDataAccessor.FINDINGS_PATIENT_SOCANAM);
		}
		return Optional.empty();
	}
}
