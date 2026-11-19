package ch.elexis.core.mail.ui.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.mail.ui.dialogs.SendMailDialogTest;
import ch.elexis.core.mail.ui.test.preference.MailAccountFormTest;

@RunWith(Suite.class)
@SuiteClasses({ SendMailDialogTest.class, MailAccountFormTest.class })
// HistoryLoaderTests.class run local non parallel, running on build server in parallel fails
public class AllMailTests {

}


