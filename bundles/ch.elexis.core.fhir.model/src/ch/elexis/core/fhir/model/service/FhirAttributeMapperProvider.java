package ch.elexis.core.fhir.model.service;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Person;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.fhir.mapper.r4.ICoverageCoverageAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.IPatientPatientAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.IPersonPersonAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.IPersonPractitionerAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.IdentifiableDomainResourceAttributeMapper;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.ICompositeModelService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IXidService;

public class FhirAttributeMapperProvider {

	@SuppressWarnings("unchecked")
	public static <T extends Identifiable, U extends DomainResource> IdentifiableDomainResourceAttributeMapper<T, U> getMapper(
			Class<?> modelType, Class<U> fhirType) {

		ICompositeModelService compositeModelService = PortableServiceLoader.get(ICompositeModelService.class);

		if (modelType.equals(IPerson.class) || fhirType.equals(Person.class)) {
			IXidService xidService = PortableServiceLoader.get(IXidService.class);
			return (IdentifiableDomainResourceAttributeMapper<T, U>) new IPersonPersonAttributeMapper(
					compositeModelService, xidService);
		} else if (modelType.equals(ICoverage.class) || fhirType.equals(Coverage.class)) {
			ICoverageService service = PortableServiceLoader.get(ICoverageService.class);
			return (IdentifiableDomainResourceAttributeMapper<T, U>) new ICoverageCoverageAttributeMapper(
					compositeModelService, service);
		} else if (modelType.equals(IMandator.class) || fhirType.equals(Practitioner.class)) {
			IXidService xidService = PortableServiceLoader.get(IXidService.class);
			return (IdentifiableDomainResourceAttributeMapper<T, U>) new IPersonPractitionerAttributeMapper(
					compositeModelService, xidService);
		} else if (modelType.equals(IPatient.class) || fhirType.equals(Patient.class)) {
			IXidService xidService = PortableServiceLoader.get(IXidService.class);
			return (IdentifiableDomainResourceAttributeMapper<T, U>) new IPatientPatientAttributeMapper(
					compositeModelService, xidService);
		}
		throw new UnsupportedOperationException("Missing mapper " + modelType + " <-> " + fhirType);
	}

}
