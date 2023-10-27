package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class IInvoiceServiceTest extends AbstractServiceTest {
	private IBillingService billingService = OsgiServiceUtil.getService(IBillingService.class).get();
	private IInvoiceService invoiceService = OsgiServiceUtil.getService(IInvoiceService.class).get();

	private static ICustomService customService;

	@BeforeClass
	public static void beforeClass() {
		customService = coreModelService.create(ICustomService.class);
		customService.setText("test service");
		customService.setCode("1234");
		customService.setNetPrice(new Money(512));
		customService.setPrice(new Money(1024));
		coreModelService.save(customService);
	}

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
	}

	@After
	public void after() {
		ContextServiceHolder.get().setActiveMandator(null);

		cleanup();
	}

	@Test
	public void updateInvoiceState() {
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(),
				ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

		Result<IBilled> billed = billingService.bill(customService, testEncounters.get(0), 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		coreModelService.save(diagnosis);
		testEncounters.get(0).addDiagnosis(diagnosis);
		coreModelService.save(testEncounters.get(0));
		Result<IInvoice> invoice = invoiceService.invoice(testEncounters);
		assertTrue(invoice.toString(), invoice.isOK());

		IQuery<IAccountTransaction> transactionQuery = CoreModelServiceHolder.get().getQuery(IAccountTransaction.class);
		transactionQuery.and(ModelPackage.Literals.IACCOUNT_TRANSACTION__PATIENT, COMPARATOR.EQUALS,
				invoice.get().getCoverage().getPatient());
		assertEquals(1, transactionQuery.execute().size());

		IPayment payment = invoiceService.addPayment(invoice.get(), new Money(1024), "test");
		assertNotNull(payment);
		assertEquals(10.24, invoice.get().getPayedAmount().getAmount(), 0.0001);
		assertEquals(2, transactionQuery.execute().size());
		assertEquals(InvoiceState.PAID, invoice.get().getState());

		CoreModelServiceHolder.get().remove(payment);
		CoreModelServiceHolder.get().remove(billed.get());
		CoreModelServiceHolder.get().remove(invoice.get());
	}

	@Test
	public void removePayment() {
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(),
				ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

		Result<IBilled> billed = billingService.bill(customService, testEncounters.get(0), 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		coreModelService.save(diagnosis);
		testEncounters.get(0).addDiagnosis(diagnosis);
		coreModelService.save(testEncounters.get(0));
		Result<IInvoice> invoice = invoiceService.invoice(testEncounters);
		assertTrue(invoice.toString(), invoice.isOK());

		IQuery<IAccountTransaction> transactionQuery = CoreModelServiceHolder.get().getQuery(IAccountTransaction.class);
		transactionQuery.and(ModelPackage.Literals.IACCOUNT_TRANSACTION__PATIENT, COMPARATOR.EQUALS,
				invoice.get().getCoverage().getPatient());
		assertEquals(1, transactionQuery.execute().size());

		IPayment payment = invoiceService.addPayment(invoice.get(), new Money(128), "test");
		assertNotNull(payment);
		assertEquals(1.28, invoice.get().getPayedAmount().getAmount(), 0.0001);
		assertEquals(2, transactionQuery.execute().size());
		assertEquals(InvoiceState.PARTIAL_PAYMENT, invoice.get().getState());

		invoiceService.removePayment(payment);
		assertEquals(0.0, invoice.get().getPayedAmount().getAmount(), 0.0001);
		assertEquals(1, transactionQuery.execute().size());

		CoreModelServiceHolder.get().remove(payment);
		CoreModelServiceHolder.get().remove(billed.get());
		CoreModelServiceHolder.get().remove(invoice.get());
	}

	@Test
	public void getInvoiceCancel() {
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(),
				ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

		Result<IBilled> billed = billingService.bill(customService, testEncounters.get(0), 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		coreModelService.save(diagnosis);
		testEncounters.get(0).addDiagnosis(diagnosis);
		coreModelService.save(testEncounters.get(0));
		// invoice
		Result<IInvoice> invoice = invoiceService.invoice(testEncounters);
		assertTrue(invoice.toString(), invoice.isOK());

		IQuery<IAccountTransaction> transactionQuery = CoreModelServiceHolder.get().getQuery(IAccountTransaction.class);
		transactionQuery.and("invoice", COMPARATOR.EQUALS, invoice.get());
		List<IAccountTransaction> transactions = transactionQuery.execute();
		IQuery<IPayment> paymentQuery = CoreModelServiceHolder.get().getQuery(IPayment.class);
		paymentQuery.and("invoice", COMPARATOR.EQUALS, invoice.get());
		List<IPayment> payments = paymentQuery.execute();
		assertEquals(1, transactions.size());
		assertEquals(0, payments.size());
		// cancel
		invoiceService.cancel(invoice.get(), true);
		transactions = transactionQuery.execute();
		payments = paymentQuery.execute();
		assertEquals(2, transactions.size());
		assertEquals(1, payments.size());

		CoreModelServiceHolder.get().remove(billed.get());
		List<IInvoice> invoices = invoiceService.getInvoices(testEncounters.get(0));
		assertEquals(1, invoices.size());

		CoreModelServiceHolder.get().remove(billed.get());
		invoices.forEach(i -> {
			CoreModelServiceHolder.get().remove(i);
		});
	}

	@Test
	public void getInvoices() {
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(),
				ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

		Result<IBilled> billed = billingService.bill(customService, testEncounters.get(0), 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		coreModelService.save(diagnosis);
		testEncounters.get(0).addDiagnosis(diagnosis);
		coreModelService.save(testEncounters.get(0));
		// invoice
		Result<IInvoice> invoice = invoiceService.invoice(testEncounters);
		assertTrue(invoice.toString(), invoice.isOK());
		// cancel
		invoiceService.cancel(invoice.get(), true);
		// invoice
		invoice = invoiceService.invoice(testEncounters);

		List<IInvoice> invoices = invoiceService.getInvoices(testEncounters.get(0));
		assertEquals(2, invoices.size());

		// test if null invoice reference is filtered
		INamedQuery<IInvoiceBilled> query = CoreModelServiceHolder.get().getNamedQuery(IInvoiceBilled.class,
				"encounter");
		List<IInvoiceBilled> invoicebilled = query
				.executeWithParameters(query.getParameterMap("encounter", testEncounters.get(0)));
		invoicebilled.get(0).setInvoice(null);
		coreModelService.save(invoicebilled.get(0));
		invoices = invoiceService.getInvoices(testEncounters.get(0));
		assertEquals(1, invoices.size());

		CoreModelServiceHolder.get().remove(billed.get());
		invoices.forEach(i -> {
			CoreModelServiceHolder.get().remove(i);
		});

	}
}
