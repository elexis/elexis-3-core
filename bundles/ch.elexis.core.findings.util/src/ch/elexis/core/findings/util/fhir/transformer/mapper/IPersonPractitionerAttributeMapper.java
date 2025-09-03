package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.transformer.helper.IPersonHelper;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IXidService;

public class IPersonPractitionerAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<IPerson, Practitioner> {

	private IXidService xidService;
	private IPersonHelper personHelper;

	public IPersonPractitionerAttributeMapper(IXidService xidService) {
		this.xidService = xidService;
		personHelper = new IPersonHelper();
	}

	@Override
	public void elexisToFhir(IPerson source, Practitioner target, SummaryEnum summaryEnum, Set<Include> includes) {
		target.setId(new IdDt("Practitioner", source.getId(), Long.toString(source.getLastupdate())));

		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

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
	public void fhirToElexis(Practitioner source, IPerson target) {
		target.setMandator(true);
		personHelper.mapIdentifiers(source.getIdentifier(), target);
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
//		personHelper.mapContactImage(modelService, source.getPhoto(), target);
	}

}
