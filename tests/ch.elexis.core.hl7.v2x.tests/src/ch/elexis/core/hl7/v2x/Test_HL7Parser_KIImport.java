package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.rgw.tools.Result;

/**
 * Testet den KI-HL7 Import (Konsultationstext) über den erweiterten HL7Parser.
 * Die Kons-ID aus der HL7-Datei wird dynamisch auf die echte Encounter-ID
 * ersetzt.
 */
public class Test_HL7Parser_KIImport {

	private static final String TEST_FILE_NAME = "KI_Report_für_in_Kons.hl7";
	private static File testFile;
	private static String konsIdGenerated;

	@BeforeClass
	public static void setup() {
		testFile = new File("rsc/KIMKI-KICh/" + TEST_FILE_NAME);
		assertTrue("Testdatei fehlt: " + testFile.getAbsolutePath(), testFile.exists());

		IEncounter encounter = new IEncounterBuilder(CoreModelServiceHolder.get(), null, null).date(LocalDateTime.now())
				.buildAndSave();
		konsIdGenerated = encounter.getId();

		System.out.println("Generierte Encounter-ID für Test: " + konsIdGenerated);
		assertNotNull(konsIdGenerated);
		assertFalse(konsIdGenerated.isBlank());
	}

	@Test
	public void testKiReportIsRecognizedAndWrittenToEncounter() throws Exception {
		File preparedFile = prepareTestHl7FileWithEncounterId(testFile, konsIdGenerated);
		assertTrue(preparedFile.canRead());
		HL7Parser parser = new HL7Parser("KiLab", new DummyPatientResolver(null), null, new DummyLabContactResolver(),
				false);
		Result<?> result = parser.importFile(preparedFile, null, null, true);
		assertNotNull(result);
		assertTrue("Import sollte OK sein", result.isOK());
		IEncounter updated = CoreModelServiceHolder.get().load(konsIdGenerated, IEncounter.class).orElse(null);
		assertNotNull("Encounter mit Kons-ID sollte existieren", updated);
		String encounterText = updated.getVersionedEntry().getHead();
		assertNotNull("Encountertext sollte nicht null sein", encounterText);
		assertTrue("Encountertext sollte KI Import Marker enthalten",
				StringUtils.containsIgnoreCase(encounterText, "HL7 KI Import"));
		assertTrue("Encountertext sollte Datum/Zeit enthalten",
				encounterText.contains(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
		String hl7Content = Files.readString(preparedFile.toPath());
		assertTrue("HL7-Datei sollte Beispieltext enthalten",
				hl7Content.contains("Eisen") || hl7Content.contains("Folsäure"));
		assertTrue("Encountertext sollte HL7-Report-Text enthalten",
				StringUtils.containsIgnoreCase(encounterText, "Eisen")
						|| StringUtils.containsIgnoreCase(encounterText, "Folsäure"));
	}

	/**
	 * Erstellt eine temporäre HL7-Datei mit der dynamischen Kons-ID (aus Encounter)
	 */
	private static File prepareTestHl7FileWithEncounterId(File original, String konsId) throws IOException {
		String hl7 = Files.readString(original.toPath());
		hl7 = hl7.replaceAll("\\|999999\\|", "|" + konsId + "|");
		hl7 = hl7.replaceAll("999999\\^", konsId + "^");

		File temp = File.createTempFile("KI_Report_", ".hl7");
		Files.writeString(temp.toPath(), hl7);
		System.out.println("Temporäre HL7-Datei erzeugt: " + temp.getAbsolutePath());
		return temp;
	}
}
