package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class InvoiceTest extends AbstractTest {
	
	@Override
	@Before
	public void before(){
		super.before();
		super.createEncounter();
	}
	
	@Override
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void create(){
		
		IInvoice invoice = coreModelService.create(IInvoice.class);
		invoice.setCoverage(coverage);
		invoice.setDate(LocalDate.now());
		invoice.setDateFrom(LocalDate.now());
		invoice.setRemark("remark");
		assertTrue(coreModelService.save(invoice));
		
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
	
}
