package ch.elexis.core.services;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.utils.OsgiServiceUtil;

@RunWith(Suite.class)
@SuiteClasses({ AccessControlServiceTest.class, IConfigServiceTest.class, ILabServiceTest.class,
		IStoreToStringServiceTest.class, IUserServiceTest.class, IStickerServiceTest.class, IAppointmentServiceTest.class,
		IVirtualFilesystemServiceTest.class, IXidServiceTest.class})
public class AllServiceTests {

	private static IModelService modelService;
	private static IElexisEntityManager entityManager;

	private static TestDatabaseInitializer tdb;

	@BeforeClass
	public static void beforeClass() throws IOException, SQLException {

		modelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();

		tdb = new TestDatabaseInitializer(modelService, entityManager);
		tdb.initializePatient();
		tdb.initializeLabResult();
		tdb.initializePrescription();
	}

	public static IModelService getModelService() {
		return modelService;
	}

	public static IPatient getPatient() {
		return tdb.getPatient();
	}
	
	public static ILaboratory getLaboratory() {
		return tdb.getLaboratory();
	}
	
	public static IArticle getEigenartikel() {
		return tdb.getArticle();
	}
}
