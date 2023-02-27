package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Set;

import org.hl7.fhir.r4.model.RelatedPerson;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.transformer.helper.IPersonHelper;
import ch.elexis.core.model.IPerson;

public class IPersonRelatedPersonAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<IPerson, RelatedPerson> {

	private IPersonHelper personHelper;

	public IPersonRelatedPersonAttributeMapper() {
		personHelper = new IPersonHelper();
	}

	@Override
	public void elexisToFhir(IPerson source, RelatedPerson target, SummaryEnum summaryEnum, Set<Include> includes) {
		// TODO Auto-generated method stub
	}

	@Override
	public void fhirToElexis(RelatedPerson source, IPerson target) {
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
	}

}
