package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class DayMessageTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void createFindDelete() {
		LocalDate date = LocalDate.of(2018, 9, 24);

		IDayMessage dayMessage = coreModelService.create(IDayMessage.class);
		dayMessage.setTitle("title");
		dayMessage.setMessage("the message");

		dayMessage.setDate(date);
		coreModelService.save(dayMessage);

		Optional<IDayMessage> loaded = coreModelService.load("20180924", IDayMessage.class);
		assertTrue(loaded.isPresent());
		assertEquals(date, loaded.get().getDate());
		assertEquals("the message", loaded.get().getMessage());
		assertEquals("title", loaded.get().getTitle());

		coreModelService.remove(dayMessage);
	}
}
