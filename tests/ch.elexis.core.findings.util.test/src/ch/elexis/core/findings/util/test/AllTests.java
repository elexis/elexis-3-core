package ch.elexis.core.findings.util.test;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.util.FindingsFormatUtilTest;
import ch.elexis.core.findings.util.fhir.transformer.AllTransformerTests;

@RunWith(Suite.class)
@SuiteClasses({ FindingsFormatUtilTest.class, AllTransformerTests.class })
public class AllTests {

	private static FhirContext context3 = FhirContext.forDstu3();

	private static FhirContext context4 = FhirContext.forR4();

	public static IParser getJsonParser3() {
		return context3.newJsonParser();
	}

	public static IParser getJsonParser4() {
		return context4.newJsonParser();
	}

	public static String getResourceAsString(String resourcePath) throws IOException {
		return IOUtils.toString(AllTests.class.getResourceAsStream(resourcePath), Charset.forName("UTF-8"));
	}
}
