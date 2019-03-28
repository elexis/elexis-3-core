package ch.elexis.core.model;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class MessageTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		super.createUser();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void createAndRemoveMessage_userToSelf(){
		
		IMessage message = coreModelService.create(IMessage.class);
		message.setSender(user);
		message.addReceiver(user);
		message.setCreateDateTime(LocalDateTime.now());
		message.setMessageText(
			"That makes me angry, and when Dr. Evil gets angry Mr. Bigglesworth gets upset. And when Mr. Bigglesworth gets upset, people DIE!");
		coreModelService.save(message);
		
		coreModelService.remove(message);
	}
	
}
