package ch.elexis.core.fhir.mapper.r4;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Organization.OrganizationContactComponent;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.FhirChConstants;
import ch.elexis.core.fhir.mapper.r4.helper.IContactHelper;
import ch.elexis.core.fhir.mapper.r4.util.ModelUtil;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;

public class IOrganizationOrganizationAttributeMapper
		extends IdentifiableDomainResourceAttributeMapper<IOrganization, Organization> {

	private IContactHelper contactHelper;
	private IModelService coreModelService;
	private IXidService xidService;

	public IOrganizationOrganizationAttributeMapper(IModelService coreModelService, IXidService xidService) {
		super(Organization.class);

		this.xidService = xidService;
		this.coreModelService = coreModelService;
		contactHelper = new IContactHelper();
	}

	@Override
	public void fullElexisToFhir(IOrganization source, Organization target, SummaryEnum summaryEnum) {

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
	public void fullFhirToElexis(Organization source, IOrganization target) {
		target.setDescription1(source.getName());

		contactHelper.mapIdentifiers(coreModelService, source.getIdentifier(), target);
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
