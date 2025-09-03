package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.transformer.helper.IPersonHelper;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;

public class IPersonPersonAttributeMapper implements IdentifiableDomainResourceAttributeMapper<IPerson, Person> {

	private IModelService modelService;
	private IXidService xidService;
	private IPersonHelper personHelper;

	public IPersonPersonAttributeMapper(IModelService modelService, IXidService xidService) {
		this.modelService = modelService;
		this.xidService = xidService;
		personHelper = new IPersonHelper();
	}

	@Override
	public void elexisToFhir(IPerson source, Person target, SummaryEnum summaryEnum, Set<Include> includes) {
		target.setId(new IdDt(Person.class.getSimpleName(), source.getId()));

		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

		List<Identifier> identifiers = personHelper.getIdentifiers(source, xidService);
		target.setIdentifier(identifiers);
		
		target.setName(personHelper.getHumanNames(source));
		target.setGender(personHelper.getGender(source.getGender()));
		target.setBirthDate(personHelper.getBirthDate(source));
		target.setAddress(personHelper.getAddresses(source));
		target.setTelecom(personHelper.getContactPoints(source));

		target.setPhoto(personHelper.mapContactImage(source));

		if (source.isPatient()) {
			target.addLink().setTarget(new Reference(new IdDt(Patient.class.getSimpleName(), source.getId())));
		}

		if (source.isMandator()) {
			target.addLink().setTarget(new Reference(new IdDt(Practitioner.class.getSimpleName(), source.getId())));
		}
	}

	@Override
	public void fhirToElexis(Person source, IPerson target) {
		checkPromoteToPatient(source, target);
		personHelper.mapIdentifiers(source.getIdentifier(), target);
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
		personHelper.mapContactImage(modelService, source.getPhoto(), target);
	}

	private void checkPromoteToPatient(Person source, IPerson target) {
		if (!target.isPatient() && !source.getLink().isEmpty()) {
			Optional<Reference> patientReference = source.getLink().stream().filter(e -> Objects.nonNull(e.getTarget())).map(e -> e.getTarget())
					.filter(e -> StringUtils.startsWith(e.getReference(), Patient.class.getSimpleName())).findFirst();
			if(patientReference.isPresent()) {
				String value = patientReference.get().getReference();
				if(StringUtils.equals(value, Patient.class.getSimpleName()+"/"+target.getId())) {
					target.setCode(null);
					target.setPatient(true); // patient-number is transparently created on save
				}
			}
			
		}
	}

}
