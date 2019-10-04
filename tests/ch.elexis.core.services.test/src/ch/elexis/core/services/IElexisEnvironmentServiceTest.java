package ch.elexis.core.services;

import static org.junit.Assert.assertTrue;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IElexisEnvironmentServiceTest extends AbstractServiceTest {
	
	private Optional<IElexisEnvironmentService> ee_service =
		OsgiServiceUtil.getService(IElexisEnvironmentService.class);
	private IMessageService messageService =
		OsgiServiceUtil.getService(IMessageService.class).get();
	private static IContextService contextService =
		OsgiServiceUtil.getService(IContextService.class).get();
	
	@BeforeClass
	public static void beforeClass() throws NoSuchAlgorithmException, KeyManagementException{
		// set it per station
		contextService.getRootContext().setNamed("rocketchat-station-integration-token",
			"b8fnKyMcMTRyeg22d/hM3LTStZheEt7w3L6fu8rDDNqcJiXWbbvmKsRrP2zm8zTYoA");
		
		acceptAllCerts();
	}
	
	@Test
	public void rocketChatMessage_fromStation(){
		Assume.assumeTrue(ee_service.isPresent());
		
		TransientMessage message = messageService.prepare(
			getClass().getName() + "@" + contextService.getStationIdentifier(),
			"rocketchat:demouser");
		message.setMessageText("rocketChatMessage_fromStation");
		message.addMessageCode(MessageCode.Key.SenderSubId, "tests.elexisenvironmentservicetest");
		message.addMessageCode("key", "value");
		IStatus status = messageService.send(message);
		assertTrue(status.getMessage(), status.isOK());
	}
	
}
