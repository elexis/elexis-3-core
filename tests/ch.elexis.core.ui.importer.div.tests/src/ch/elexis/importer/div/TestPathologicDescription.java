package ch.elexis.importer.div;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{}
	
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
	
	private HL7Parser hlp = new TestHL7Parser("HL7_Test");
	
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
				assertEquals(Description.PATHO_REF_ITEM, description.getDescription());
				assertEquals("4.0-9.4", description.getReference());
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
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Keimzahl")) {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				PathologicDescription description = qrr.get(j).getPathologicDescription();
				assertNotNull(description);
				assertEquals(Description.PATHO_IMPORT, description.getDescription());
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
				assertEquals(Description.PATHO_REF, description.getDescription());
				assertEquals("4.0-9.4", description.getReference());
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
		qrli = new Query<LabItem>(LabItem.class);
		qLi = qrli.execute();
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
