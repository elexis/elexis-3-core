package ch.elexis.importer.div;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ch.elexis.importer.div.Helpers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.core.ui.importer.div.importers.TestHL7Parser;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;

public class TestPathologicDescription {
	
	private static Path workDir = null;
	
	private static HL7Parser hlp;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		hlp = new TestHL7Parser("HL7_Test");
	}
	
	@Before
	public void setup() throws Exception{
		workDir = Helpers.copyRscToTempDirectory();
	}
	
	@After
	public void teardown() throws Exception{
		removeAllPatientsAndDependants();
		if (workDir != null) {
			Helpers.removeTempDirectory(workDir);
		}
	}
	
	@Test
	public void testAnalyticaHL7UseLocalRef() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		// set the use local config to true
		CoreHub.userCfg.set(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, true);
		
		parseOneHL7file(new File(workDir.toString(), "Analytica/01TEST5005.hl7"), false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.orderBy(false, LabResult.ITEM_ID, LabResult.DATE, LabResult.RESULT);
		List<LabResult> qrr = qr.execute();
		int j = 0;
		Query<LabItem> query = new Query<LabItem>(LabItem.class);
		query.orderBy(false, LabItem.SHORTNAME);
		List<LabItem> items = query.execute();
		LabItem[] itemArray = new LabItem[items.size()];
		j = 0;
		if (items != null) {
			for (LabItem item : items) {
				itemArray[j] = item;
				j++;
			}
		}
		assertEquals(7, qrr.size());
		boolean foundpatho1 = false;
		boolean foundpatho2 = false;
		boolean foundpatho3 = false;
		for (j = 0; j < qrr.size(); j++) {
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Kalium")) {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				assertEquals(Description.PATHO_REF_ITEM, description.getDescription());
				assertEquals("3.5-5.1", description.getReference());
				foundpatho1 = true;
			}
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Leukozyten")) {
				assertTrue(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				assertEquals(Description.PATHO_IMPORT, description.getDescription());
				assertEquals("H", description.getReference());
				foundpatho2 = true;
			}
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Progesteron")) {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				assertEquals(Description.PATHO_NOREF, description.getDescription());
				assertEquals("", description.getReference());
				foundpatho3 = true;
			}
		}
		assertTrue(foundpatho1);
		assertTrue(foundpatho2);
		assertTrue(foundpatho3);
	}
	
	@Test
	public void testAnalyticaHL7NotNumeric() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		// set the use local config to true
		CoreHub.userCfg.set(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, true);
		
		parseOneHL7file(new File(workDir.toString(), "Analytica/0216370074_6417526401671.hl7"),
			false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.orderBy(false, LabResult.ITEM_ID, LabResult.DATE, LabResult.RESULT);
		List<LabResult> qrr = qr.execute();
		int j = 0;
		Query<LabItem> query = new Query<LabItem>(LabItem.class);
		query.orderBy(false, LabItem.SHORTNAME);
		List<LabItem> items = query.execute();
		LabItem[] itemArray = new LabItem[items.size()];
		j = 0;
		if (items != null) {
			for (LabItem item : items) {
				itemArray[j] = item;
				j++;
			}
		}
		assertEquals(3, qrr.size());
		boolean foundpatho1 = false;
		for (j = 0; j < qrr.size(); j++) {
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("URIN-VACUTAINER - Keimzahl")) {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				assertEquals(Description.PATHO_IMPORT_NO_INFO, description.getDescription());
				assertEquals("", description.getReference());
				foundpatho1 = true;
			}
		}
		assertTrue(foundpatho1);
	}
	
	@Test
	public void testAnalyticaHL7UseRef() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		// set the use local config to false
		CoreHub.userCfg.set(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, false);
		
		parseOneHL7file(new File(workDir.toString(), "Analytica/01TEST5005.hl7"), false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.orderBy(false, LabResult.ITEM_ID, LabResult.DATE, LabResult.RESULT);
		List<LabResult> qrr = qr.execute();
		int j = 0;
		Query<LabItem> query = new Query<LabItem>(LabItem.class);
		query.orderBy(false, LabItem.SHORTNAME);
		List<LabItem> items = query.execute();
		LabItem[] itemArray = new LabItem[items.size()];
		j = 0;
		if (items != null) {
			for (LabItem item : items) {
				itemArray[j] = item;
				j++;
			}
		}
		assertEquals(7, qrr.size());
		boolean foundpatho1 = false;
		boolean foundpatho2 = false;
		boolean foundpatho3 = false;
		for (j = 0; j < qrr.size(); j++) {
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Kalium")) {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				// default use local ref is true
				assertEquals(Description.PATHO_REF, description.getDescription());
				assertEquals("3.5-5.1", description.getReference());
				foundpatho1 = true;
			}
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Leukozyten")) {
				assertTrue(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				// default use local ref is true
				assertEquals(Description.PATHO_IMPORT, description.getDescription());
				assertEquals("H", description.getReference());
				foundpatho2 = true;
			}
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Progesteron")) {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				assertEquals(Description.PATHO_NOREF, description.getDescription());
				assertEquals("", description.getReference());
				foundpatho3 = true;
			}
		}
		assertTrue(foundpatho1);
		assertTrue(foundpatho2);
		assertTrue(foundpatho3);
	}
	
	@Test
	public void testAnalyticaStringResults_10786() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		
		parseOneHL7file(new File(workDir.toString(), "Analytica/0116294364_6412642631625.hl7"),
			false, true);
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(8, qrr.size());
		for (LabResult labResult : qrr) {
			assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
			PathologicDescription pathologicDescription = labResult.getPathologicDescription();
			if (labResult.getItem().getLabel().contains("Borrelien (IgM)")) {
				// OBX|500505|FT|BORRM^Borrelien (IgM)^^^BORRELIEN IGM||positiv||  negativ||||C||||||
				assertEquals(Description.PATHO_IMPORT_NO_INFO,
					pathologicDescription.getDescription());
				assertEquals("", pathologicDescription.getReference());
				assertEquals("positiv", labResult.getResult());
				// it is pathologic, but we don't know - we can't interpret
				assertEquals(0, labResult.getFlags());
				assertTrue(labResult.isPathologicFlagIndetermined(pathologicDescription));
				assertEquals("", labResult.getItem().getUnit());
			} else if (labResult.getItem().getLabel().equalsIgnoreCase("TestStupidValues")) {
				assertEquals(Description.PATHO_IMPORT_NO_INFO,
					pathologicDescription.getDescription());
				assertEquals("hund", pathologicDescription.getReference());
				assertEquals("katze", labResult.getResult());
				assertEquals(0, labResult.getFlags());
			} else if (labResult.getItem().getLabel().equalsIgnoreCase("TestImportHighFlag")) {
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("bar", pathologicDescription.getReference());
				assertEquals("foo", labResult.getResult());
				assertEquals(1, labResult.getFlags());
			}
			
		}
		
	}
	
	@Test
	public void testOpenMedicalMissingImportPathFlag_10962() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		
		parseOneHL7file(new File(workDir.toString(),
			"OpenMedical/176471863520180219143653675__20180217123041_L18070354_20180217_TESTPERSON_19500101_1234_18635.hl7"),
			false, true);
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(120, qrr.size());
		for (LabResult labResult : qrr) {
			PathologicDescription pathologicDescription = labResult.getPathologicDescription();
			String itemCode = labResult.getItem().getKuerzel();
			switch (itemCode) {
			case "VCAG":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("A", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
				break;
			case "EBNAG":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("A", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
				break;
			case "BBIG":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("HH", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				break;
			case "PHEN":
				assertEquals(Description.PATHO_REF_ITEM, pathologicDescription.getDescription());
				assertEquals("12.0 - 80.0", pathologicDescription.getReference());
				assertEquals(0, labResult.getFlags());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				break;
			case "HSVM":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("A", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
				break;
			case "ZIKG":
				assertEquals(Description.PATHO_IMPORT_NO_INFO,
					pathologicDescription.getDescription());
				assertEquals("", pathologicDescription.getReference());
				assertEquals(0, labResult.getFlags());
				assertTrue(labResult.isPathologicFlagIndetermined(null));
				break;
			default:
				break;
			}
		}
	}
	
	@Test
	public void testLabCubeNumberMissingImportPathFlag_11057() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		
		parseOneHL7file(new File(workDir.toString(),
			"LabCube/5083_LabCube_DriChem7000_20180314131140_288107.hl7"), false, true);
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(8, qrr.size());
		for (LabResult labResult : qrr) {
			PathologicDescription pathologicDescription = labResult.getPathologicDescription();
			String itemCode = labResult.getItem().getKuerzel();
			switch (itemCode) {
			case "HDLC-P":
				assertEquals(Description.PATHO_REF, pathologicDescription.getDescription());
				assertEquals("0.93-1.78", pathologicDescription.getReference());
				assertEquals(0, labResult.getFlags());
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals("1.69", labResult.getResult());
				break;
			case "TCHO-P":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("HH", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals("6.19", labResult.getResult());
				assertEquals("3.88-5.66", labResult.getItem().getReferenceFemale());
				break;
			case "GPT-P":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("LL", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals("<10", labResult.getResult());
				assertEquals("4-44", labResult.getItem().getReferenceFemale());
				break;
			case "CRE-P":
				assertEquals(Description.PATHO_REF, pathologicDescription.getDescription());
				assertEquals(0, labResult.getFlags());
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				assertEquals("47", labResult.getResult());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				break;
			default:
				break;
			}
		}
	}
	
	private void parseOneHL7file(File f, boolean deleteAll, boolean alsoFailing) throws IOException{
		String name = f.getAbsolutePath();
		if (f.canRead() && (name.toLowerCase().endsWith(".hl7"))) {
			if (f.getName().equalsIgnoreCase("01TEST5005.hl7")
				|| f.getName().equalsIgnoreCase("1_Kunde_20090612083757162_10009977_.HL7")) {
				if (!alsoFailing) {
					// System.out.println("Skipping " + name);
					return;
				}
			}
			// System.out.println("parseOneHL7file " + name + "  " + f.length() + " bytes ");
			Result<?> rs = hlp.importFile(f, f.getParentFile(), true);
			if (!rs.isOK()) {
				String info = "Datei " + name + " fehlgeschlagen";
				System.out.println(info);
				fail(info);
			} else {
				assertTrue(true); // To show that we were successfull
				// System.out.println("Datei " + name +
				// " erfolgreich verarbeitet");
				Query<LabResult> qr = new Query<LabResult>(LabResult.class);
				List<LabResult> qrr = qr.execute();
				if (qrr.size() <= 0)
					fail("NoLabResult");
				if (deleteAll) {
					removeAllPatientsAndDependants();
				}
			}
		} else {
			System.out.println("Skipping Datei " + name);
		}
	}
	
	static private void removeAllLaboWerte(){
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		for (int j = 0; j < qrr.size(); j++) {
			qrr.get(j).delete();
		}
		Query<LabItem> qrli = new Query<LabItem>(LabItem.class);
		List<LabItem> qLi = qrli.execute();
		for (int j = 0; j < qLi.size(); j++) {
			qLi.get(j).delete();
		}
		PersistentObject.clearCache();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		qr = new Query<LabResult>(LabResult.class);
		qrr = qr.execute();
		assertEquals(0, qrr.size());
		qrli = new Query<LabItem>(LabItem.class);
		qLi = qrli.execute();
		assertEquals(0, qLi.size());
	}
	

}
