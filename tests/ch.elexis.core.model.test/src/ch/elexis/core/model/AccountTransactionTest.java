package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.test.AbstractTest;
import ch.rgw.tools.Money;

public class AccountTransactionTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		createPatient();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void createAndCalculateBalance(){
		IAccountTransaction accountTransaction = coreModelService.create(IAccountTransaction.class);
		accountTransaction.setPatient(patient);
		accountTransaction.setAmount(new Money(-300));
		accountTransaction.setDate(LocalDate.now().minusDays(5));
		coreModelService.save(accountTransaction);
		
		IAccountTransaction accountTransaction2 =
			coreModelService.create(IAccountTransaction.class);
		accountTransaction2.setPatient(patient);
		accountTransaction2.setAmount(new Money(200));
		accountTransaction2.setDate(LocalDate.now());
		coreModelService.save(accountTransaction2);
		
		INamedQuery<Long> namedQuery = coreModelService.getNamedQuery(Long.class,
			IAccountTransaction.class, true, "balance.patient");
		assertNotNull(namedQuery);
		List<Long> executeWithParameters =
			namedQuery.executeWithParameters(namedQuery.getParameterMap("patient", patient));
		Long expectedResult = -100l;
		assertEquals(expectedResult, executeWithParameters.get(0));
		
		coreModelService.remove(accountTransaction);
		coreModelService.remove(accountTransaction2);
	}
	
}
