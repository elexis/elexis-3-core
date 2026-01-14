package ch.elexis.core.fhir.mapper.r4;

import org.hl7.fhir.r4.model.RelatedPerson;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.IPersonHelper;
import ch.elexis.core.model.IPerson;

public class IPersonRelatedPersonAttributeMapper
		extends IdentifiableDomainResourceAttributeMapper<IPerson, RelatedPerson> {

	private IPersonHelper personHelper;

	public IPersonRelatedPersonAttributeMapper() {
		super(RelatedPerson.class);

		personHelper = new IPersonHelper();
	}

	@Override
	public void fullElexisToFhir(IPerson source, RelatedPerson target, SummaryEnum summaryEnum) {
		// TODO Auto-generated method stub
	}

	@Override
	public void fullFhirToElexis(RelatedPerson source, IPerson target) {
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
	}

}
