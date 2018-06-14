package ch.elexis.importer.div;

import static ch.elexis.importer.div.Helpers.removeAllPatientsAndDependants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.importer.div.importers.multifile.MultiFileParser;
import ch.elexis.core.ui.importer.div.importers.PersistenceHandler;
import ch.elexis.core.ui.importer.div.importers.TestHL7Parser;
import ch.elexis.core.ui.importer.div.importers.multifile.strategy.DefaultImportStrategyFactory;
import ch.rgw.tools.Result;

public class MultiFileParserTests {
	private static MultiFileParser mfParser;
	private static final String MY_TESTLAB = "myTestLab";
	private static Path workDir = null;
	private static HL7Parser hl7Parser = new TestHL7Parser(MY_TESTLAB);
	private static IPersistenceHandler persistenceHandler = new PersistenceHandler();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		mfParser = new MultiFileParser(MY_TESTLAB);
		mfParser.setTestMode(true);
	}
	
	@Before
	public void setup() throws Exception{
		workDir = Helpers.copyRscToTempDirectory();
	}
	
	@After
	public void tearDown() throws Exception{
		removeAllPatientsAndDependants();
		if (workDir != null) {
			Helpers.removeTempDirectory(workDir);
		}
	}
	
	@Test
	public void testImportFromFile(){
		// requires omnivore
		File hl7File = new File(workDir.toString(), "Synlab/Labor-Befund.HL7");
		Result<Object> result = mfParser.importFromFile(hl7File, new DefaultImportStrategyFactory(),
			hl7Parser, persistenceHandler);
		if (result.isOK()) {
			assertTrue(true); // show import was successful
			assertEquals(2, result.getMessages().size());
			removeAllPatientsAndDependants();
		} else {
			String msg = "Import of 'Labor-Befund.HL7' failed";
			if (msg.contains("Omnivore") && System.getProperty("doNotFailOnMissingOmnivore") == null) {
				fail(msg + " " + result.toString());
			}
		}
	}
	
	@Test
	public void testImportFromDirectory(){
		File synlabDir = new File(workDir.toString(), "Synlab");
		
		Result<Object> result = mfParser.importFromDirectory(synlabDir,
			new DefaultImportStrategyFactory(), hl7Parser, persistenceHandler);
		if (result.isOK()) {
			assertTrue(true); // show import was successful
			assertEquals(4, result.getMessages().size());
			removeAllPatientsAndDependants();
		} else {
			String msg = "Import of 'Laborbefund-Musterfrau.HL7' failed";
			if (msg.contains("Omnivore") && System.getProperty("doNotFailOnMissingOmnivore") == null) {
				fail(msg + " " + result.toString());
			}
		}
	}
	
	@Test
	public void testMoveAfterImport(){
		File moveAfterImportDir = new File(workDir.toString(), "MoveAfterImport");
		
		Result<Object> result = mfParser.importFromDirectory(moveAfterImportDir,
			new DefaultImportStrategyFactory().setMoveAfterImport(true), hl7Parser,
			persistenceHandler);
		if (result.isOK()) {
			assertTrue(true); // show import was successful
			assertEquals(4, result.getMessages().size());
			
			assertTrue(new File(moveAfterImportDir, "archive").exists());
			assertFalse(new File(moveAfterImportDir, "error").exists());
			// 1 because directory is returned by listFiles
			assertTrue(moveAfterImportDir.listFiles().length == 1);
			
			removeAllPatientsAndDependants();
		} else {
			String msg = "Import of 'Laborbefund-Musterfrau.HL7' failed";
			if (msg.contains("Omnivore") && System.getProperty("doNotFailOnMissingOmnivore") == null) {
				fail(msg + " " + result.toString());
			}
		}
	}
}
