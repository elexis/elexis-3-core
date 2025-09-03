package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Organization.OrganizationContactComponent;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.FhirChConstants;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.IContactHelper;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IXidService;

public class IOrganizationOrganizationAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<IOrganization, Organization> {

	private IContactHelper contactHelper;
	private IXidService xidService;

	public IOrganizationOrganizationAttributeMapper(IXidService xidService) {
		this.xidService = xidService;
		contactHelper = new IContactHelper();
	}

	@Override
	public void elexisToFhir(IOrganization source, Organization target, SummaryEnum summaryEnum,
			Set<Include> includes) {

		target.setId(new IdDt(Organization.class.getSimpleName(), source.getId()));
		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}
		
		List<Identifier> identifiers = contactHelper.getIdentifiers(source, xidService);
		target.setIdentifier(identifiers);

		target.setName(contactHelper.getOrganizationName(source));
		mapContactPerson(source.getDescription3(), target.getContact());

		List<Address> addresses = contactHelper.getAddresses(source);
		for (Address address : addresses) {
			target.addAddress(address);
		}
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(source);
		for (ContactPoint contactPoint : contactPoints) {
			target.addTelecom(contactPoint);
		}

		if (source.isLaboratory()) {
			target.addType().addCoding().setSystem(FhirChConstants.HEALTHCARE_FACILITY_TYPE_CODE_SYSTEM)
					.setCode(FhirChConstants.SCTID_LABORATORY_ENVIRONMENT).setDisplay("Laboratory");
		}

	}

	@Override
	public void fhirToElexis(Organization source, IOrganization target) {
		target.setDescription1(source.getName());
		
		contactHelper.mapIdentifiers(source.getIdentifier(), target);
		contactHelper.mapAddress(source.getAddress(), target);
		contactHelper.mapTelecom(source.getTelecom(), target);
		mapContactPerson(source.getContact(), target);

		Coding hftcs = ModelUtil
				.getCodeableConceptBySystem(source.getType(), FhirChConstants.HEALTHCARE_FACILITY_TYPE_CODE_SYSTEM)
				.map(e -> e.getCodingFirstRep()).orElse(null);
		target.setLaboratory(StringUtils.equals(hftcs != null ? hftcs.getCode() : null,
				FhirChConstants.SCTID_LABORATORY_ENVIRONMENT));
	}

	private void mapContactPerson(String contactPersonName, List<OrganizationContactComponent> contact) {
		if (StringUtils.isNotBlank(contactPersonName)) {
			OrganizationContactComponent occ = new OrganizationContactComponent();
			occ.setName(new HumanName().setText(contactPersonName));
			contact.add(occ);
		}
	}

	private void mapContactPerson(List<OrganizationContactComponent> occs, IOrganization target) {
		if (!occs.isEmpty()) {
			target.setDescription3(occs.get(0).getName().getText());
		} else {
			target.setDescription3(null);
		}
	}

}
