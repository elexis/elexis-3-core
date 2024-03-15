package ch.elexis.core.mail.ui.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.mail.ui.dialogs.SendMailDialogTest;

@RunWith(Suite.class)
@SuiteClasses({ SendMailDialogTest.class })
// HistoryLoaderTests.class run local non parallel, running on build server in parallel fails
public class AllMailTests {

}


