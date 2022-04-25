package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.DocumentStatus;

public class DocumentLetterTest extends AbstractTest {

	private IContact contact1;
	private IPatient patient1;

	@Override
	@Before
	public void before() {
		super.before();
		contact1 = coreModelService.create(IContact.class);
		contact1.setDescription1("test contact 1");
		coreModelService.save(contact1);
		patient1 = coreModelService.create(IPatient.class);
		patient1.setDescription1("test patient 2");
		coreModelService.save(patient1);
	}

	@Override
	@After
	public void after() {
		coreModelService.remove(contact1);
		coreModelService.remove(patient1);
		super.after();
	}

	@Test
	public void create() throws IOException {
		IDocumentLetter letter = coreModelService.create(IDocumentLetter.class);
		assertNotNull(letter);
		assertTrue(letter instanceof IDocumentLetter);
		assertEquals(Collections.singletonList(DocumentStatus.NEW), letter.getStatus());

		letter.setDescription("test letter 1");
		letter.setAuthor(contact1);
		letter.setContent(new ByteArrayInputStream("test content".getBytes()));
		coreModelService.save(letter);

		Optional<IDocumentLetter> loadedLetter = coreModelService.load(letter.getId(), IDocumentLetter.class);
		assertTrue(loadedLetter.isPresent());
		assertFalse(letter == loadedLetter.get());
		assertEquals(letter, loadedLetter.get());
		assertEquals(letter.getDescription(), loadedLetter.get().getDescription());
		try (ByteArrayOutputStream contentByteArray = new ByteArrayOutputStream();
				InputStream contentStream = letter.getContent()) {
			IOUtils.copy(contentStream, contentByteArray);
			assertEquals("test content", new String(contentByteArray.toByteArray()));
		}
		coreModelService.remove(letter);
	}

	@Test
	public void query() throws IOException {
		IDocumentLetter letter1 = coreModelService.create(IDocumentLetter.class);
		letter1.setDescription("test letter 1");
		letter1.setAuthor(contact1);
		letter1.setContent(new ByteArrayInputStream("test content 1".getBytes()));
		coreModelService.save(letter1);
		IDocumentLetter letter2 = coreModelService.create(IDocumentLetter.class);
		letter2.setDescription("test letter 2");
		letter2.setAuthor(contact1);
		letter2.setPatient(patient1);
		letter2.setContent(new ByteArrayInputStream("test content 2".getBytes()));
		coreModelService.save(letter2);

		IQuery<IDocumentLetter> query = coreModelService.getQuery(IDocumentLetter.class);
		query.and(ModelPackage.Literals.IDOCUMENT__PATIENT, COMPARATOR.EQUALS, patient1);
		List<IDocumentLetter> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertFalse(letter2 == existing.get(0));
		assertEquals(letter2, existing.get(0));
		assertEquals(letter2.getDescription(), existing.get(0).getDescription());
		IOUtils.contentEquals(letter2.getContent(), existing.get(0).getContent());

		coreModelService.remove(letter1);
		coreModelService.remove(letter2);
	}

	@Test
	public void storeAndReadExternal() throws IOException {
		Path tempDirectory = Files.createTempDirectory(getClass().getName() + "_test");
		ConfigServiceHolder.get().set(Preferences.P_TEXT_EXTERN_FILE, true);
		ConfigServiceHolder.get().set(Preferences.P_TEXT_EXTERN_FILE_PATH, tempDirectory.toUri().toString());

		IDocumentLetter letter1 = coreModelService.create(IDocumentLetter.class);
		letter1.setDescription("test letter external");
		letter1.setPatient(patient1);
		letter1.setAuthor(contact1);
		letter1.setMimeType("txt");
		letter1.setContent(new ByteArrayInputStream("My name is Mr. Me seeks, look at me".getBytes()));
		coreModelService.save(letter1);

		File file = tempDirectory.resolve(patient1.getPatientNr()).resolve(letter1.getId() + ".txt").toFile();
		assertTrue(file.exists());

		byte[] content;
		try (InputStream is = letter1.getContent()) {
			content = IOUtils.toByteArray(is);
		}
		assertTrue(new String(content).startsWith("My name is"));

		FileUtils.deleteDirectory(tempDirectory.toFile());
		ConfigServiceHolder.get().set(Preferences.P_TEXT_EXTERN_FILE, false);
	}

}
