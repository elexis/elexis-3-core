package ch.elexis.core.test.initializer;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.builder.ILabItemBuilder;
import ch.elexis.core.model.builder.ILabResultBuilder;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.test.util.TestUtil;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.VersionedResource;

public class TestDatabaseInitializer {

	private static Logger logger = LoggerFactory.getLogger(TestDatabaseInitializer.class);

	private static boolean isDbInitialized = false;

	private static IUser user;

	private static boolean isPatientInitialized = false;
	private static IPatient patient;

	private static boolean isOrganizationInitialized = false;
	private static IOrganization organization;

	private static boolean isMandantInitialized = false;
	private static IMandator mandant;

	private static boolean isUserGrouüInitialized = false;
	private static IUserGroup userGroup;

	private ILaboratory laboratory;
	private ILaboratory laboratory2;

	private static boolean isArticleInitialized;
	private static IArticle article;

	private static boolean isFallInitialized = false;
	private static ICoverage fall;

	private static boolean isBehandlungInitialized = false;
	private static IEncounter behandlung;

	private static boolean isILabResultInitialized = false;
	private static List<ILabResult> labResults = new ArrayList<>();
	private static ILabItem labItem;

	private static boolean isPrescriptionInitialized = false;
	private static IPrescription prescription;

	private static boolean isArtikelstammTableInitialized = false;

	private static boolean isLaborItemsOrdersResultsInitialized = false;

	private static boolean isLaborTarif2009Initialized = false;
	private static boolean isTarmedInitialized = false;
	private static boolean isPhysioLeistungInitialized = false;

	private static boolean isAgendaInitialized = false;
	private static boolean isRemindersInitialized = false;
	private static boolean isLeistungsblockInitialized = false;
	private ISickCertificate sickCertificate;
	private static boolean isAUFInitialized = false;

	private IModelService modelService;
	private IElexisEntityManager entityManager;
	private IConfigService configService;

	private static IXidService xidService;

	public TestDatabaseInitializer(IModelService modelService, IElexisEntityManager entityManager) {
		this.modelService = modelService;
		this.entityManager = entityManager;
	}

	public TestDatabaseInitializer() {
		modelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
	}

	public synchronized TestDatabaseInitializer initializeDb() throws IOException, SQLException {
		initializeDb(configService);
		user = modelService.load("user", IUser.class).get();
		return this;
	}

	public void setConfigService(IConfigService configService) {
		this.configService = configService;
	}

	/**
	 *
	 * @param configService if not <code>null</code> - initializes the system
	 *                      configuration
	 * @throws IOException
	 * @throws SQLException
	 */
	public TestDatabaseInitializer initializeDb(IConfigService configService) throws IOException, SQLException {
		if (!isDbInitialized) {
			entityManager.getEntityManager(); // lazy initialize the database
			// initialize
			executeScript("test_initUser", "/rsc/dbScripts/User.sql");
			executeScript("test_initRoles", "/rsc/dbScripts/Role.sql");
			executeScript("test_initSampleContacts", "/rsc/dbScripts/sampleContacts.sql");
			executeScript("test_initBillingVKPreise", "/rsc/dbScripts/BillingVKPreise.sql");
			executeScript("test_initStickers", "/rsc/dbScripts/Etiketten.sql");
			if (configService != null) {
				new ConfigInitializer().initializeConfiguration(configService);
			}
			isDbInitialized = true;
		} else {
			logger.error("No connection available!");
		}
		return this;
	}

	public synchronized void initializeArtikelstammTable() throws IOException {
		if (!isArtikelstammTableInitialized) {
			isArtikelstammTableInitialized = executeScript("test_initArtikelstammItem",
					"/rsc/dbScripts/ArtikelstammItem.sql");
		}
	}

	private boolean executeScript(String liquibase_id, String scriptLocation) throws IOException {
		boolean result = entityManager.executeSQLScript(liquibase_id, TestUtil.loadFile(getClass(), scriptLocation));
		if (!result) {
			throw new IOException("Error executing script in [" + scriptLocation + "]");
		}
		return true;
	}

	public synchronized void initializeAgendaTable() throws IOException, SQLException {
		initializeDb();
		if (!isAgendaInitialized) {
			isAgendaInitialized = executeScript("test_initAgenda", "/rsc/dbScripts/Agenda.sql");
		}
	}

	public synchronized void initializeLaborTarif2009Tables() throws IOException, SQLException {
		initializeDb();
		if (!isLaborTarif2009Initialized) {
			isLaborTarif2009Initialized = executeScript("test_initLaborTarif2009", "/rsc/dbScripts/LaborTarif2009.sql");
		}
	}

	public synchronized void initializeTarmedTables() throws IOException, SQLException {
		initializeDb();
		if (!isTarmedInitialized) {
			isTarmedInitialized = executeScript("test_initTarmed", "/rsc/dbScripts/Tarmed.sql");
			isTarmedInitialized = executeScript("test_initTarmedKumulation", "/rsc/dbScripts/TarmedKumulation.sql");
			isTarmedInitialized = executeScript("test_initTarmedExtension", "/rsc/dbScripts/TarmedExtension.sql");
			isTarmedInitialized = executeScript("test_initTarmedGroup", "/rsc/dbScripts/TarmedGroup.sql");
			isTarmedInitialized = executeScript("test_initTarmedDefinitionen", "/rsc/dbScripts/TarmedDefinitionen.sql");
		}
	}

	public synchronized void initializeArzttarifePhysioLeistungTables() throws IOException, SQLException {
		initializeDb();
		if (!isPhysioLeistungInitialized) {
			isPhysioLeistungInitialized = executeScript("test_initArzttarifePhysio",
					"/rsc/dbScripts/ArzttarifePhysio.sql");
		}
	}

	public synchronized void initializeLeistungsblockTables() throws IOException, SQLException {
		initializeDb();
		if (!isLeistungsblockInitialized) {
			isLeistungsblockInitialized = executeScript("test_initLeistungsblock", "/rsc/dbScripts/Leistungsblock.sql");
		}
	}

	/**
	 * Initializes an intrinsic consistent set of ILabItems, ILabResults and
	 * LabOrders
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	public synchronized void initializeLaborItemsOrdersResults() throws IOException, SQLException {
		initializeDb();
		if (!isLaborItemsOrdersResultsInitialized) {
			isLaborItemsOrdersResultsInitialized = executeScript("test_LaborItemsWerteResults",
					"/rsc/dbScripts/LaborItemsWerteResults.sql");
		}
	}

	public synchronized void initializeReminders() throws IOException, SQLException {
		initializeDb();
		if (!isRemindersInitialized) {
			isRemindersInitialized = executeScript("test_Reminder", "/rsc/dbScripts/Reminder.sql");
		}
	}

	/**
	 * Initialize a test Patient.
	 *
	 * <li>Firstname: Test</li>
	 * <li>Lastname: Patient</li>
	 * <li>DateofBirth: 1.1.1990</li>
	 * <li>Gender: FEMALE</li>
	 * <li>Phone1: +01555123</li>
	 * <li>Mobile: +01444123</li>
	 * <li>City: City</li>
	 * <li>Zip: 123</li>
	 * <li>Street: Street 1</li>
	 * <li>Xid AHV: 756...</li>
	 * <li>Diagnosen: Test Diagnose 1\nTest Diagnose 2</li>
	 *
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	public synchronized void initializePatient() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isPatientInitialized) {
			patient = new IContactBuilder.PatientBuilder(modelService, "Test", "Patient", LocalDate.of(1990, 1, 1),
					Gender.FEMALE).build();
			patient.setPhone1("+01555123");
			patient.setMobile("+01444123");
			patient.setCity("City");
			patient.setZip("123");
			patient.setStreet("Street 1");
			patient.setDiagnosen("Test Diagnose 1\nTest Diagnose 2");
			modelService.save(patient);

			addAHVNumber(patient, 1);
			isPatientInitialized = true;
		}
	}

	/**
	 * Get the initialized Patient
	 *
	 * @return
	 */
	public static IPatient getPatient() {
		return patient;
	}

	public static void addAHVNumber(IPatient kontakt, int index) {
		String country = "756";
		String number = String.format("%09d", index);
		StringBuilder ahvBuilder = new StringBuilder(country + number);
		ahvBuilder.append(getAHVCheckNumber(ahvBuilder.toString()));

		kontakt.addXid(XidConstants.DOMAIN_AHV, ahvBuilder.toString(), true);
	}

	private static String getAHVCheckNumber(String string) {
		int sum = 0;
		for (int i = 0; i < string.length(); i++) {
			// reverse order
			char character = string.charAt((string.length() - 1) - i);
			int intValue = Character.getNumericValue(character);
			if (i % 2 == 0) {
				sum += intValue * 3;
			} else {
				sum += intValue;
			}
		}
		return Integer.toString(sum % 10);
	}

	/**
	 * Initialize a test Organization.
	 *
	 * <li>Description1: Test Organization</li>
	 * <li>Lastname: Test</li>
	 * <li>Phone1: +01555345</li>
	 * <li>Mobile: +01444345</li>
	 * <li>City: City</li>
	 * <li>Zip: 123</li>
	 * <li>Street: Street 10</li>
	 *
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	public synchronized void initializeOrganization() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isOrganizationInitialized) {
			organization = new IContactBuilder.OrganizationBuilder(modelService, "Test Organization").build();
			organization.setPhone1("+01555345");
			organization.setMobile("+01444345");

			organization.setCity("City");
			organization.setZip("123");
			organization.setStreet("Street 10");

			modelService.save(organization);
			isOrganizationInitialized = true;
		}
	}

	public static IOrganization getOrganization() {
		return organization;
	}

	/**
	 * Initialize a test Mandant.
	 *
	 * <li>Firstname: Test</li>
	 * <li>Lastname: Mandant</li>
	 * <li>DateofBirth: 1.1.1970</li>
	 * <li>Gender: MALE</li>
	 * <li>Phone1: +01555234</li>
	 * <li>Mobile: +01444234</li>
	 * <li>City: City</li>
	 * <li>Zip: 123</li>
	 * <li>Street: Street 100</li>
	 * <li>EAN: 2000000000002</li>
	 * <li>KSK: C000002</li>
	 *
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	public synchronized void initializeMandant() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}

		if (!isMandantInitialized) {
			IPerson mandantPerson = new IContactBuilder.PersonBuilder(modelService, "Test", "Mandant",
					LocalDate.of(1970, 1, 1), Gender.MALE).mandator().buildAndSave();
			mandantPerson.setMandator(true);
			mandant = modelService.load(mandantPerson.getId(), IMandator.class).get();
			mandant.setPhone1("+01555234");
			mandant.setMobile("+01444234");

			mandant.setCity("City");
			mandant.setZip("123");
			mandant.setStreet("Street 100");
			mandant.setUser(true);
			modelService.save(mandant);

			user = new IUserBuilder(modelService, "tst", mandantPerson).buildAndSave();
			modelService.save(user);

			mandant.addXid(XidConstants.DOMAIN_EAN, "2000000000002", true);
			mandant.addXid("www.xid.ch/id/ksk", "C000002", true);
			isMandantInitialized = true;
		}
	}

	public static IMandator getMandant() {
		return mandant;
	}

	public static IUser getUser() {
		return user;
	}

	public void initializeUserGroup() throws IOException, SQLException {
		if (!isMandantInitialized) {
			initializeMandant();
		}

		if (!isUserGrouüInitialized) {
			userGroup = new IUserGroupBuilder(modelService, "TestGroup").build();
			userGroup.addUser(user);
			modelService.save(userGroup);
			isUserGrouüInitialized = true;
		}
	}

	/**
	 * Initialize an test Prescription.
	 *
	 * <li>Article: see
	 * {@link TestDatabaseInitializer#initializeArtikelstamm()}</li>
	 * <li>Patient: see {@link TestDatabaseInitializer#initializePatient()}</li>
	 * <li>Dosage: 1-1-1-1</li>
	 *
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	public synchronized void initializePrescription() throws IOException, SQLException {
		if (!isDbInitialized) {
			initializeDb();
		}
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isMandantInitialized) {
			initializeMandant();
		}

		if (!isArticleInitialized) {
			article = modelService.create(IArticle.class);
			article.setName("test article");
			article.setCode("123456789");
			article.setTyp(ArticleTyp.EIGENARTIKEL);
			article.setGtin("0000001111111");
			article.setPackageSize(12);
			article.setSellingSize(12);
			article.setPurchasePrice(new Money(100));
			article.setSellingPrice(new Money(150));
			modelService.save(article);
			isArticleInitialized = true;

		}

		if (!isPrescriptionInitialized) {
			prescription = new IPrescriptionBuilder(modelService, null, article, patient, "1-1-1-1").build();
			prescription.setPrescriptor(mandant);
			modelService.save(prescription);

			isPrescriptionInitialized = true;
		}
	}

	/**
	 * Initialize a test Fall.
	 *
	 * <li>Patient: {@link TestDatabaseInitializer#getPatient()}</li>
	 * <li>Label: "Test Fall"</li>
	 * <li>Reason: "reason"</li>
	 * <li>BillingMethod: "method"</li>
	 * <li>KostentrKontakt: {@link TestDatabaseInitializer#getOrganization()}</li>
	 * <li>VersNummer: 1234-5678</li>
	 * <li>DatumVon: 1.9.2016</li>
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	public synchronized void initializeFall() throws IOException, SQLException {
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isOrganizationInitialized) {
			initializeOrganization();
		}
		if (!isFallInitialized) {
			fall = new ICoverageBuilder(modelService, patient, "Test Fall", "reason", "method").build();
			fall.setCostBearer(organization);
			fall.setInsuranceNumber("1234-5678");
			fall.setDateFrom(LocalDate.of(2016, Month.SEPTEMBER, 1));
			modelService.save(fall);
			patient = modelService.load(patient.getId(), IPatient.class).get();
			isFallInitialized = true;
		}
	}

	public static ICoverage getFall() {
		return fall;
	}

	/**
	 * Initialize a test Behandlung.
	 *
	 * <li>Patient: {@link TestDatabaseInitializer#getPatient()}</li>
	 * <li>ServiceProvider: {@link TestDatabaseInitializer#getMandant()}</li>
	 * <li>Datum: 21.9.2016</li>
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	public void initializeBehandlung() throws IOException, SQLException {
		if (!isMandantInitialized) {
			initializeMandant();
		}
		if (!isFallInitialized) {
			initializeFall();
		}
		if (!isBehandlungInitialized) {
			behandlung = new IEncounterBuilder(modelService, getFall(), getMandant()).buildAndSave();
			behandlung.setDate(LocalDate.of(2016, Month.SEPTEMBER, 21));
			VersionedResource vr = VersionedResource.load(null);
			vr.update("Test consultation\nWith some test text.", "Administrator");
			vr.update("Test consultation\n pdate done by user", "user");
			behandlung.setVersionedEntry(vr);
			modelService.save(behandlung);
			isBehandlungInitialized = true;
		}
	}

	public void initializeAUF() throws IOException, SQLException {
		if (!isFallInitialized) {
			initializeFall();
		}
		if (!isAUFInitialized) {
			sickCertificate = modelService.create(ISickCertificate.class);
			sickCertificate.setPatient(patient);
			sickCertificate.setCoverage(fall);
			sickCertificate.setDate(LocalDate.now());
			sickCertificate.setStart(LocalDate.now());
			sickCertificate.setEnd(LocalDate.now().plusDays(7));
			sickCertificate.setNote("note");
			sickCertificate.setReason("Krankheit");
			sickCertificate.setPercent(75);
			modelService.save(sickCertificate);
		}
	}

	public ISickCertificate getAUFs() {
		return sickCertificate;
	}

	public static IEncounter getBehandlung() {
		return behandlung;
	}

	/**
	 * Initialize a test ILabResults.
	 *
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	public synchronized void initializeLabResult() throws IOException, SQLException {
		if (!isPatientInitialized) {
			initializePatient();
		}
		if (!isILabResultInitialized) {
			getXidService().localRegisterXIDDomainIfNotExists(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY,
					"Sendende Institution", XidConstants.ASSIGNMENT_LOCAL);

			laboratory = new IContactBuilder.LaboratoryBuilder(modelService, "Labor Test").build();
			laboratory.setDescription2("Test");
			modelService.save(laboratory);

			laboratory2 = new IContactBuilder.LaboratoryBuilder(modelService, "Labor Test2").build();
			laboratory2.setDescription2("Test2");
			assertTrue(laboratory2.addXid(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, "ZURANA", true));
			modelService.save(laboratory2);

			labItem = new ILabItemBuilder(modelService, "TEST NUMERIC", "Test Laboratory", ">1", "3-3.5", "unit",
					LabItemTyp.NUMERIC, "group", 1).origin(laboratory, "TEST NUMERIC", true).buildAndSave();
			labItem.setExport("vitolabkey:1,2");
			modelService.save(labItem);

			ILabMapping mapping = modelService.create(ILabMapping.class);
			mapping.setOrigin(laboratory2);
			mapping.setItemName("TEST_NUMERIC_EXT");
			modelService.save(mapping);
			modelService.save(labItem);

			ILabItem textILabItem = new ILabItemBuilder(modelService, "TEST TEXT", "Test Laboratory", null, null,
					"unit", LabItemTyp.TEXT, "group", 2).origin(laboratory, "Test Laboratory", true).buildAndSave();
			modelService.save(labItem);

			ILabResult labResult = new ILabResultBuilder(modelService, labItem, patient).build();

			labResult.setObservationTime(LocalDateTime.of(2016, Month.DECEMBER, 14, 17, 44, 25));
			labResult.setUnit("u");
			labResult.setReferenceMale("<1");
			labResult.setReferenceFemale("1-1.5");
			labResult.setOrigin(laboratory);
			labResult.setResult("2");
			labResult.setComment("no comment");
			modelService.save(labResult);
			labResults.add(labResult);

			labResult = new ILabResultBuilder(modelService, labItem, patient).build();
			labResult.setObservationTime(LocalDateTime.of(2016, Month.DECEMBER, 15, 10, 10, 30));
			labResult.setOrigin(laboratory);
			labResult.setResult("2");
			labResult.setComment("no comment");
			modelService.save(labResult);
			labResults.add(labResult);

			labResult = new ILabResultBuilder(modelService, labItem, patient).build();
			labResult.setObservationTime(LocalDateTime.of(2017, Month.FEBRUARY, 28, 12, 59, 23));
			labResult.setOrigin(laboratory);
			labResult.setResult("124/79");
			labResult.setUnit("Bloodpressure");
			modelService.save(labResult);
			labResults.add(labResult);

			labResult = new ILabResultBuilder(modelService, textILabItem, patient).build();
			labResult.setObservationTime(LocalDateTime.of(2017, Month.FEBRUARY, 28, 10, 02, 23));
			labResult.setOrigin(laboratory);
			labResult.setResult("(Text)");
			labResult.setComment("The Text Result ...");
			modelService.save(labResult);
			labResults.add(labResult);

			isILabResultInitialized = true;
		}
	}

	public static IXidService getXidService() {
		if (xidService == null) {
			xidService = OsgiServiceUtil.getService(IXidService.class)
					.orElseThrow(() -> new IllegalStateException("No XidService available"));
		}
		return xidService;
	}

	public ILabItem getILabItem() {
		return labItem;
	}

	public ILaboratory getLaboratory() {
		return laboratory;
	}

	public ILaboratory getLaboratory2() {
		return laboratory2;
	}

	public static List<ILabResult> getLabResults() {
		return labResults;
	}

	public IArticle getArticle() {
		return article;
	}

	public IUserGroup getUserGroup() {
		return userGroup;
	}
}
