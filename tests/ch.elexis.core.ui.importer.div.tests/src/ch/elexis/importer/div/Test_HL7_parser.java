/*******************************************************************************
 * Copyright (c) 2010, Elexis und Niklaus Giger <niklaus.giger@member.fsf.org
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    N. Giger - initial implementation
 *
 * This is a generic test for importing HL7-files.
 * For each laboratory you should create a corresponding folder under rsc
 * and add (at least one) hl7 file(s).
 *
 * The testHL7files will try to parse all hl7 files, but will not check the imported LabResult.
 * This should be enough in most cases.
 *
 * However it might be a good idea to add a procedure (e.g. testAnalyticaHL7)
 * if you have unusual requirements or stumbled over a bug in elexis HL7 parser.
 *
 * Side-effects: Removes all patients & LabResults before & after running each test!
 *
 *******************************************************************************/
package ch.elexis.importer.div;

import static ch.elexis.importer.div.Helpers.parseOneHL7file;
import static ch.elexis.importer.div.Helpers.removeAllLaboWerte;
import static ch.elexis.importer.div.Helpers.removeAllPatientsAndDependants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.Messages;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.core.ui.importer.div.importers.TestHL7Parser;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

public class Test_HL7_parser {
	
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
	
	@SuppressWarnings("unused")
	private void dumpLabresult(LabResult res){
		System.out.println("LabResult: pathological ? " + res.isFlag(LabResultConstants.PATHOLOGIC)
			+ " name: " + res.getItem().getName() + " label: " + res.getLabel() + " result: "
			+ res.getResult());
	}
	
	private void parseAllHL7files(File directory) throws IOException{
		File[] files = directory.listFiles();
		int nrFiles = 0;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				parseAllHL7files(file);
			} else {
				System.out.println("TESTING: " + file.getAbsolutePath());
				parseOneHL7file(hlp, file, true, false);
				nrFiles += 1;
			}
		}
		System.out.println("testHL7files: " + nrFiles + " files in " + directory.toString());
	}
	
	/**
	 * Test method for {@link ch.elexis.importers.HL7#HL7(java.lang.String, java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testHL7files() throws IOException{
		System.out.println("testHL7files in elexis-import_test/rsc: This will take some time");
		parseAllHL7files(new File(workDir.toString()));
	}
	
	@Test
	public void testOverwrite() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		File overwrite_test_1 = new File(workDir.toString(), "overwrite_test_1.hl7");
		assertEquals("Must be able to read: " + overwrite_test_1.getAbsoluteFile().toString(), true,
			overwrite_test_1.canRead());
		parseOneHL7file(hlp, new File(workDir.toString(), "overwrite_test_1.hl7"), false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.orderBy(false, LabResult.ITEM_ID, LabResult.DATE, LabResult.RESULT);
		List<LabResult> qrr = qr.execute();
		
		int foundCnt = 0;
		for (LabResult labResult : qrr) {
			String name = labResult.getItem().getName();
			System.out.println(name);
			if (name.equals("AST (GOT)")) {
				assertEquals("?", labResult.getResult());
				foundCnt++;
			}
		}
		assertEquals(1, foundCnt);
	}
	
	/**
	 * Rothen filled the HL7 field(8) with 'N' if there was no patholical value found
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRothenPatholical() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp,
			new File(workDir.toString(), "Rothen/1_Kunde_20090612083757162_10009977_.HL7"), false,
			true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.orderBy(false, LabResult.ITEM_ID, LabResult.DATE, LabResult.RESULT);
		List<LabResult> qrr = qr.execute();
		assertEquals(40, qrr.size());
		
		int j = 0;
		Query<LabItem> query = new Query<LabItem>(LabItem.class);
		query.orderBy(false, LabItem.SHORTNAME);
		LabItem item = null;
		j = 0;
		LabResult res = null;
		boolean foundLymphozyten = false;
		boolean foundPathological = false;
		for (j = 0; j < qrr.size(); j++) {
			// dumpLabresult(qrr.get(j));
			LabItem li = (LabItem) qrr.get(j).getItem();
			String name = li.getName();
			assertTrue(qrr.get(j).getAnalyseTime().getTime().toString().contains("2009"));
			assertTrue(qrr.get(j).getAnalyseTime().getTime().toString().contains("Jun 11"));
			if (name.contentEquals("Lymphozyten G/l")) {
				foundLymphozyten = true;
				res = qrr.get(j);
				item = (LabItem) qrr.get(j).getItem();
			}
			
			if (name.contentEquals("MCV") || name.contentEquals("Basophile%")
				|| name.contentEquals("Triglyceride")) {
				assertTrue(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
				assertTrue(qrr.get(j).getFlags() == LabResultConstants.PATHOLOGIC);
				foundPathological = true;
			} else {
				assertFalse(qrr.get(j).isFlag(LabResultConstants.PATHOLOGIC));
			}
			if (foundPathological && foundLymphozyten)
				break;
		}
		System.out.println(qrr.size());
		assertEquals(40, qrr.size());
		assertTrue(foundPathological);
		assertTrue(foundLymphozyten);
		assertNotNull(item);
		assertEquals("G/l", item.getEinheit());
		assertEquals("lymA_B", item.getKuerzel());
		assertEquals("Lymphozyten G/l", item.getName());
		assertEquals(LabItemTyp.TEXT, item.getTyp());
		// assertEquals(typ.NUMERIC, item.getTyp());
		assertTrue(item.getGroup().contains(Messages.HL7Parser_AutomaticAddedGroup));
		assertNotNull(res);
		assertEquals(res.getResult(), "1.6");
	}
	
	@Test
	public void testTextResult() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp,
			new File(workDir.toString(), "Arnaboldi/1_20090729162631490_6038_09_12100.HL7"), false,
			true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> results = qr.execute();
		assertFalse(results.isEmpty());
		LabResult result = results.get(0);
		assertTrue(result.isLongText());
		String resultString = result.getComment();
		assertFalse(resultString.contains("\\.br\\"));
		assertTrue(resultString.contains("\n"));
	}
	
	@Test
	public void testTextResultMultiLinebreaks() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/0216370074_6417526401671.hl7"),
			false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> results = qr.execute();
		assertFalse(results.isEmpty());
		assertEquals(3, results.size());
		LabResult result = results.get(2);
		String resultString = result.getComment();
		assertFalse(resultString, resultString.contains("\\.br\\"));
		assertTrue(resultString, resultString.contains("\n"));
	}
	
	/**
	 * Test method Analytica HL7 (Details) Some detailed checks about how a sample hl7-file is
	 * imported Actually Analytica has a special importer
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAnalyticaHL7() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/01TEST5005.hl7"), false, true);
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
		assertTrue(itemArray[0].getLabel().contains("BEM, BEM"));
		assertTrue(itemArray[3].getLabel().contains("K, Kalium"));
		assertTrue(itemArray[4].getLabel().contains("LEUK, Leukozyten"));
		assertTrue(itemArray[5].getLabel().contains("PROG, Progesteron"));
		boolean found = false;
		for (j = 0; j < qrr.size(); j++) {
			assertTrue(qrr.get(j).getLabel().contains("10.03.2004"));
			if (qrr.get(j).getItem().getName().equalsIgnoreCase("Progesteron")) {
				found = true;
				assertEquals(qrr.get(j).getResult(), "2.0");
			}
		}
		assertTrue(found);
		
		// Test fields
		LabItem aItem = itemArray[2];
		assertEquals("g/dl", aItem.getEinheit());
		assertEquals("HB", aItem.getKuerzel());
		assertTrue(aItem.getName().contains("moglobin"));
		assertEquals(LabItemTyp.NUMERIC, aItem.getTyp());
		assertTrue(aItem.getGroup().contains(Messages.HL7Parser_AutomaticAddedGroup));
		assertEquals("HL7_Test", aItem.getLabor().getKuerzel());
		assertEquals(AllTests.testLab.getId(), aItem.getLabor().getId());
		Query<Patient> pqr = new Query<Patient>(Patient.class);
		List<Patient> pqrr = pqr.execute();
		assertEquals(1, pqrr.size());
	}
	
	@Test
	public void testAnalyticaFollowUpResultMissing_9252() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp,
			new File(workDir.toString(), "Analytica/erstes Resultat0217330708_6451725824332.hl7"),
			false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.add(LabResult.RESULT, Query.LIKE, "Bisher kein Wachstum%");
		List<LabResult> qrr = qr.execute();
		assertEquals(1, qrr.size());
		LabResult labResult = qrr.get(0);
		assertEquals("20170822091024", labResult.get(LabResult.ANALYSETIME));
		
		qr = new Query<>(LabResult.class);
		qr.add(LabResult.PATIENT_ID, Query.EQUALS, labResult.getPatient().getId());
		assertEquals(6, qr.execute().size());
		
		parseOneHL7file(hlp,
			new File(workDir.toString(), "Analytica/zweites Resultat0217330708_6452745605734.hl7"),
			false, true);
		assertEquals("Trichophyton rubrum (nachweisbar)", labResult.getResult());
		assertEquals("20170901144005", labResult.get(LabResult.ANALYSETIME));
	}
	
	/**
	 * Test method Analytica HL7 (Details), check if the names of the imported {@link LabItem} are
	 * correct.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAnalyticaHL7LabItemName() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/Influenza_Labresultat.hl7"),
			false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		qr.orderBy(false, LabResult.ITEM_ID, LabResult.DATE, LabResult.RESULT);
		assertEquals(3, qr.execute().size());
		
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
		
		assertEquals("NASOPHARYNGEALABSTRICH - Influenza A Virus  (Genom-Nachweis)",
			itemArray[0].getName());
		assertEquals("NASOPHARYNGEALABSTRICH - Influenza B Virus  (Genom-Nachweis)",
			itemArray[1].getName());
	}
	
	@Test
	public void testImport_10655() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/0218040634_6467538389964.hl7"),
			false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(5, qrr.size());
		
		qr = new Query<LabResult>(LabResult.class);
		qr.add(LabResult.COMMENT, Query.LIKE, "Candida albicans%");
		
		LabResult labResult = qr.execute().get(0);
		assertEquals(0, labResult.getFlags());
		assertEquals("text", labResult.getResult());
		assertEquals(Description.PATHO_IMPORT_NO_INFO,
			labResult.getPathologicDescription().getDescription());
		assertTrue(labResult.getComment(), labResult.getComment().startsWith("Candida albicans"));
		assertTrue(StringUtils.isEmpty(labResult.getRefMale()));
		assertTrue(StringUtils.isEmpty(labResult.getRefFemale()));
		assertEquals(1517049608000l, labResult.getAnalyseTime().getTimeAsLong()); // 20180127114008
		assertEquals(1516812120000l, labResult.getObservationTime().getTimeAsLong()); // 20180124174200
		
		ILabItem item = labResult.getItem();
		assertEquals("VAGINA-ABSTRICH - Kultur aerob", item.getName());
		assertTrue(item.getGroup(), item.getGroup().startsWith("Z Automatisch"));
	}
	
	@Test
	public void testImportForMissingLabOrder() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/01TEST5005.hl7"), false, true);
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		// diese zeile kommt auch rein NTE|1||Das ist eine Bemerkung zum Befund\.br\Das ist eine weitere Bemerkung zum Befund|RE
		assertEquals(7, qrr.size());
		assertEquals(6, new Query<>(LabOrder.class).execute().size());
	}
	
	@Test
	public void testPatientRelatedNoteImportAsNoteLabResult_11154() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/Spermiogramm.hl7"), false,
			true);
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(3, qrr.size());
		
		boolean noteExists = false;
		boolean spernExists = false;
		for (LabResult labResult : qrr) {
			LabItem item = (LabItem) labResult.getItem();
			switch (item.getKuerzel()) {
			case "NOTE":
				assertEquals("Allgemeine Notiz", item.getName());
				assertEquals("AA", item.getGroup());
				assertEquals("1", item.getPrio());
				assertEquals("text", labResult.getResult());
				assertTrue(labResult.getComment().startsWith("Untersuchungsdatum und Zeit"));
				noteExists = true;
				break;
			case "SPERN":
				assertEquals("Spermien: nachweisbar", labResult.getResult());
				assertEquals("negativ", labResult.getRefMale());
				spernExists = true;
				break;
			default:
				break;
			}
			assertEquals(1520430605000l, labResult.getAnalyseTime().getTimeAsLong());
			assertEquals(1520316000000l, labResult.getObservationTime().getTimeAsLong());
		}
		
		assertTrue(spernExists);
		assertTrue(noteExists);
	}
	
}
