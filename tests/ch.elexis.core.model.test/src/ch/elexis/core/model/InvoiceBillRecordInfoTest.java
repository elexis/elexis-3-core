package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;

public class InvoiceBillRecordInfoTest extends AbstractTest {

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

		IBilled billed = coreModelService.create(IBilled.class);
		billed.setEncounter(encounter);
		billed.setText("test");
		coreModelService.save(billed);

		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		coreModelService.save(invoice);

		IInvoiceBillRecordInfo info = coreModelService.create(IInvoiceBillRecordInfo.class);
		info.setBilled(billed);
		info.setInvoice(invoice);
		info.setBillid("test_bill_id");
		info.setBillrecordid("test_bill_record_id");
		coreModelService.save(info);

		Optional<IInvoiceBillRecordInfo> loaded = coreModelService.load(info.getId(), IInvoiceBillRecordInfo.class);
		assertTrue(loaded.isPresent());
		assertEquals(billed, loaded.get().getBilled());
		assertEquals(invoice, loaded.get().getInvoice());
		assertEquals("test_bill_id", loaded.get().getBillid());
		assertEquals("test_bill_record_id", loaded.get().getBillrecordid());

		coreModelService.remove(info);
		coreModelService.remove(billed);
		coreModelService.remove(invoice);
	}

	@Test
	public void queryByInvoiceAndBill() {

		IBilled billed = coreModelService.create(IBilled.class);
		billed.setEncounter(encounter);
		billed.setText("test");
		coreModelService.save(billed);

		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		coreModelService.save(invoice);

		IInvoiceBillRecordInfo info = coreModelService.create(IInvoiceBillRecordInfo.class);
		info.setBilled(billed);
		info.setInvoice(invoice);
		info.setBillid("test_bill_id");
		info.setBillrecordid("test_bill_record_id");
		coreModelService.save(info);

		IQuery<IInvoiceBillRecordInfo> query = coreModelService.getQuery(IInvoiceBillRecordInfo.class);
		query.and(ModelPackage.Literals.IINVOICE_BILL_RECORD_INFO__INVOICE, COMPARATOR.EQUALS, invoice);
		query.and(ModelPackage.Literals.IINVOICE_BILL_RECORD_INFO__BILLED, COMPARATOR.EQUALS, billed);
		List<IInvoiceBillRecordInfo> result = query.execute();

		assertTrue(result.contains(info));

		query = coreModelService.getQuery(IInvoiceBillRecordInfo.class);
		query.and(ModelPackage.Literals.IINVOICE_BILL_RECORD_INFO__BILLID, COMPARATOR.EQUALS, "test_bill_id");
		query.and(ModelPackage.Literals.IINVOICE_BILL_RECORD_INFO__BILLRECORDID, COMPARATOR.EQUALS,
				"test_bill_record_id");
		result = query.execute();

		assertTrue(result.contains(info));

		coreModelService.remove(info);
		coreModelService.remove(billed);
		coreModelService.remove(invoice);
	}
}
