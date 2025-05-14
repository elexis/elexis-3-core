package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.hl7.fhir.r4.model.CareTeam;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class CareTeamIUserGroupTransformerTest {

	private static IFhirTransformer<CareTeam, IUserGroup> transformer;
	private static IPatient patient;

	@BeforeClass
	public static void beforeClass() {
		transformer = AllTransformerTests.getTransformerRegistry()
				.getTransformerFor(CareTeam.class, IUserGroup.class);
		assertNotNull(transformer);
	}

	@Test
	public void getFhirObject() {
		IUserGroup localObject = setupLocalIUserGroup("TestGroup");
		Optional<CareTeam> fhirObject = transformer.getFhirObject(localObject);
		assertTrue(fhirObject.isPresent());
		assertFalse(fhirObject.get().getParticipant().isEmpty());

		CoreModelServiceHolder.get().remove(localObject.getUsers().get(0).getAssignedContact());
		CoreModelServiceHolder.get().remove(localObject.getUsers().get(0));
		CoreModelServiceHolder.get().remove(localObject);
	}

	private IUserGroup setupLocalIUserGroup(String name) {
		IUserGroup ret = new IUserGroupBuilder(AllTransformerTests.getCoreModelService(), name).build();

		IContact mandator = new IContactBuilder.MandatorBuilder(AllTransformerTests.getCoreModelService(), "Test",
				"Mandator").build();
		mandator.setUser(true);
		AllTransformerTests.getCoreModelService().save(mandator);
		IUser user = new IUserBuilder(AllTransformerTests.getCoreModelService(), name, mandator).buildAndSave();
		ret.addUser(user);
		AllTransformerTests.getCoreModelService().save(ret);
		return ret;
	}

}
