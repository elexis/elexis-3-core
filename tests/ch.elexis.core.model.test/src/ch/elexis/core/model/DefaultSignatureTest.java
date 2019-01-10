package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.test.AbstractTest;

public class DefaultSignatureTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		createPatient();
		createMandator();
		createLocalArticle();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void create(){
		IArticleDefaultSignature defaultSignature =
			coreModelService.create(IArticleDefaultSignature.class);
		assertNotNull(defaultSignature);
		assertTrue(defaultSignature instanceof IArticleDefaultSignature);
		
		defaultSignature.setArticle(localArticle);
		defaultSignature.setDisposalType(EntryType.FIXED_MEDICATION);
		defaultSignature.setMorning("1");
		defaultSignature.setEvening("1");
		coreModelService.save(defaultSignature);
		
		Optional<IArticleDefaultSignature> loaded =
			coreModelService.load(defaultSignature.getId(), IArticleDefaultSignature.class);
		assertTrue(loaded.isPresent());
		assertFalse(defaultSignature == loaded.get());
		assertEquals(defaultSignature, loaded.get());
		assertEquals(defaultSignature.getMorning(), loaded.get().getMorning());
		assertEquals(defaultSignature.getNoon(), loaded.get().getNoon());
		assertEquals(defaultSignature.getDisposalType(), loaded.get().getDisposalType());
		assertEquals("1-0-1-0", loaded.get().getSignatureAsDosisString());
		
		coreModelService.remove(defaultSignature);
	}
}
