package ch.elexis.core.model.builder;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.test.AbstractTest;
import ch.rgw.tools.Money;

public class IBilledBuilderTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createEncounter();
		super.createLocalArticle();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void build() {
		IBilled billed = new IBilledBuilder(coreModelService, localArticle, encounter, mandator).buildAndSave();

		assertEquals(encounter, billed.getEncounter());
		assertEquals("test article", billed.getText());
		assertEquals("123456789", billed.getCode());
		assertEquals("class ch.elexis.core.model.TypedArticle", billed.getBillable().getClass().toString());
		assertEquals(1d, billed.getAmount(), 0.01);
		assertEquals(new Money(0), billed.getTotal()); // done via IBillingService
		assertEquals(new Money(0), billed.getPrice()); // done via IBillingService
		assertEquals(1d, billed.getFactor(), 0.01);
		assertEquals("test article", billed.getLabel());
		assertEquals(new Money(0), billed.getNetPrice()); // done via IBillingService
		assertEquals(0, billed.getPoints());
		assertEquals(100, billed.getPrimaryScale());
		assertEquals(1d, billed.getPrimaryScaleFactor(), 0.01);
		assertEquals(new Money(0), billed.getScaledPrice());
		assertEquals(100, billed.getSecondaryScale());
		assertEquals(1d, billed.getSecondaryScaleFactor(), 0.01);
		assertEquals(mandator.getId(), billed.getBiller().getId());

		coreModelService.remove(billed);
	}

}
