package ch.elexis.core.fhir.mapper.r4;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.IPersonHelper;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;

public class IPersonPractitionerAttributeMapper
		extends IdentifiableDomainResourceAttributeMapper<IPerson, Practitioner> {

	private IModelService coreModelService;
	private IXidService xidService;
	private IPersonHelper personHelper;

	public IPersonPractitionerAttributeMapper(IModelService coreModelService, IXidService xidService) {
		super(Practitioner.class);

		this.xidService = xidService;
		this.coreModelService = coreModelService;
		personHelper = new IPersonHelper();
	}

	@Override
	public void fullElexisToFhir(IPerson source, Practitioner target, SummaryEnum summaryEnum) {
		List<Identifier> identifiers = personHelper.getIdentifiers(source, xidService);
		target.setIdentifier(identifiers);

		target.setActive(!source.isDeleted());
		target.setName(personHelper.getHumanNames(source));
		target.setGender(personHelper.getGender(source.getGender()));
		target.setBirthDate(personHelper.getBirthDate(source));
		target.setAddress(personHelper.getAddresses(source));
		target.setTelecom(personHelper.getContactPoints(source));

		Attachment mapContactImage = personHelper.mapContactImage(source);
		target.setPhoto(mapContactImage != null ? Collections.singletonList(mapContactImage) : null);

	}

	@Override
	public void fullFhirToElexis(Practitioner source, IPerson target) {
		target.setMandator(true);
		personHelper.mapIdentifiers(coreModelService, source.getIdentifier(), target);
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
//		personHelper.mapContactImage(modelService, source.getPhoto(), target);
	}

}
