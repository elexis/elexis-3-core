package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class DocumentBriefTest {
	private IModelService modelSerice;
	
	private IContact contact1;
	private IPatient patient1;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		
		contact1 = modelSerice.create(IContact.class);
		contact1.setDescription1("test contact 1");
		modelSerice.save(contact1);
		patient1 = modelSerice.create(IPatient.class);
		patient1.setDescription1("test patient 2");
		modelSerice.save(patient1);
	}
	
	@After
	public void after(){
		modelSerice.remove(contact1);
		modelSerice.remove(patient1);
		
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create() throws IOException{
		IDocumentLetter letter = modelSerice.create(IDocumentLetter.class);
		assertNotNull(letter);
		assertTrue(letter instanceof IDocumentLetter);
		
		letter.setDescription("test letter 1");
		letter.setAuthor(contact1);
		letter.setContent(new ByteArrayInputStream("test content".getBytes()));
		assertTrue(modelSerice.save(letter));
		
		Optional<IDocumentLetter> loadedLetter =
			modelSerice.load(letter.getId(), IDocumentLetter.class);
		assertTrue(loadedLetter.isPresent());
		assertFalse(letter == loadedLetter.get());
		assertEquals(letter, loadedLetter.get());
		assertEquals(letter.getDescription(), loadedLetter.get().getDescription());
		try (ByteArrayOutputStream contentByteArray = new ByteArrayOutputStream(); InputStream contentStream = letter.getContent()) {
			IOUtils.copy(contentStream, contentByteArray);
			assertEquals("test content", new String(contentByteArray.toByteArray()));
		}
		modelSerice.remove(letter);
	}
	
	@Test
	public void query() throws IOException{
		IDocumentLetter letter1 = modelSerice.create(IDocumentLetter.class);
		letter1.setDescription("test letter 1");
		letter1.setAuthor(contact1);
		letter1.setContent(new ByteArrayInputStream("test content 1".getBytes()));
		assertTrue(modelSerice.save(letter1));
		IDocumentLetter letter2 = modelSerice.create(IDocumentLetter.class);
		letter2.setDescription("test letter 2");
		letter2.setAuthor(contact1);
		letter2.setPatient(patient1);
		letter2.setContent(new ByteArrayInputStream("test content 2".getBytes()));
		assertTrue(modelSerice.save(letter2));
		
		IQuery<IDocumentLetter> query = modelSerice.getQuery(IDocumentLetter.class);
		query.add(ModelPackage.Literals.IDOCUMENT__PATIENT, COMPARATOR.EQUALS, patient1);
		List<IDocumentLetter> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertFalse(letter2 == existing.get(0));
		assertEquals(letter2, existing.get(0));
		assertEquals(letter2.getDescription(), existing.get(0).getDescription());
		IOUtils.contentEquals(letter2.getContent(), existing.get(0).getContent());
		
		modelSerice.remove(letter1);
		modelSerice.remove(letter2);
	}
}
