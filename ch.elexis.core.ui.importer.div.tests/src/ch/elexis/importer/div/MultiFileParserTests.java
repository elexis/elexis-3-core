package ch.elexis.importer.div;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.ui.importer.div.importers.multifile.MultiFileParser;
import ch.elexis.core.ui.importer.div.importers.multifile.strategy.DefaultImportStrategyFactory;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;

public class MultiFileParserTests {
	private static MultiFileParser mfParser;
	private static final String MY_TESTLAB = "myTestLab";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		mfParser = new MultiFileParser(MY_TESTLAB);
		mfParser.setTestMode(true);
	}
	
	@After
	public void tearDown() throws Exception{
		removeAllPatientsAndDependants();
	}
	
	@Test
	public void testImportFromFile(){
		File hl7File = new File(PlatformHelper.getBasePath("ch.elexis.core.ui.importer.div.tests"),
			"rsc/Synlab/Labor-Befund.HL7");
		Result<Object> result =
			mfParser.importFromFile(hl7File, new DefaultImportStrategyFactory());
		if (result.isOK()) {
			assertTrue(true); // show import was successful
			assertEquals(2, result.getMessages().size());
			removeAllPatientsAndDependants();
		} else {
			String msg = "Import of 'Labor-Befund.HL7' failed";
			fail(msg);
		}
	}
	
	@Test
	public void testImportFromDirectory(){
		File synlabDir = new File(
			PlatformHelper.getBasePath("ch.elexis.core.ui.importer.div.tests"), "rsc/Synlab/");
			
		Result<Object> result =
			mfParser.importFromDirectory(synlabDir, new DefaultImportStrategyFactory());
		if (result.isOK()) {
			assertTrue(true); // show import was successful
			assertEquals(4, result.getMessages().size());
			removeAllPatientsAndDependants();
		} else {
			String msg = "Import of 'Laborbefund-Musterfrau.HL7' failed";
			fail(msg);
		}
	}
	
	static private void removeAllPatientsAndDependants(){
		Query<Patient> qr = new Query<Patient>(Patient.class);
		List<Patient> qrr = qr.execute();
		for (int j = 0; j < qrr.size(); j++) {
			qrr.get(j).delete(true);
		}
		
		qr = new Query<Patient>(Patient.class);
		qrr = qr.execute();
	}
}
