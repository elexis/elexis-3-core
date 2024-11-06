package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IContactServiceTest extends AbstractServiceTest {

	private IContactService contactService = OsgiServiceUtil.getService(IContactService.class).get();

	@Test
	public void findPersonFuzzy() {
		IPerson testPerson = new IContactBuilder.PersonBuilder(coreModelService, "Arnold", "Schwoarzenegger",
				LocalDate.of(1947, 7, 30), Gender.MALE).buildAndSave();

		List<IPerson> findPersonDuplicates = contactService.findPersonFuzzy(LocalDate.of(1947, 7, 30), Gender.MALE,
				"Schwarzenegger", "Arnld", false);
		assertEquals(1, findPersonDuplicates.size());

		coreModelService.remove(testPerson);
	}

}
