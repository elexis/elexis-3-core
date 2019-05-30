package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.test.AbstractTest;
import ch.rgw.tools.Money;

public class BilledTest extends AbstractTest {
	
	@Test
	public void create(){
		IBilled billed = coreModelService.create(IBilled.class);
		billed.setAmount(1);
		// use components to set price, setPrice method is interpreted as manual price change
		billed.setPoints(2);
		billed.setFactor(1);
		billed.setPrimaryScale(100);
		billed.setSecondaryScale(100);
		
		assertFalse(billed.isNonIntegerAmount());
		assertFalse(billed.isChangedPrice());
		assertEquals(0.02, billed.getPrice().doubleValue(), 0.001);
		assertEquals(1.0, billed.getAmount(), 0.001);
		assertEquals(0.02, billed.getTotal().doubleValue(), 0.001);
		
		assertTrue(coreModelService.save(billed));
		
		Optional<IBilled> loadedBilled = coreModelService.load(billed.getId(), IBilled.class);
		assertTrue(loadedBilled.isPresent());
		assertFalse(billed == loadedBilled.get());
		assertEquals(billed, loadedBilled.get());
		assertEquals(billed.getAmount(), loadedBilled.get().getAmount(), 0.01);
		assertEquals(billed.getPrice(), loadedBilled.get().getPrice());
		
		coreModelService.remove(billed);
	}
	
	@Test
	public void changeNonIntegerAmount(){
		IBilled billed = coreModelService.create(IBilled.class);
		// use components to set price, setPrice method is interpreted as manual price change
		billed.setPoints(2);
		billed.setFactor(1);
		billed.setPrimaryScale(100);
		billed.setSecondaryScale(100);
		billed.setAmount(2.5);
		assertTrue(coreModelService.save(billed));
		
		assertTrue(billed.isNonIntegerAmount());
		assertFalse(billed.isChangedPrice());
		assertEquals(0.02, billed.getPrice().doubleValue(), 0.001);
		assertEquals(2.5, billed.getAmount(), 0.001);
		assertEquals(0.05, billed.getTotal().doubleValue(), 0.001);
		// can not set price, as secondary scale is already used by non integer amount
		Exception exception = null;
		try {
			billed.setPrice(new Money(5));
		} catch (Exception e) {
			exception = e;
		}
		assertTrue(exception instanceof IllegalStateException);
		coreModelService.remove(billed);
	}
	
	@Test
	public void changePrice(){
		IBilled billed = coreModelService.create(IBilled.class);
		// use components to set price, setPrice method is interpreted as manual price change
		billed.setPoints(2);
		billed.setFactor(1);
		billed.setPrimaryScale(100);
		billed.setSecondaryScale(100);
		billed.setAmount(1);
		billed.setPrice(new Money(2.5));
		assertTrue(coreModelService.save(billed));
		
		assertFalse(billed.isNonIntegerAmount());
		assertTrue(billed.isChangedPrice());
		assertEquals(2.5, billed.getPrice().doubleValue(), 0.001);
		assertEquals(1.0, billed.getAmount(), 0.001);
		assertEquals(2.5, billed.getTotal().doubleValue(), 0.001);
		
		// can not set non integer amount, as secondary scale is already used by changed price
		Exception exception = null;
		try {
			billed.setAmount(2.5);
		} catch (Exception e) {
			exception = e;
		}
		assertTrue(exception instanceof IllegalStateException);
		// integer amount change is still possible
		billed.setAmount(3.0);
		assertTrue(coreModelService.save(billed));
		
		assertFalse(billed.isNonIntegerAmount());
		assertTrue(billed.isChangedPrice());
		assertEquals(2.5, billed.getPrice().doubleValue(), 0.001);
		assertEquals(3.0, billed.getAmount(), 0.001);
		assertEquals(7.5, billed.getTotal().doubleValue(), 0.001);
		
		coreModelService.remove(billed);
	}
	
	@Test
	public void testShowTwoDifferentTotalImplementations(){
		IBilled billed = coreModelService.create(IBilled.class);
		billed.setAmount(2);
		billed.setFactor(1);
		billed.setPrimaryScale(3);
		billed.setPrice(new Money(105));
		assertTrue(coreModelService.save(billed));
		
		// test1: 1.05 x 0.03 x 1 x 2 = 0.063
		Money moneyNewTotal = billed.getTotal();
		Money moneyInvalidTotal = calculateTotalInvalidWay(billed);
		assertEquals(6, moneyNewTotal.getCents());
		assertEquals(6, moneyInvalidTotal.getCents()); //OK
		
		// test2: 1.10 x 0.03 x 1 x 2 = 0.066 
		billed.setPrice(new Money(110));
		 moneyNewTotal = billed.getTotal();
		 moneyInvalidTotal = calculateTotalInvalidWay(billed);
		assertEquals(7, moneyNewTotal.getCents());
		assertEquals(6, moneyInvalidTotal.getCents()); //invalid
			
		// test3: 1.20 x 0.03 x 1 x 2 = 0.072
		billed.setPrice(new Money(120));
		moneyNewTotal = billed.getTotal();
		moneyInvalidTotal = calculateTotalInvalidWay(billed);
		assertEquals(7, moneyNewTotal.getCents());
		assertEquals(8, moneyInvalidTotal.getCents()); //invalid
	}

	/**
	 * If money multiply is used cascaded rounding issues can happened
	 * @param billed
	 * @return
	 */
	private Money calculateTotalInvalidWay(IBilled billed){
		return billed.getPrice().multiply(billed.getPrimaryScaleFactor())
			.multiply(billed.getSecondaryScaleFactor()).multiply(billed.getAmount());
	}
}
