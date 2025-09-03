package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.constants.ExtInfoConstants;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ITextReplacementServiceTest extends AbstractServiceTest {

	private ITextReplacementService textReplacementService = OsgiServiceUtil.getService(ITextReplacementService.class)
			.get();
	private IContextService contextService = OsgiServiceUtil.getService(IContextService.class).get();

	private static IAppointment appointment;

	@BeforeClass
	public static void beforeClass() {
		LocalDateTime ldt = LocalDateTime.of(2019, 12, 12, 12, 12);
		appointment = new IAppointmentBuilder(coreModelService, "testSchedule", ldt, ldt.plusHours(1), "type", "state")
				.buildAndSave();
	}

	@Test
	public void patientReplacement() {
		contextService.setActivePatient(AllServiceTests.getPatient());

		String template = "Liebe[Patient:mw:r/ ] [Patient.Name] [Patient.Vorname],";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Liebe Patient Test,", replaced);

		template = "[Patient.Geschlecht]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("w", replaced);

		AllServiceTests.getPatient().setPersonalAnamnese("pers anamnese");
		CoreModelServiceHolder.get().save(AllServiceTests.getPatient());
		template = "[Patient.PersAnamnese]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("pers anamnese", replaced);

		AllServiceTests.getPatient().setFamilyAnamnese("fam anamnese");
		CoreModelServiceHolder.get().save(AllServiceTests.getPatient());
		template = "[Patient.FamilienAnamnese]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("fam anamnese", replaced);

		AllServiceTests.getPatient().setRisk("risks");
		CoreModelServiceHolder.get().save(AllServiceTests.getPatient());
		template = "[Patient.Risiken]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("risks", replaced);

		AllServiceTests.getPatient().addXid(XidConstants.DOMAIN_AHV, "1234", true);
		CoreModelServiceHolder.get().save(AllServiceTests.getPatient());
		template = "[Patient.AHV]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("1234", replaced);

		AllServiceTests.getPatient().setLegalGuardian(AllServiceTests.getCoverage().getCostBearer());
		CoreModelServiceHolder.get().save(AllServiceTests.getPatient());
		template = "[Patient:-:-:GesetzVertreter]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Test Organization\n" + "Street 10\n" + "123 City\n", replaced);
		AllServiceTests.getPatient().setLegalGuardian(null);
		CoreModelServiceHolder.get().save(AllServiceTests.getPatient());
	}

	@Test
	public void adressatReplacement() {
		contextService.getRootContext().setNamed("Adressat", AllServiceTests.getMandator());

		String template = "Liebe[Adressat:mw:r/ ] [Adressat.Vorname]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Lieber Test", replaced);

		template = "[Adressat.Anschrift]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Herr\n" + "Test Mandant\n" + "Street 100\n" + "123 City\n", replaced);
	}
	
	@Test
	public void terminReplacement() {
		contextService.getRootContext().setTyped(appointment);

		String template = "[Termin.Tag] [Termin.zeit] [Termin.Bereich]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("12.12.2019 12:12 testSchedule", replaced);
	}

	@Test
	public void konsultationReplacement() {
		createTestMandantPatientFallBehandlung();

		IEncounter encounter = testEncounters.get(0);
		encounter.getVersionedEntry().update("Test consultation\ndone by user", "user");
		CoreModelServiceHolder.get().save(encounter);
		contextService.getRootContext().setTyped(encounter);

		String template = "[Konsultation.Eintrag]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Test consultation\ndone by user", replaced);
	}

	@Test
	public void mandantReplacement() {
		contextService.setActiveMandator(AllServiceTests.getMandator());

		String template = "[Mandant.Anschrift]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Herr\n" + "Test Mandant\n" + "Street 100\n" + "123 City\n", replaced);

		IPerson person = AllServiceTests.getMandator().asIPerson();
		person.setTitel("Titel");
		CoreModelServiceHolder.get().save(person);
		template = "[Mandant.Titel]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Titel", replaced);

		person = AllServiceTests.getMandator().asIPerson();
		person.setEmail("test@test.tst");
		CoreModelServiceHolder.get().save(person);
		template = "[Mandant.E-Mail]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("test@test.tst", replaced);

		AllServiceTests.getMandator().setExtInfo("TarmedSpezialität", "Allgemein");
		CoreModelServiceHolder.get().save(AllServiceTests.getMandator());
		template = "[Mandant.TarmedSpezialität]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Allgemein", replaced);

		AllServiceTests.getMandator().addXid(XidConstants.EAN, "2000000000000", true);
		CoreModelServiceHolder.get().save(AllServiceTests.getMandator());
		template = "[Mandant.EAN]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("2000000000000", replaced);

		AllServiceTests.getMandator().addXid(XidConstants.DOMAIN_KSK, "C000002", true);
		CoreModelServiceHolder.get().save(AllServiceTests.getMandator());
		template = "[Mandant.KSK]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("C000002", replaced);
	}

	@Test
	public void datumReplacement() {
		String template = "[Datum.heute]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals(TimeUtil.DATE_GER.format(LocalDate.now()), replaced);

		template = "[Datum.Datum]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals(TimeUtil.DATE_GER.format(LocalDate.now()), replaced);
	}

	@Test
	public void aufReplacement() {
		LocalDate start = LocalDate.of(2000, 1, 12);
		ISickCertificate sickCertificate = CoreModelServiceHolder.get().create(ISickCertificate.class);
		sickCertificate.setPatient(AllServiceTests.getPatient());
		sickCertificate.setStart(start);
		sickCertificate.setEnd(start.plusDays(7));
		sickCertificate.setNote("note");
		sickCertificate.setReason("reason");
		sickCertificate.setPercent(99);
		CoreModelServiceHolder.get().save(sickCertificate);

		contextService.getRootContext().setTyped(sickCertificate);

		String template = "[AUF.von]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("12.01.2000", replaced);
		
		template = "[AUF.bis]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("19.01.2000", replaced);

		template = "[AUF.Grund]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("reason", replaced);

		template = "[AUF.Prozent]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("99", replaced);

		template = "[AUF.Zusatz]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("note", replaced);
	}

	@Test
	public void fallReplacement() {
		contextService.getRootContext().setNamed("Fall", AllServiceTests.getCoverage());

		String template = "[Fall.Versicherungsnummer]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("1234-5678", replaced);

		template = "[Fall.Kostentraeger]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Test Organization\n" + "Street 10\n" + "123 City\n", replaced);

		template = "[Fall:-:-:Kostentraeger]";
		replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Test Organization\n" + "Street 10\n" + "123 City\n", replaced);
	}
}
