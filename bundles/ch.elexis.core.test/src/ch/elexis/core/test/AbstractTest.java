package ch.elexis.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;

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
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;

public abstract class AbstractTest {

	protected IModelService coreModelService;
	protected IContextService contextService;

	protected IUser user;
	protected IPerson person;
	protected IMandator mandator;
	protected IPatient patient;
	protected ICoverage coverage;
	protected IEncounter encounter;
	protected IArticle localArticle;

	@Before
	public void before() {
		coreModelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		contextService = OsgiServiceUtil.getService(IContextService.class).get();

		contextService.setActiveUser(TestDatabaseInitializer.getUser());
		contextService.setActiveMandator(TestDatabaseInitializer.getMandant());
	}

	/**
	 * Removes all model instance fields.
	 *
	 */
	@After
	public void after() {

		contextService.setActiveUser(null);

		if (encounter != null) {
			coreModelService.remove(encounter);
		}
		if (coverage != null) {
			coreModelService.remove(coverage);
		}
		if (localArticle != null) {
			coreModelService.remove(localArticle);
		}
		if (user != null) {
			coreModelService.remove(user);
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

		List<IPerson> execute = coreModelService.getQuery(IPerson.class, true, true).execute();
		int size = execute.size();
		if (size > 0) {
			LoggerFactory.getLogger(getClass()).warn("multiple IPerson [{}]", execute.size(), new Throwable());
		}
		OsgiServiceUtil.ungetService(coreModelService);
		coreModelService = null;
	}

	/**
	 * includes {@link #createPerson()}
	 */
	public void createUserSetActiveInContext() {
		if (person == null) {
			createPerson();
		}
		user = new IUserBuilder(coreModelService, "b_a_barracus", person).buildAndSave();

		contextService.setActiveUser(user);
	}

	public IPerson createPerson() {
		LocalDate dob = LocalDate.of(1979, 7, 26);
		person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson", dob, Gender.FEMALE)
				.buildAndSave();
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());
		assertFalse(person.isMandator());
		return person;
	}

	public void createMandator() {
		mandator = new IContactBuilder.MandatorBuilder(coreModelService, "TestMandator", "TestMandator").buildAndSave();
		assertFalse(mandator.isPerson());
		assertFalse(mandator.isPatient());
		assertFalse(mandator.isOrganization());
		assertFalse(mandator.isLaboratory());
		assertTrue(mandator.isMandator());
	}

	public void createPatient() {
		LocalDate dob = LocalDate.of(2016, 9, 1);
		patient = new IContactBuilder.PatientBuilder(coreModelService, "TestPatient", "TestPatient", dob, Gender.MALE)
				.buildAndSave();
		assertTrue(patient.isPerson());
		assertTrue(patient.isPatient());
		assertFalse(patient.isOrganization());
		assertFalse(patient.isLaboratory());
		assertFalse(patient.isMandator());
	}

	public static final String KVG_NAME = Messages.Case_KVG_Short;
	public static final String UVG_NAME = Messages.Case_UVG_Short;
	public static final String MV_NAME = Messages.Fall_MV_Name;
	public static final String IV_NAME = Messages.Fall_IV_Name;
	private static final String KVG_REQUIREMENTS = Messages.Fall_KVGRequirements;
	public static final String UVG_REQUIREMENTS = Messages.Fall_UVGRequirements;
	public static final String CONST_TARMED_DRUCKER = Messages.Fall_TarmedPrinter;
	public static final String CONST_TARMED_LEISTUNG = Messages.Fall_TarmedLeistung;
	public static final String VVG_NAME = Messages.Fall_VVG_Name;
	public static final String PRIVATE_NAME = Messages.Case_Privat_Short;

	public void createCoverage() {
		IBillingSystemService billingSystemService = OsgiServiceUtil.getService(IBillingSystemService.class).get();
		billingSystemService.addOrModifyBillingSystem(KVG_NAME, CONST_TARMED_DRUCKER, KVG_REQUIREMENTS, BillingLaw.KVG);
		billingSystemService.addOrModifyBillingSystem(UVG_NAME, CONST_TARMED_DRUCKER, UVG_REQUIREMENTS, BillingLaw.UVG);

		if (patient == null) {
			createPatient();
		}
		coverage = new ICoverageBuilder(coreModelService, patient, "testCoverage", "testReason", "KVG").buildAndSave();
	}

	public void createEncounter() {
		if (coverage == null) {
			createCoverage();
		}
		if (mandator == null) {
			createMandator();
		}
		encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
	}

	public void createLocalArticle() {
		localArticle = new IArticleBuilder(coreModelService, "test article", "123456789", ArticleTyp.EIGENARTIKEL)
				.build();
		localArticle.setGtin("0000001111111");
		localArticle.setPackageSize(12);
		localArticle.setSellingSize(12);
		localArticle.setSellingPrice(new Money(150));
		localArticle.setPurchasePrice(new Money(100));
		coreModelService.save(localArticle);
	}
}
