package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.IContactHelper;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;

@Component
public class OrganizationKontaktTransformer
		implements IFhirTransformer<Organization, IOrganization> {
	
	@Reference(target="("+IModelService.SERVICEMODELNAME+"=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IXidService xidService;
	
	@Reference
	private IUserService userService;
	
	private IContactHelper contactHelper;
	
	@Activate
	public void activate() {
		contactHelper = new IContactHelper(modelService, xidService, userService);
	}
	
	@Override
	public Optional<Organization> getFhirObject(IOrganization localObject,SummaryEnum summaryEnum, Set<Include> includes){
		Organization organization = new Organization();
		
		organization.setId(new IdDt("Organization", localObject.getId()));
		
		List<Identifier> identifiers = contactHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		organization.setIdentifier(identifiers);
		
		organization.setName(contactHelper.getOrganizationName(localObject));
		List<Address> addresses = contactHelper.getAddresses(localObject);
		for (Address address : addresses) {
			organization.addAddress(address);
		}
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(localObject);
		for (ContactPoint contactPoint : contactPoints) {
			organization.addTelecom(contactPoint);
		}
		
		return Optional.of(organization);
	}
	
	@Override
	public Optional<IOrganization> getLocalObject(Organization fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IOrganization> updateLocalObject(Organization fhirObject,
		IOrganization localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IOrganization> createLocalObject(Organization fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Organization.class.equals(fhirClazz) && IOrganization.class.equals(localClazz);
	}
	
}
