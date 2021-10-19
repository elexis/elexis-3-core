package ch.elexis.core.services.eenv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.elexis.core.model.message.TransientMessage;

public class RocketchatMessageTest {
	
	@Test
	public void prepareRocketchatMessage(){
		RocketchatMessageTransporter rmt = new RocketchatMessageTransporter();
		TransientMessage tm = new TransientMessage("sender", "receiver");
		String jsonMessage = rmt.prepareRocketchatMessage(tm);
		assertEquals(
			"{\"sender\":\"sender\",\"text\":\" @receiver\",\"attachments\":{\"color\":\"#0000FF\"}}",
			jsonMessage);
	}
	
}
