package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class IInvoiceServiceTest extends AbstractServiceTest {
	private IBillingService billingService =
		OsgiServiceUtil.getService(IBillingService.class).get();
	private IInvoiceService invoiceService =
		OsgiServiceUtil.getService(IInvoiceService.class).get();
	
	private static ICustomService customService;
	private static IEncounter encounter;
	
	@BeforeClass
	public static void beforeClass(){
		customService = coreModelService.create(ICustomService.class);
		customService.setText("test service");
		customService.setCode("1234");
		customService.setNetPrice(new Money(512));
		customService.setPrice(new Money(1024));
		coreModelService.save(customService);
		
		encounter = new IEncounterBuilder(CoreModelServiceHolder.get(),
			AllServiceTests.getCoverage(), AllServiceTests.getMandator()).buildAndSave();
	}
	
	@AfterClass
	public static void afterClass(){
		CoreModelServiceHolder.get().remove(encounter);
	}
	
	@Before
	public void before(){
		createTestMandantPatientFallBehandlung();
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
	}
	
	@After
	public void after(){
		ContextServiceHolder.get().setActiveMandator(null);
		
		cleanup();
	}
	
	@Test
	public void removePayment(){
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(),
			ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
		
		Result<IBilled> billed = billingService.bill(customService, encounter, 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		coreModelService.save(diagnosis);
		testEncounters.get(0).addDiagnosis(diagnosis);
		coreModelService.save(testEncounters.get(0));
		Result<IInvoice> invoice = invoiceService.invoice(testEncounters);
		assertTrue(invoice.toString(), invoice.isOK());

		IQuery<IAccountTransaction> transactionQuery =
				CoreModelServiceHolder.get().getQuery(IAccountTransaction.class);
		transactionQuery.and(ModelPackage.Literals.IACCOUNT_TRANSACTION__PATIENT, COMPARATOR.EQUALS, invoice.get().getCoverage().getPatient());
		assertTrue(transactionQuery.execute().isEmpty());

		
		IPayment payment = invoiceService.addPayment(invoice.get(), new Money(128), "test");
		assertNotNull(payment);
		assertEquals(1.28, invoice.get().getPayedAmount().getAmount(), 0.0001);
		assertFalse(transactionQuery.execute().isEmpty());
		
		invoiceService.removePayment(payment);
		assertEquals(0.0, invoice.get().getPayedAmount().getAmount(), 0.0001);
		assertTrue(transactionQuery.execute().isEmpty());
		
		CoreModelServiceHolder.get().remove(payment);
		CoreModelServiceHolder.get().remove(billed.get());
		CoreModelServiceHolder.get().remove(invoice.get());
	}
}
