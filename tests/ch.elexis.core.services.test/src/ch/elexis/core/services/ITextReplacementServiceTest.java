package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.builder.IAppointmentBuilder;
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

	}

	@Test
	public void adressatReplacement() {
		contextService.getRootContext().setNamed("Adressat", AllServiceTests.getMandator());

		String template = "Liebe[Adressat:mw:r/ ] [Adressat.Vorname]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Lieber Test", replaced);
	}
	
	@Test
	public void terminReplacement() {
		contextService.getRootContext().setTyped(appointment);

		String template = "[Termin.Tag] [Termin.zeit] [Termin.Bereich]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("12.12.2019 12:12 testSchedule", replaced);
	}

	@Test
	public void mandantReplacement() {
		contextService.setActiveMandator(AllServiceTests.getMandator());

		String template = "[Mandant.Anschrift]";
		String replaced = textReplacementService.performReplacement(contextService.getRootContext(), template);
		assertEquals("Herr\n" + "Test Mandant\n" + "Street 100\n" + "123 City\n", replaced);
	}
}
