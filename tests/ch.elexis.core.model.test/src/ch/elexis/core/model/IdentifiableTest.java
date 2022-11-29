package ch.elexis.core.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class IdentifiableTest extends AbstractTest {

	@Test
	public void touch() throws InterruptedException {
		IPerson person = super.createPerson();
		Long untouched = person.getLastupdate();
		Thread.sleep(10);
		coreModelService.touch(person);
		Long touched = person.getLastupdate();
		assertTrue(touched > untouched);
	}

}
