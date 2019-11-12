package ch.elexis.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.IBillingSystemService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public abstract class AbstractTest {
	
	protected IModelService coreModelService;
	
	protected IUser user;
	protected IPerson person;
	protected IMandator mandator;
	protected IPatient patient;
	protected ICoverage coverage;
	protected IEncounter encounter;
	protected IArticle localArticle;
	
	@Before
	public void before(){
		coreModelService = OsgiServiceUtil.getService(IModelService.class,
			"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	}
	
	/**
	 * Removes all model instance fields.
	 * 
	 */
	@After
	public void after(){
		if (encounter != null) {
			coreModelService.remove(encounter);
		}
		if (coverage != null) {
			coreModelService.remove(coverage);
		}
		if (localArticle != null) {
			coreModelService.remove(localArticle);
		}
		if (person != null) {
			coreModelService.remove(person);
		}
		if (mandator != null) {
			coreModelService.remove(mandator);
		}
		if (patient != null) {
			coreModelService.remove(patient);
		}
		if (user != null) {
			coreModelService.remove(user);
		}
		
		OsgiServiceUtil.ungetService(coreModelService);
		coreModelService = null;
	}
	
	public void createUser(){
		if (person == null) {
			createPerson();
		}
		user = new IUserBuilder(coreModelService, "b_a_barracus", person).buildAndSave();
	}
	
	public void createPerson(){
		LocalDate dob = LocalDate.of(2016, 9, 1);
		person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson",
			dob, Gender.FEMALE).buildAndSave();
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());
		assertFalse(person.isMandator());
	}
	
	public void createMandator(){
		LocalDate dob = LocalDate.of(1979, 7, 26);
		person = new IContactBuilder.PersonBuilder(coreModelService, "TestMandator", "TestMandator",
			dob, Gender.FEMALE).mandator().buildAndSave();
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());
		assertTrue(person.isMandator());
	}
	
	public void createPatient(){
		LocalDate dob = LocalDate.of(2016, 9, 1);
		patient = (IPatient) new IContactBuilder.PatientBuilder(coreModelService, "TestPatient",
			"TestPatient", dob, Gender.MALE).buildAndSave();
		assertTrue(patient.isPerson());
		assertTrue(patient.isPatient());
		assertFalse(patient.isOrganization());
		assertFalse(patient.isLaboratory());
		assertFalse(patient.isMandator());
	}
	
	public static final String KVG_NAME = Messages.Fall_KVG_Name;
	public static final String UVG_NAME = Messages.Fall_UVG_Name;
	public static final String MV_NAME = Messages.Fall_MV_Name;
	public static final String IV_NAME = Messages.Fall_IV_Name;
	private static final String KVG_REQUIREMENTS = Messages.Fall_KVGRequirements; //$NON-NLS-1$
	public static final String UVG_REQUIREMENTS = Messages.Fall_UVGRequirements; //$NON-NLS-1$
	public static final String CONST_TARMED_DRUCKER = Messages.Fall_TarmedPrinter; //$NON-NLS-1$
	public static final String CONST_TARMED_LEISTUNG = Messages.Fall_TarmedLeistung; //$NON-NLS-1$
	public static final String VVG_NAME = Messages.Fall_VVG_Name;
	public static final String PRIVATE_NAME = Messages.Fall_Private_Name; //$NON-NLS-1$	
	
	public void createCoverage(){
		IBillingSystemService billingSystemService = OsgiServiceUtil.getService(IBillingSystemService.class).get();
		billingSystemService.addOrModifyBillingSystem(KVG_NAME, CONST_TARMED_LEISTUNG, CONST_TARMED_DRUCKER, KVG_REQUIREMENTS, BillingLaw.KVG);
		billingSystemService.addOrModifyBillingSystem(UVG_NAME, CONST_TARMED_LEISTUNG, CONST_TARMED_DRUCKER, UVG_REQUIREMENTS, BillingLaw.UVG);
		
		if (patient == null) {
			createPatient();
		}
		coverage = new ICoverageBuilder(coreModelService, patient, "testCoverage", "testReason",
			"KVG").buildAndSave();
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
}
