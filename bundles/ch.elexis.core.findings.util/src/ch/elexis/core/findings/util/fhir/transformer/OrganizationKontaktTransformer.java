package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Organization;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.IOrganizationOrganizationAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;

@Component
public class OrganizationKontaktTransformer implements IFhirTransformer<Organization, IOrganization> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IXidService xidService;

	@Reference
	private IUserService userService;

	private IOrganizationOrganizationAttributeMapper attributeMapper;

	@Activate
	public void activate() {
		attributeMapper = new IOrganizationOrganizationAttributeMapper(modelService, xidService);
	}

	@Override
	public Optional<Organization> getFhirObject(IOrganization localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Organization organization = new Organization();
		attributeMapper.elexisToFhir(localObject, organization, summaryEnum);
		return Optional.of(organization);
	}

	@Override
	public Optional<IOrganization> getLocalObject(Organization fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return modelService.load(localId.get(), IOrganization.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IOrganization> updateLocalObject(Organization fhirObject, IOrganization localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		modelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IOrganization> createLocalObject(Organization fhirObject) {
		IOrganization create = modelService.create(IOrganization.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		modelService.save(create);
		return Optional.of(create);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Organization.class.equals(fhirClazz) && IOrganization.class.equals(localClazz);
	}

}
