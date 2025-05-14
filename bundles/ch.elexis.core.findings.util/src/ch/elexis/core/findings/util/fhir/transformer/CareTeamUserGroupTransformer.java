package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.services.IModelService;

@Component
public class CareTeamUserGroupTransformer implements IFhirTransformer<CareTeam, IUserGroup> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Override
	public Optional<CareTeam> getFhirObject(IUserGroup localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		CareTeam careTeam = new CareTeam();
		careTeam.setId(new IdDt("CareTeam", localObject.getId()));
		careTeam.setName(localObject.getGroupname());

		List<IUser> users = localObject.getUsers();
		for (IUser iUser : users) {
			if (iUser.getAssignedContact() != null) {
				CareTeamParticipantComponent participant = careTeam.addParticipant();
				participant.setMember(new Reference(
						new IdDt(Practitioner.class.getSimpleName(), iUser.getAssignedContact().getId())));
			}
		}
		return Optional.of(careTeam);
	}

	@Override
	public Optional<IUserGroup> getLocalObject(CareTeam fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IUserGroup.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IUserGroup> updateLocalObject(CareTeam fhirObject, IUserGroup localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IUserGroup> createLocalObject(CareTeam fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return CareTeam.class.equals(fhirClazz) && IUserGroup.class.equals(localClazz);
	}
}
