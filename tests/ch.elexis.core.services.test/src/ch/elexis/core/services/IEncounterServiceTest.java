package ch.elexis.core.services;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;

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
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> encounter.getBilled().size() == 100);
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
}
