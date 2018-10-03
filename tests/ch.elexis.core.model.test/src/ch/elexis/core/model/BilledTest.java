package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.rgw.tools.Money;

public class BilledTest extends AbstractTest {
	
	@Test
	public void create(){
		IBilled billed = modelService.create(IBilled.class);
		billed.setAmount(2.5);
		billed.setPrice(new Money(2));
		assertTrue(modelService.save(billed));
		
		Optional<IBilled> loadedBilled = modelService.load(billed.getId(), IBilled.class);
		assertTrue(loadedBilled.isPresent());
		assertFalse(billed == loadedBilled.get());
		assertEquals(billed, loadedBilled.get());
		assertEquals(billed.getAmount(), loadedBilled.get().getAmount(), 0.01);
		assertEquals(billed.getPrice(), loadedBilled.get().getPrice());
		
		modelService.remove(billed);
	}
}
