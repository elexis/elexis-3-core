package ch.elexis.core.ui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.ui.tests.views.invoicelist.Test_InvoiceBillState;

@RunWith(Suite.class)
@SuiteClasses({
	HistoryLoaderTests.class, Test_InvoiceBillState.class
})
public class AllTests {
	
}
