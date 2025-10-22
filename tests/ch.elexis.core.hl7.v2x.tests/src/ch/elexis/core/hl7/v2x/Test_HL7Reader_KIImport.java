package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.v2x.HL7ReaderV251;

/**
 * Testet den KI-HL7 Import (Konsultationstext) über den HL7Reader direkt.
 * Die Kons-ID aus der HL7-Datei wird dynamisch auf die echte Encounter-ID ersetzt.
 */
public class Test_HL7Reader_KIImport {

	private static final String TEST_FILE_NAME = "KI_Report_für_in_Kons.hl7";
	private static File testFile;
	private static String konsIdGenerated;
	private static DummyPatientResolver resolver;

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
	public void test_KI_Report_IsRecognizedAndContainsExpectedText() throws Exception {
		File preparedFile = prepareTestHl7FileWithEncounterId(testFile, konsIdGenerated);
		assertTrue(preparedFile.canRead());

		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(preparedFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV251.class, reader.getClass());

		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		assertNotNull("ObservationMessage sollte nicht null sein", observationMsg);

		var observations = observationMsg.getObservations();
		assertNotNull("Observations sollten nicht null sein", observations);
		assertFalse("Es sollten mindestens 1 Observation enthalten sein", observations.isEmpty());

		String kiText = observations.stream().filter(o -> o instanceof ch.elexis.hl7.model.TextData)
				.map(o -> ((ch.elexis.hl7.model.TextData) o).getText()).filter(StringUtils::isNotBlank).findFirst()
				.orElse("");

		System.out.println("KI Report Text (aus OBX): " + kiText);
		assertFalse("KI-Report Text sollte nicht leer sein", kiText.isBlank());

		String hl7Content = Files.readString(preparedFile.toPath());
		assertTrue("HL7-Datei sollte Beispieltext enthalten",
				hl7Content.contains("Eisen") || hl7Content.contains("Folsäure"));

		assertTrue("KI-Report sollte HL7-Report-Text enthalten",
				StringUtils.containsIgnoreCase(kiText, "Eisen") || StringUtils.containsIgnoreCase(kiText, "Folsäure"));

		var encounterOpt = CoreModelServiceHolder.get().load(konsIdGenerated, IEncounter.class);
		assertTrue("Encounter sollte nach KI-Import existieren", encounterOpt.isPresent());
		var encounter = encounterOpt.get();
		String encounterText = encounter.getVersionedEntry().getHead();

		System.out.println("Encounter-Text:\n" + encounterText);

		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		assertTrue("Encountertext sollte HL7 KI Import Marker enthalten",
				encounterText.contains("=== HL7 KI Import " + today));

		assertTrue("Encountertext sollte den KI-Reporttext enthalten",
				StringUtils.containsIgnoreCase(encounterText, "Eisen")
						|| StringUtils.containsIgnoreCase(encounterText, "Folsäure"));
	}

	/**
	 * Erstellt eine temporäre HL7-Datei mit dynamischer Kons-ID (aus Encounter)
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
