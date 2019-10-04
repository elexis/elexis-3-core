package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IMessageServiceTest extends AbstractServiceTest {
	
	private IMessageService service = OsgiServiceUtil.getService(IMessageService.class).get();
	
	@Test
	public void internalDatabaseMessage_explicitScheme_fromUser(){
		IUser user = coreModelService.load(TestEntities.USER_USER_ID, IUser.class).orElse(null);
		TransientMessage message = service.prepare(user.getId(), "internaldb:" + user.getId());
		message.setMessageText("internalDatabaseMessage_explicitScheme_fromUser");
		message.addMessageCode(MessageCode.Key.SenderSubId, "tests.messageServiceTest");
		ObjectStatus status = service.send(message);
		assertTrue(status.getMessage(), status.isOK());
		assertEquals(status.getMessage(), "internaldb", status.getObject());
	}
	
	@Test
	public void internalDatabaseMessage_internalScheme_fromUser(){
		IUser user = coreModelService.load(TestEntities.USER_USER_ID, IUser.class).orElse(null);
		TransientMessage message = service.prepare(user.getId(),
			IMessageService.INTERNAL_MESSAGE_URI_SCHEME + ":" + user.getId());
		message.setMessageText("internalDatabaseMessage_internalScheme_fromUser");
		message.addMessageCode(MessageCode.Key.SenderSubId, "tests.messageServiceTest");
		ObjectStatus status = service.send(message);
		assertTrue(status.getMessage(), status.isOK());
	}
	
}
