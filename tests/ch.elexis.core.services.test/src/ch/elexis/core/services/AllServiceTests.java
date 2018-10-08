package ch.elexis.core.services;

import java.time.LocalDate;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

@RunWith(Suite.class)
@SuiteClasses({
	LabServiceTest.class
})
public class AllServiceTests {
	
	private static IModelService modelService;
	
	private static IPatient patient;
	
	@BeforeClass
	public static void beforeClass(){
		
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		
		LocalDate dob = LocalDate.of(2016, 9, 1);
		patient = (IPatient) new IContactBuilder.PatientBuilder(modelService, "TestPatient",
			"TestPatient", dob, Gender.MALE).buildAndSave();
	}
	
	public static IModelService getModelService(){
		return modelService;
	}
	
	public static IPatient getPatient(){
		return patient;
	}
}
