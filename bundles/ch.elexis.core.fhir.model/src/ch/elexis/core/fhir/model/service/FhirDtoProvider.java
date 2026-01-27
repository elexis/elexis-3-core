package ch.elexis.core.fhir.model.service;

import ch.elexis.core.fhir.model.dto.ICoverageDto;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.Identifiable;

public class FhirDtoProvider {

	@SuppressWarnings("unchecked")
	public static <T extends Identifiable> T createDto(Class<?> modelType) {
		if (modelType.equals(ICoverage.class)) {
			return (T) new ICoverageDto();
		}
		return null;
	}

}
