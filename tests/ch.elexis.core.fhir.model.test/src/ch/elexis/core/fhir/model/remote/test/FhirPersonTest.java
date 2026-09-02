package ch.elexis.core.fhir.model.remote.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.fhir.model.interfaces.IFhirBased;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.ICompositeModelService;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;

public class FhirPersonTest {

	ICompositeModelService compositeModelService;

	@Before
	public void beforeAll() {
		compositeModelService = PortableServiceLoader.get(ICompositeModelService.class);
	}

	@Test
	public void queryAccessorReloadTest() {
		List<IPatient> execute = compositeModelService.getQuery(IPatient.class)
				.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "Musterman%")
				.orderBy(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, ORDER.ASC).execute();
		assertEquals(2, execute.size());

		IPatient patient = execute.get(0);
		assertEquals("455d6d11f16d4509bde58bd10", patient.getId());
		assertEquals("Mustermann Max (m), 01.01.1990", patient.getLabel());
		// TODO sticker should add meta somehow
		// stickers with coverages directly in CSS? or narrative.meta.tags

		assertTrue(((IFhirBased) patient).isSubsetted());
		assertEquals(1990, execute.get(0).getDateOfBirth().getYear());
		assertFalse(((IFhirBased) patient).isSubsetted());
	}

	@Test
	public void loadModifySave() {
		IPerson person = compositeModelService.load("71042abe54b7453cba4eabfc8", IPerson.class).get();
		assertFalse(((IFhirBased) person).isSubsetted());
		assertEquals(1999, person.getDateOfBirth().getYear());
		assertEquals("71042abe54b7453cba4eabfc8", person.getId());

		long timeMillis = System.currentTimeMillis();
		person.setComment(System.currentTimeMillis() + " testvalue");
		compositeModelService.save(person);

		person = compositeModelService.load("71042abe54b7453cba4eabfc8", IPerson.class).get();
		assertTrue(person.getComment(), person.getComment().startsWith(Long.toString(timeMillis)));
	}

}
