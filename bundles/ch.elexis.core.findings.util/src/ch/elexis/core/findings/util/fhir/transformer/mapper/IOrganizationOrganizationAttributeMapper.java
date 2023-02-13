package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.fhir.FhirChConstants;
import ch.elexis.core.findings.util.fhir.transformer.helper.IContactHelper;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.types.Country;

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

		List<Identifier> identifiers = contactHelper.getIdentifiers(source, xidService);
		identifiers.add(getElexisObjectIdentifier(source));
		target.setIdentifier(identifiers);

		target.setName(contactHelper.getOrganizationName(source));
		List<Address> addresses = contactHelper.getAddresses(source);
		for (Address address : addresses) {
			target.addAddress(address);
		}
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(source);
		for (ContactPoint contactPoint : contactPoints) {
			target.addTelecom(contactPoint);
		}

	}

	@Override
	public void fhirToElexis(Organization source, IOrganization target) {
		mapIdentifiers(source, target);
		mapNameContactData(source, target);
		contactHelper.mapTelecom(source.getTelecom(), target);
	}

	private void mapNameContactData(Organization source, IOrganization target) {
		target.setDescription1(source.getName());

		Address addressFirstRep = source.getAddressFirstRep();
		if (addressFirstRep != null) {
			if (addressFirstRep.hasLine()) {
				StringBuilder sb = new StringBuilder();
				addressFirstRep.getLine().forEach(e -> sb.append(e));
				target.setStreet(sb.toString());
			} else {
				target.setStreet(null);
			}
			target.setCity(addressFirstRep.getCity());
			target.setZip(addressFirstRep.getPostalCode());
			try {
				Country country = Country.valueOf(addressFirstRep.getCountry());
				target.setCountry(country);
			} catch (IllegalArgumentException | NullPointerException e) {
				// ignore
			}
		} else {
			target.setStreet(null);
			target.setCity(null);
			target.setCountry(null);
			target.setZip(null);
		}

	}

	private void mapIdentifiers(Organization source, IOrganization target) {
		for (Identifier identifier : source.getIdentifier()) {
			if (FhirChConstants.OID_GLN_SYSTEM.equals(identifier.getSystem())) {
				target.addXid(XidConstants.DOMAIN_RECIPIENT_EAN, identifier.getValue(), true);
			}
			if (FhirChConstants.BSV_NUMMER_SYSTEM.equals(identifier.getSystem())) {
				target.addXid(XidConstants.DOMAIN_BSVNUM, identifier.getValue(), true);
			}
		}

	}

}
