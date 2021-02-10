package ch.elexis.core.mail.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.elexis.core.mail.MailAccountTest;
import ch.elexis.core.mail.MailTextTemplateTest;
import ch.elexis.core.mail.internal.MailClientImapTest;
import ch.elexis.core.mail.internal.MailClientTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	MailAccountTest.class, MailClientTest.class, MailTextTemplateTest.class,
	MailClientImapTest.class

})
public class AllTests {
	
}
