package ch.elexis.core.model;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public abstract class AbstractTest {
	
	IModelService modelService;
	
	IPatient patient;
	ICoverage coverage;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}
	
	public void createPatient(){
		LocalDate dob = LocalDate.of(2016, 9, 1);
		patient =
			(IPatient) new IContactBuilder.PatientBuilder(modelService, "", "", dob, Gender.MALE)
				.buildAndSave();
	}
	
	public void createCoverage(){
		coverage = new ICoverageBuilder(modelService, patient, "testCoverage", "testReason",
			"testBillingSystem").buildAndSave();
	}
	
	public void removePatient(){
		modelService.remove(patient);
	}
	
	public void removeCoverage() {
		modelService.remove(coverage);
	}
}
