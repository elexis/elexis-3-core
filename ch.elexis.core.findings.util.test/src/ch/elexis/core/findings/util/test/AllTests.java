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

@RunWith(Suite.class)
@SuiteClasses({ FindingsFormatUtilTest.class })
public class AllTests {

	private static FhirContext context = FhirContext.forDstu3();

	public static IParser getJsonParser() {
		return context.newJsonParser();
	}

	public static String getResourceAsString(String resourcePath) throws IOException {
		return IOUtils.toString(AllTests.class.getResourceAsStream(resourcePath), Charset.forName("UTF-8"));
	}
}
