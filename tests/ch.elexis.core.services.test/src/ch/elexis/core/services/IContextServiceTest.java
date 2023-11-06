package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import ch.elexis.core.utils.OsgiServiceUtil;

public class IContextServiceTest extends AbstractServiceTest {

	private IContextService contextService = OsgiServiceUtil.getService(IContextService.class).get();

	@After
	public void after() {
		cleanup();
	}

	@Test
	public void submitContextInheriting() {
		// Runs against testContextService where implementation is identical
		// to Elexis RCP - this is only a general smoke test as more advanced
		// scenarios can only be tested against the ES IContextService
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();

		List<String> submitContextInheriting = contextService.submitContextInheriting(
				() -> testEncounters.parallelStream().map(ec -> ec.getId()).collect(Collectors.toList()));
		assertEquals(3, submitContextInheriting.size());
	}

}
