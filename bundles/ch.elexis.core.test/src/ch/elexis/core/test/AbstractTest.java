package ch.elexis.core.test;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public abstract class AbstractTest {
	
	public IModelService coreModelService;
	
	public IPerson person;
	public IMandator mandator;
	public IPatient patient;
	public ICoverage coverage;
	public IEncounter encounter;
	public IArticle localArticle;
	
	@Before
	public void before(){
		coreModelService = OsgiServiceUtil.getService(IModelService.class,
			"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(coreModelService);
		coreModelService = null;
	}
	
	public void createPerson(){
		LocalDate dob = LocalDate.of(2016, 9, 1);
		person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson",
			dob, Gender.FEMALE).buildAndSave();
	}
	
	public void createMandator(){
		LocalDate dob = LocalDate.of(1979, 7, 26);
		person = new IContactBuilder.PersonBuilder(coreModelService, "TestMandator", "TestMandator",
			dob, Gender.FEMALE).mandator().buildAndSave();
	}
	
	public void createPatient(){
		LocalDate dob = LocalDate.of(2016, 9, 1);
		patient = (IPatient) new IContactBuilder.PatientBuilder(coreModelService, "TestPatient",
			"TestPatient", dob, Gender.MALE).buildAndSave();
	}
	
	public void createCoverage(){
		if (patient == null) {
			createPatient();
		}
		coverage = new ICoverageBuilder(coreModelService, patient, "testCoverage", "testReason",
			"testBillingSystem").buildAndSave();
	}
	
	public void createEncounter(){
		if (coverage == null) {
			createCoverage();
		}
		if (mandator == null) {
			createMandator();
		}
		encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
	}
	
	public void createLocalArticle(){
		localArticle = new IArticleBuilder(coreModelService, "test article", "123456789",
			ArticleTyp.EIGENARTIKEL).build();
		localArticle.setGtin("0000001111111");
		localArticle.setPackageSize(12);
		localArticle.setSellingSize(12);
		coreModelService.save(localArticle);
	}
	
	public void removePerson(){
		coreModelService.remove(person);
	}
	
	public void removePatient(){
		coreModelService.remove(patient);
	}
	
	public void removeCoverage(){
		coreModelService.remove(coverage);
	}
	
	public void removeLocalArticle(){
		coreModelService.remove(localArticle);
	}
}
