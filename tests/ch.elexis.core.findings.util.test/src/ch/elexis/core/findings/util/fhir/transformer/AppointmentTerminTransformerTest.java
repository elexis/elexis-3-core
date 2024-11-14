package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.r4.model.Appointment;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.util.TestUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;

public class AppointmentTerminTransformerTest {

	private static IFhirTransformer<Appointment, IAppointment> transformer;

	@BeforeClass
	public static void beforeClass() throws Exception {
		IFhirTransformerRegistry transformerRegistry = OsgiServiceUtil.getService(IFhirTransformerRegistry.class).get();
		transformer = transformerRegistry.getTransformerFor(Appointment.class, IAppointment.class);
	}

	private IAppointment setupLocalAppointment(LocalDateTime startTime, LocalDateTime endTime, String reason,
			String state, String schedule) {
		IAppointmentBuilder appointmentBuilder = new IAppointmentBuilder(AllTransformerTests.getCoreModelService(),
				schedule, startTime, endTime,
				"TestType", state);
		IAppointment localAppointment = appointmentBuilder.build();
		localAppointment.setReason(reason);
		localAppointment.setState(state);
		localAppointment.setSubjectOrPatient("Test Patient");
		TestUtil.setId(localAppointment, "test-appointment-id");
		AllTransformerTests.getCoreModelService().save(localAppointment);
		return localAppointment;
	}

	private Appointment createFHIRAppointment(String appointmentId, LocalDateTime startTime, LocalDateTime endTime,
			String description) {
		Appointment fhirAppointment = new Appointment();
		fhirAppointment.setId(appointmentId);
		fhirAppointment.setStart(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
		fhirAppointment.setEnd(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
		fhirAppointment.setDescription(description);
		return fhirAppointment;
	}

	@Test
	public void testStartTimeChange() throws Exception {
		IAppointment localAppointment = setupLocalAppointment(LocalDate.now().atTime(15, 0),
				LocalDate.now().atTime(16, 0), "Initial Grund", "Initial Status", "Initial Bereich");
		Appointment fhirAppointment = createFHIRAppointment("test-appointment-id", LocalDate.now().atTime(17, 0),
				LocalDate.now().atTime(18, 0), "Updated Grund");
		Optional<IAppointment> result = transformer.updateLocalObject(fhirAppointment, localAppointment);
		assertTrue(result.isPresent());
		IAppointment updatedAppointment = result.get();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		String expectedHistoryEntry = "Dauer geändert von " + LocalDate.now().atTime(16, 0).format(formatter) + " auf "
				+ LocalDate.now().atTime(18, 0).format(formatter) + " [Unbekannt]";
		assertTrue(updatedAppointment.getStateHistory().contains(expectedHistoryEntry));
	}

	@Test
	public void testEndTimeChange() throws Exception {
		IAppointment localAppointment = setupLocalAppointment(LocalDate.now().atTime(15, 0),
				LocalDate.now().atTime(16, 0), "Initial Grund", "Initial Status", "Initial Bereich");
		Appointment fhirAppointment = createFHIRAppointment("test-appointment-id", LocalDate.now().atTime(15, 0),
				LocalDate.now().atTime(17, 0), "Updated Grund");
		Optional<IAppointment> result = transformer.updateLocalObject(fhirAppointment, localAppointment);
		assertTrue(result.isPresent());
		IAppointment updatedAppointment = result.get();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		String expectedHistoryEntry = "Dauer geändert von " + LocalDate.now().atTime(16, 0).format(formatter) + " auf "
				+ LocalDate.now().atTime(17, 0).format(formatter) + " [Unbekannt]";
		assertTrue(updatedAppointment.getStateHistory().contains(expectedHistoryEntry));
	}

	@Test
	public void testReasonChange() throws Exception {
		IAppointment localAppointment = setupLocalAppointment(LocalDate.now().atTime(15, 0),
				LocalDate.now().atTime(16, 0), "Initial Grund", "Initial Status", "Initial Bereich");
		Appointment fhirAppointment = createFHIRAppointment("test-appointment-id", LocalDate.now().atTime(15, 0),
				LocalDate.now().atTime(16, 0), "Updated Grund");
		Optional<IAppointment> result = transformer.updateLocalObject(fhirAppointment, localAppointment);
		assertTrue(result.isPresent());
		IAppointment updatedAppointment = result.get();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		String expectedHistoryEntry = "Termin bearbeitet am " + LocalDateTime.now().format(formatter)
				+ " durch [Unbekannt]";
		assertTrue(updatedAppointment.getStateHistory().contains(expectedHistoryEntry));
	}
}
