package ch.elexis.core.mail.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.elexis.core.mail.MailAccountTest;
import ch.elexis.core.mail.internal.MailClientTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	MailAccountTest.class, MailClientTest.class
})
public class AllTests {
	
}
