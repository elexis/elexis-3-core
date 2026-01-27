package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.MandantHelper;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;

@Component
public class PractitionerRoleUserTransformer implements IFhirTransformer<PractitionerRole, IUser> {

	private MandantHelper mandantHelper = new MandantHelper();

	@Override
	public Optional<PractitionerRole> getFhirObject(IUser localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		PractitionerRole practitionerRole = new PractitionerRole();
		practitionerRole.setId(new IdDt("PractitionerRole", localObject.getId()));

		Collection<IRole> roles = localObject.getRoles();
		for (IRole role : roles) {
			String roleId = role.getId();
			if (roleId != null) {
				practitionerRole.addCode(mandantHelper.getPractitionerRoleCode(roleId));
			}
		}
		practitionerRole.setActive(localObject.isActive());
		// add the practitioner
		if (localObject.getAssignedContact() != null) {
			practitionerRole.setPractitioner(new Reference(
					new IdDt(Practitioner.class.getSimpleName(), localObject.getAssignedContact().getId())));
		}

		return Optional.of(practitionerRole);
	}

	@Override
	public Optional<IUser> getLocalObject(PractitionerRole fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IUser> updateLocalObject(PractitionerRole fhirObject, IUser localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IUser> createLocalObject(PractitionerRole fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return PractitionerRole.class.equals(fhirClazz) && IUser.class.equals(localClazz);
	}
}
