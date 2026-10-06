package ch.elexis.core.text.docx.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ch.elexis.core.text.docx.BulletConverterTest;
import ch.elexis.core.text.docx.DocxTextPluginTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ DocxTextPluginTest.class, BulletConverterTest.class })
public class AllTests {

}
