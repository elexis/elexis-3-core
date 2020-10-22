package ch.elexis.core.services;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.utils.OsgiServiceUtil;

@RunWith(Suite.class)
@SuiteClasses({
	AccessControlServiceTest.class, IAppointmentServiceTest.class, IBillingServiceTest.class,
	IConfigServiceTest.class, IElexisEnvironmentServiceTest.class, ILabServiceTest.class,
	IStoreToStringServiceTest.class, IStickerServiceTest.class,
	IUserServiceTest.class, IMessageServiceTest.class, IVirtualFilesystemServiceTest.class,
	IXidServiceTest.class, IMedicationServiceTest.class, ITextReplacementServiceTest.class
})
public class AllServiceTests {
	
	private static IModelService modelService;
	private static IElexisEntityManager entityManager;
	
	private static TestDatabaseInitializer tdb;
	
	@BeforeClass
	public static void beforeClass() throws IOException, SQLException{
		
		modelService = OsgiServiceUtil.getService(IModelService.class,
			"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
		
		tdb = new TestDatabaseInitializer(modelService, entityManager);
		tdb.initializePatient();
		tdb.initializeMandant();
		tdb.initializeLabResult();
		tdb.initializePrescription();
	}
	
	public static IModelService getModelService(){
		return modelService;
	}
	
	public static IPatient getPatient(){
		return tdb.getPatient();
	}
	
	public static ILaboratory getLaboratory(){
		return tdb.getLaboratory();
	}
	
	public static IArticle getEigenartikel(){
		return tdb.getArticle();
	}
	
	public static ICoverage getCoverage(){
		return tdb.getFall();
	}
	
	public static IMandator getMandator(){
		return tdb.getMandant();
	}

	public static IUser getUser(){
		return tdb.getUser();
	}
}
