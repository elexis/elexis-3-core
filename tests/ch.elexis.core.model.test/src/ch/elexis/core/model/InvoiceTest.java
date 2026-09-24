package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IAccountTransactionBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.builder.IPaymentBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.rgw.tools.Money;

public class InvoiceTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createEncounter();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void create() {

		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		coreModelService.save(invoice);

		// attachments
		assertTrue(Long.valueOf(invoice.getNumber()) > 0);
		assertEquals(0, invoice.getAttachments().size());
		IDocumentLetter document = coreModelService.create(IDocumentLetter.class);
		document.setAuthor(mandator);
		coreModelService.save(document);
		invoice.addAttachment(document);
		assertEquals(1, invoice.getAttachments().size());
		invoice.removeAttachment(invoice.getAttachments().get(0));
		assertEquals(0, invoice.getAttachments().size());

		coreModelService.remove(document);
		coreModelService.remove(invoice);
	}

	@Test
	public void queryByCoverage() {

		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		coreModelService.save(invoice);

		IQuery<IInvoice> query = coreModelService.getQuery(IInvoice.class);
		query.and(ModelPackage.Literals.IINVOICE__COVERAGE, COMPARATOR.EQUALS, coverage);
		List<IInvoice> result = query.execute();

		assertTrue(result.contains(invoice));

		coreModelService.remove(invoice);
	}

	@Test
	public void getSortedEncounters() throws InterruptedException {
		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		coreModelService.save(invoice);

		List<Identifiable> created = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).build();
			encounter.setDate(LocalDate.now().minusDays(i));
			encounter.setInvoice(invoice);
			coreModelService.save(encounter);
			created.add(encounter);
		}
		// two encounters yesterday
		IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).build();
		encounter.setDate(LocalDate.now().minusDays(1));
		encounter.setInvoice(invoice);
		coreModelService.save(encounter);
		created.add(encounter);

		// reload from db
		invoice = coreModelService.load(invoice.getId(), IInvoice.class).get();
		List<IEncounter> encounters = invoice.getEncounters();
		// check if sorted as expected
		assertEquals(11, encounters.size());
		assertEquals(LocalDate.now(), encounters.get(10).getDate());
		assertEquals(encounters.get(8).getDate(), encounters.get(9).getDate());

		coreModelService.remove(invoice);

		created.forEach(i -> coreModelService.remove(i));
	}

	@Test
	public void multiThreadMappedProperties() throws InterruptedException {
		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		coreModelService.save(invoice);

		List<Identifiable> created = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(3);
		for (int i = 0; i < 100; i++) {
			final int number = i;
			executor.execute(() -> {
				IInvoiceBilled invoiceBilled = coreModelService.create(IInvoiceBilled.class);
				invoiceBilled.setInvoice(invoice);
				coreModelService.save(invoiceBilled);
				created.add(invoiceBilled);
			});
			executor.execute(() -> {
				IEncounter encounter = new IEncounterBuilder(coreModelService, coverage, mandator).build();
				encounter.setInvoice(invoice);
				coreModelService.save(encounter);
				created.add(encounter);
			});
			executor.execute(() -> {
				IPayment payment = new IPaymentBuilder(coreModelService, invoice, new Money(number), "test")
						.buildAndSave();
				created.add(payment);
			});
			executor.execute(() -> {
				IAccountTransaction transaction = new IAccountTransactionBuilder(coreModelService, invoice, patient,
						new Money(number), LocalDate.now(), "test").buildAndSave();
				created.add(transaction);
			});
			executor.execute(() -> {
				invoice.setRemark("test " + number);
				coreModelService.save(invoice);
			});
		}
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);
		assertEquals(100, invoice.getBilled().size());
		assertEquals(100, invoice.getEncounters().size());
		assertEquals(100, invoice.getPayments().size());
		assertEquals(100, invoice.getTransactions().size());
		assertTrue(invoice.getRemark().startsWith("test "));

		coreModelService.remove(invoice);

		created.forEach(i -> coreModelService.remove(i));
	}
}
