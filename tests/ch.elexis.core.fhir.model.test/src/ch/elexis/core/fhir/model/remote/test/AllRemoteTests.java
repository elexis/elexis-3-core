package ch.elexis.core.fhir.model.remote.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.utils.CoreUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SystemStartupTest.class, FhirPersonTest.class })
public class AllRemoteTests {

	//@formatter:off
	// perform remote tests against a specific EE

	// The system property variables of FhirRemoteModelTests has to be set correctly
	// so we may login as a specific user and perform the requests

	
	// MDE Workspace preparation
	// /Users/mdescher/git/elexis-environment-console -> ./ee start
	// /Users/mdescher/git/myelexis-server -> ./mvnw quarkus:de -Dquarkus.profile="eedev"

	// Opens specific perspective see ch.elexis.core.application.perspectives.RemoteOnlyPerspective
	//@formatter:on

	@BeforeClass
	public static void beforeAll() {
		assertFalse(CoreUtil.isTestMode()); // Would initialize DataSource!
		assertTrue(ElexisSystemPropertyConstants.IS_EE_DEPENDENT_OPERATION_MODE);
		// should this mode induce that skip liquibase = true ?
		System.setProperty(ElexisSystemPropertyConstants.CONN_SKIP_LIQUIBASE, Boolean.TRUE.toString());

		// fetch the token manually, as done in
		// EEDependentLoginDialog#performDirectOAuthLogin

	}

}
