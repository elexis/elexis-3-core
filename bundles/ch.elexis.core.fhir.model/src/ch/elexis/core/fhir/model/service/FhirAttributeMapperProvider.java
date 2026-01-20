package ch.elexis.core.fhir.model.service;

import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DomainResource;

import ch.elexis.core.fhir.mapper.r4.ICoverageCoverageAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.IdentifiableDomainResourceAttributeMapper;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class FhirAttributeMapperProvider {

	@SuppressWarnings("unchecked")
	public static <T extends Identifiable, U extends DomainResource> IdentifiableDomainResourceAttributeMapper<T, U> getMapper(
			Class<?> modelType, Class<U> fhirType) {

		if (modelType.equals(ICoverage.class) || fhirType.equals(Coverage.class)) {
			ICoverageService service = OsgiServiceUtil.getService(ICoverageService.class).get();
			return (IdentifiableDomainResourceAttributeMapper<T, U>) new ICoverageCoverageAttributeMapper(
					CoreModelServiceHolder.get(), service);
		}
		return null;
	}

}
