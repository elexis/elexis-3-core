package ch.elexis.core.fhir.model.adapter;

import static org.junit.Assert.assertNotNull;

import org.hl7.fhir.r4.model.Task;
import org.junit.Test;

import ch.elexis.core.model.Identifiable;

public class ModelAdapterFactoryTest {

	@Test
	public void createAdapter() {
		ModelAdapterFactory factory = new ModelAdapterFactory();

		Task task = new Task();
		Identifiable adapter = factory.createAdapter(task);
		assertNotNull(adapter);
	}
}
