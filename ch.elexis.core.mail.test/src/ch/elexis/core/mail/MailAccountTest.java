package ch.elexis.core.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import ch.elexis.core.mail.MailAccount.TYPE;

public class MailAccountTest {
	
	@Test
	public void from(){
		MailAccount account =
			MailAccount.from("id=testId,type=SMTP,username=testUsername,password=dGVzdFBhc3N3b3Jk");
		assertNotNull(account);
		assertEquals("testId", account.getId());
		assertEquals(TYPE.SMTP, account.getType());
		assertEquals("testUsername", account.getUsername());
		assertEquals("testPassword", account.getPassword());
	}
	
	@Test
	public void string(){
		MailAccount account = MailAccount.from("id=testId,type=SMTP,username=testUsername");
		assertNotNull(account);
		String string = account.toString();
		assertEquals("id=testId,type=SMTP,username=testUsername,starttls=false", string);
		
		account.setPassword("testPassword");
		string = account.toString();
		assertEquals(
			"id=testId,type=SMTP,username=testUsername,password=dGVzdFBhc3N3b3Jk,starttls=false",
			string);
		
	}
}
