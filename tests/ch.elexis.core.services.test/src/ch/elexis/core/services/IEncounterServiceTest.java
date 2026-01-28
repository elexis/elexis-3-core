package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class IEncounterServiceTest extends AbstractServiceTest {

	private IEncounter encounter;

	private IEncounterService encounterService = OsgiServiceUtil.getService(IEncounterService.class).get();

	private IBillingService billingService = OsgiServiceUtil.getService(IBillingService.class).get();

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		encounter = testEncounters.get(0);
	}

	@After
	public void after() {
		cleanup();
	}

	@Ignore("QUARANTINE #24743")
	@Test
	public void multiThreadUpdate() throws InterruptedException {

		ExecutorService executor = Executors.newFixedThreadPool(3);

		for (int i = 0; i < 100; i++) {
			final int number = i;
			executor.execute(() -> {
				ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
				ContextServiceHolder.get().setActiveMandator(testMandators.get(0));

				ICustomService service = coreModelService.create(ICustomService.class);
				service.setCode("code" + number);
				service.setNetPrice(new Money(number));
				service.setPrice(new Money(number));
				service.setText("test" + number);
				coreModelService.save(service);

				billingService.bill(service, encounter, 1.0);
			});
			executor.execute(() -> {
				encounterService.updateVersionedEntry(encounter, "Test consultation\nmulti update " + number,
						"Administrator");
			});
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);
		coreModelService.refresh(encounter, true);
		assertEquals(100, encounter.getBilled().size());
		assertTrue(encounter.getVersionedEntry().getHeadVersion() > 1);
		// IEncounterService#updateVersionedEntry is not thread save but better than
		// direct modify without refresh ...
		// enable if there is some locking to ensure thread safety
		// assertEquals(99, encounter.getVersionedEntry().getHeadVersion());

		IQuery<ICustomService> query = coreModelService.getQuery(ICustomService.class);
		for (ICustomService service : query.execute()) {
			coreModelService.remove(service);
		}
	}

	@Test
	public void getLatestEncounter() {

		ICoverage iCoverage = testCoverages.get(0);
		testCoverages.remove(iCoverage);
		coreModelService.remove(iCoverage);
		IEncounter iEncounter = testEncounters.get(0);
		testEncounters.remove(iEncounter);
		coreModelService.remove(iEncounter);

		Optional<IEncounter> encounter = encounterService.getLatestEncounter(testPatients.get(0));
		assertFalse(encounter.isPresent());

		encounter = encounterService.getLatestEncounter(testPatients.get(0), true);
		assertTrue(encounter.isPresent());
		assertNotNull(encounter.get().getCoverage());
		assertEquals(testPatients.get(0), encounter.get().getPatient());

		coreModelService.remove(encounter.get().getCoverage());
		coreModelService.remove(encounter.get());
	}

	@Test
	public void reBillEncounter() {
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));

		for (int number = 1; number < 10; number++) {
			ICustomService service = coreModelService.create(ICustomService.class);
			service.setCode("code" + number);
			service.setNetPrice(new Money(number));
			service.setPrice(new Money(number));
			service.setText("test" + number);
			coreModelService.save(service);

			billingService.bill(service, encounter, 1.0);
		}

		int billedSize = encounter.getBilled().size();

		Result<IEncounter> result = encounterService.reBillEncounter(encounter);

		assertTrue(result.isOK());
		assertEquals(billedSize, encounter.getBilled().size());
	}
}
