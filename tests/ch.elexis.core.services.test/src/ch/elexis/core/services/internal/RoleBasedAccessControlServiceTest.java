package ch.elexis.core.services.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.ac.SystemCommandConstants;
import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ac.EvACEs;
import ch.elexis.core.services.AllServiceTests;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class RoleBasedAccessControlServiceTest {

	private IModelService modelService = AllServiceTests.getModelService();
	private static IAccessControlService accessControlService;
	private static IContextService contextService;

	@BeforeClass
	public static void beforeClass() {
		accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).get();
		contextService = OsgiServiceUtil.getService(IContextService.class).get();
		contextService.getRootContext().setNamed("testAccessControl", Boolean.TRUE);
	}

	@AfterClass
	public static void afterClass() {
		contextService.getRootContext().setNamed("testAccessControl", null);
	}
	
	@Test
	public void userHasSystemCommandRightToLogin() {
		assertTrue(accessControlService.evaluate(EvACE.of(SystemCommandConstants.LOGIN_UI)));
		assertFalse(accessControlService.evaluate(EvACE.of("some-invalid-command")));
	}

	@Test
	public void userHasNoRightToLoadPatients() {
		assertFalse(accessControlService.evaluate(EvACE.of(IPatient.class, Right.READ)));
		assertFalse(accessControlService.evaluate(EvACE.of(IPatient.class, Right.VIEW)));

		// direct load
		Optional<IPatient> patient = modelService.load(AllServiceTests.getPatient().getId(), IPatient.class);
		assertTrue(patient.isEmpty());

		// fetch via query
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		query.limit(10);
		List<IPatient> execute = query.execute();
		assertTrue(execute.isEmpty());
	}

	@Test(expected = AccessControlException.class)
	public void userHasNoRightToCreatePatient() {
		assertFalse(accessControlService.evaluate(EvACE.of(IPatient.class, Right.CREATE)));

		CoreModelServiceHolder.get().create(IPatient.class);
	}

	@Test(expected = AccessControlException.class)
	public void userHasNoRightToRemoveOrganization() {
		assertTrue(accessControlService.evaluate(EvACE.of(IOrganization.class, Right.READ)));
		assertFalse(accessControlService.evaluate(EvACE.of(IOrganization.class, Right.REMOVE)));
		
		Optional<IOrganization> load = CoreModelServiceHolder.get().load(AllServiceTests.getLaboratory().getId(),
				IOrganization.class);
		CoreModelServiceHolder.get().remove(load.get()); // or other exception
	}

	@Test
	public void userHasRightToLoadOrganizationAndLaboratory() {
		assertTrue(accessControlService.evaluate(EvACE.of(IOrganization.class, Right.READ)));
		assertTrue(accessControlService.evaluate(EvACE.of(IOrganization.class, Right.VIEW)));

		// direct load
		Optional<ILaboratory> laboratory = modelService.load(AllServiceTests.getLaboratory().getId(),
				ILaboratory.class);
		assertTrue(laboratory.isPresent());
	}

	@Test(expected = AccessControlException.class)
	public void userHasNoRightToDeleteLaboratory() {
		assertTrue(accessControlService.evaluate(EvACE.of(ILaboratory.class, Right.READ)));
		assertFalse(accessControlService.evaluate(EvACE.of(ILaboratory.class, Right.DELETE)));

		Optional<ILaboratory> load = CoreModelServiceHolder.get().load(AllServiceTests.getLaboratory().getId(),
				ILaboratory.class);
		CoreModelServiceHolder.get().delete(load.get()); // or other exception
	}

	@Test
	public void userHasRightToLoadPerson() {
		assertTrue(accessControlService.evaluate(EvACE.of(IPerson.class, Right.READ)));
		assertTrue(accessControlService.evaluate(EvACE.of(IPerson.class, Right.VIEW)));

		Optional<IPerson> load = CoreModelServiceHolder.get().load(AllServiceTests.getPatient().getId(),
				IPerson.class);
		assertTrue(load.isPresent());
	}

	@Test(expected = AccessControlException.class)
	public void userHasNoRightToUpdateArticle() {
		assertTrue(accessControlService.evaluate(EvACE.of(IArticle.class, Right.READ)));
		assertTrue(accessControlService.evaluate(EvACE.of(IArticle.class, Right.VIEW)));
		assertFalse(accessControlService.evaluate(EvACE.of(IArticle.class, Right.UPDATE)));

		IArticle iArticle = CoreModelServiceHolder.get().load(AllServiceTests.getEigenartikel().getId(), IArticle.class)
				.get();
		iArticle.setName("this-should-not-work");
		CoreModelServiceHolder.get().save(iArticle); // better exception than IllegalState??

		CoreModelServiceHolder.get().touch(iArticle); // should fail too - better exception than IllegalState??
	}
	
	@Test(expected = AccessControlException.class)
	public void userHasNoRightToTouchArticle() {
		assertTrue(accessControlService.evaluate(EvACE.of(IArticle.class, Right.READ)));
		assertTrue(accessControlService.evaluate(EvACE.of(IArticle.class, Right.VIEW)));
		assertFalse(accessControlService.evaluate(EvACE.of(IArticle.class, Right.UPDATE)));

		IArticle iArticle = CoreModelServiceHolder.get().load(AllServiceTests.getEigenartikel().getId(), IArticle.class)
				.get();
		CoreModelServiceHolder.get().touch(iArticle);
	}
	
	@Test
	public void namedQueryExecution() {
		// ? CoreModelServiceHolder.get().getNamedQuery
	}

	@Test
	public void checkOut() {
		EvaluatableACE accountingGlobal = EvACEs.ACCOUNTING_GLOBAL;
	}
	
}
