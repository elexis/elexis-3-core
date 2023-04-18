package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Set;

import org.hl7.fhir.r4.model.Person;

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
		target.setId(new IdDt("Person", source.getId()));

		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

		target.setName(personHelper.getHumanNames(source));
		target.setGender(personHelper.getGender(source.getGender()));
		target.setBirthDate(personHelper.getBirthDate(source));
		target.setAddress(personHelper.getAddresses(source));
		target.setTelecom(personHelper.getContactPoints(source));

		target.setPhoto(personHelper.mapContactImage(source));
	}

	@Override
	public void fhirToElexis(Person source, IPerson target) {
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
	}

}
