package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class IBillingServiceTest extends AbstractServiceTest {
	
	private IBillingService billingService =
		OsgiServiceUtil.getService(IBillingService.class).get();
	
	private static ICustomService customBillable;
	private static IEncounter encounter;
	
	@BeforeClass
	public static void beforeClass(){
		customBillable = CoreModelServiceHolder.get().create(ICustomService.class);
		customBillable.setText("test service");
		customBillable.setCode("1234");
		customBillable.setNetPrice(new Money(512));
		customBillable.setPrice(new Money(1024));
		coreModelService.save(customBillable);
		
		encounter = new IEncounterBuilder(CoreModelServiceHolder.get(),
			AllServiceTests.getCoverage(), AllServiceTests.getMandator()).buildAndSave();
	}
	
	@AfterClass
	public static void afterClass(){
		CoreModelServiceHolder.get().remove(encounter);
	}
	
	@Test
	public void bill() throws InterruptedException{
		Result<IBilled> billed = billingService.bill(customBillable, encounter, 1.0);
		assertTrue(billed.isOK());
		List<IBilled> billedList = encounter.getBilled();
		
		assertEquals(1, billedList.size());
		assertEquals(1, billedList.get(0).getAmount(), 0d);
		assertEquals(1024, billedList.get(0).getPrice().getCents());
		
		// Import - wird verrechnet
		// Leistung wird deleted = 1
		// neuerliche verrechnung aber keine anzeige
		EntityManager em = (EntityManager) OsgiServiceUtil.getService(IElexisEntityManager.class)
			.get().getEntityManager(true);
		em.getTransaction().begin();
		int executeUpdate =
			em.createNativeQuery("UPDATE LEISTUNGEN SET DELETED='1' WHERE DELETED='0'")
				.executeUpdate();
		assertEquals(1, executeUpdate);
		em.getTransaction().commit();
		
		billed = billingService.bill(customBillable, encounter, 1.0);
		
		billedList = encounter.getBilled();
		assertFalse(billedList.get(0).isDeleted());
		assertEquals(1, billedList.size());
		assertEquals(1, billedList.get(0).getAmount(), 0d);
		assertEquals(1024, billedList.get(0).getPrice().getCents());
	}
	
}
