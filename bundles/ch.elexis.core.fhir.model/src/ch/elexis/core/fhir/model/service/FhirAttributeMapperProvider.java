package ch.elexis.core.fhir.model.service;

import org.hl7.fhir.r4.model.DomainResource;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.fhir.mapper.r4.IdentifiableDomainResourceAttributeMapper;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public class FhirAttributeMapperProvider {

	@Reference
	IModelService coreModelService;

	public static <T extends Identifiable, U extends DomainResource> IdentifiableDomainResourceAttributeMapper<T, U> getMapper(
			Class<?> modelType, Class<?> fhirType) {
		// TODO Auto-generated method stub
		return null;
	}

}
