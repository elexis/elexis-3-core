package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;

public class EncounterTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createUserSetActiveInContext();
		super.createMandator();
		super.createPatient();
		super.createCoverage();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void createFindDeleteEncounter() {
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		LocalDate date = LocalDate.of(2018, Month.SEPTEMBER, 21);
		encounter.setDate(date);
		VersionedResource vr = VersionedResource.load(null);
		vr.update("Test consultation\nWith some test text.", "Administrator");
		vr.update("Test consultation\n pdate done by user", "user");
		encounter.setVersionedEntry(vr);
		coreModelService.save(encounter);

		IQuery<IEncounter> query = coreModelService.getQuery(IEncounter.class);
		query.and(ModelPackage.Literals.IENCOUNTER__COVERAGE, COMPARATOR.EQUALS, coverage);
		assertEquals(encounter, query.executeSingleResult().get());
		assertEquals(date, encounter.getDate());
		assertTrue(encounter.getVersionedEntry().getHead(),
				encounter.getVersionedEntry().getHead().contains("done by user"));

		coreModelService.delete(encounter);
	}

	@Test
	public void addRemoveDiagnosis() {
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		IDiagnosisReference diagnosis = coreModelService.create(IDiagnosisReference.class);
		diagnosis.setCode("test");
		coreModelService.save(diagnosis);
		IDiagnosisReference diagnosis1 = coreModelService.create(IDiagnosisReference.class);
		diagnosis.setCode("test1");
		coreModelService.save(diagnosis1);

		encounter.addDiagnosis(diagnosis);
		encounter.addDiagnosis(diagnosis1);
		coreModelService.save(encounter);

		Optional<IEncounter> loaded = coreModelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertEquals(encounter.getDiagnoses().size(), loaded.get().getDiagnoses().size());

		encounter.removeDiagnosis(diagnosis1);
		coreModelService.save(encounter);

		loaded = coreModelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertEquals(encounter.getDiagnoses().size(), loaded.get().getDiagnoses().size());

		encounter.removeDiagnosis(diagnosis);
		coreModelService.save(encounter);
		loaded = coreModelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertTrue(loaded.get().getDiagnoses().isEmpty());

		coreModelService.remove(diagnosis);
		coreModelService.remove(diagnosis1);
		coreModelService.remove(encounter);
	}

	@Test
	public void addRemoveBilled() {
		ICustomService service = coreModelService.create(ICustomService.class);
		service.setCode("12.34");
		service.setNetPrice(new Money(1));
		service.setPrice(new Money(2));
		service.setText("test");
		coreModelService.save(service);

		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		coreModelService.save(encounter);

		Result<IBilled> result = service.getOptifier().add(service, encounter, 1.5);
		assertTrue(result.isOK());

		assertFalse(encounter.getBilled().isEmpty());
		Optional<IEncounter> loaded = coreModelService.load(encounter.getId(), IEncounter.class);
		assertEquals(loaded.get(), encounter);
		assertFalse(loaded.get().getBilled().isEmpty());
		IBilled billed = loaded.get().getBilled().get(0);
		assertEquals(1.5, billed.getAmount(), 0.01);
		assertEquals(service.getText(), billed.getText());
		assertEquals(service, billed.getBillable());

		encounter.removeBilled(billed);
		assertFalse(encounter.getBilled().contains(billed));
		assertTrue(billed.isDeleted());

		coreModelService.remove(service);
		coreModelService.remove(encounter);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void modifyBilled() {
		ICustomService service = coreModelService.create(ICustomService.class);
		service.setCode("12.34");
		service.setNetPrice(new Money(1));
		service.setPrice(new Money(2));
		service.setText("test");
		coreModelService.save(service);

		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		coreModelService.save(encounter);

		// add billed
		Result<IBilled> result = service.getOptifier().add(service, encounter, 1.5);
		assertTrue(result.isOK());
		IBilled billed = encounter.getBilled().get(0);
		assertEquals(1.5, billed.getAmount(), 0.01);
		// change property and save -> results in new entity which leads to problem
		// (reason for caching lists in Adapters)
		billed.setText("changed text");
		coreModelService.save(billed);
		// add amount
		result = service.getOptifier().add(service, encounter, 1.5);
		assertEquals(3, billed.getAmount(), 0.01);
		billed = encounter.getBilled().get(0);
		assertEquals(3, billed.getAmount(), 0.01);

		coreModelService.remove(service);
		coreModelService.remove(encounter);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void multiThreadMappedProperties() throws InterruptedException {
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		coreModelService.save(encounter);

		ExecutorService executor = Executors.newFixedThreadPool(3);

		for (int i = 0; i < 100; i++) {
			final int number = i;
			executor.execute(() -> {
				contextService.setActiveUser(user);

				ICustomService service = coreModelService.create(ICustomService.class);
				service.setCode("code" + number);
				service.setNetPrice(new Money(number));
				service.setPrice(new Money(number));
				service.setText("test" + number);
				coreModelService.save(service);

				Result<IBilled> result = service.getOptifier().add(service, encounter, 1);
				assertNotNull(result);
				assertTrue(result.isOK());
			});
			executor.execute(() -> {
				if (!encounter.getVersionedEntry().update("Test consultation\nmulti billing " + number,
						"Administrator")) {
					fail();
				}
				coreModelService.save(encounter);
			});
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);
		// test if size will be correct after max 5 seconds
		int retryCount = 50;
		while (encounter.getBilled().size() < 100 && --retryCount > 0) {
			Thread.sleep(100);
			coreModelService.refresh(encounter, true);
		}
		assertEquals(100, encounter.getBilled().size());
		assertTrue(encounter.getLastupdate() > 0);
		for (IBilled billed : encounter.getBilled()) {
			assertTrue(billed.getLastupdate() > 0);
		}

		coreModelService.remove(encounter);

		IQuery<ICustomService> query = coreModelService.getQuery(ICustomService.class);
		for (ICustomService service : query.execute()) {
			coreModelService.remove(service);
		}
	}

	@Test
	public void changeCoverage() {
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		LocalDate date = LocalDate.of(2018, Month.SEPTEMBER, 21);
		encounter.setDate(date);
		coreModelService.save(encounter);

		IEncounter encounter1 = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();
		LocalDate date1 = LocalDate.of(2018, Month.SEPTEMBER, 22);
		encounter.setDate(date1);
		coreModelService.save(encounter1);

		assertEquals(2, coverage.getEncounters().size());

		ICoverage coverage1 = new ICoverageBuilder(coreModelService, patient, "testCoverage1", "testReason1",
				"testBillingSystem1").buildAndSave();
		encounter1.setCoverage(coverage1);
		coreModelService.save(encounter1);

		assertEquals(1, coverage1.getEncounters().size());
		assertEquals(1, coverage.getEncounters().size());

		coreModelService.delete(encounter);
	}

	@Test
	public void addAndUpdateVersionsEntry() {
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).buildAndSave();

		VersionedResource vr = VersionedResource.load(null);
		vr.update("TESTME", "Administrator");
		encounter.setVersionedEntry(vr);
		coreModelService.save(encounter);
		assertEquals("TESTME", encounter.getVersionedEntry().getHead());

		encounter.getVersionedEntry().update("changed", "");
		coreModelService.save(encounter);

		assertEquals("changed", encounter.getVersionedEntry().getHead());
		assertEquals("changed",
				coreModelService.load(encounter.getId(), IEncounter.class).get().getVersionedEntry().getHead());
		assertEquals("changed", coreModelService.load(encounter.getId(), IEncounter.class, true, true).get()
				.getVersionedEntry().getHead());

	}
}
