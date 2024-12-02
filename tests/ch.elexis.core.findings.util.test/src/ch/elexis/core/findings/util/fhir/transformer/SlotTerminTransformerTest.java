package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Slot;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.test.util.TestUtil;
import ch.elexis.core.utils.OsgiServiceUtil;

public class SlotTerminTransformerTest {

	private static SlotTerminTransformer transformer;
	private static IAppointmentService appointmentService;


	@BeforeClass
	public static void beforeClass() throws Exception {


		appointmentService = OsgiServiceUtil.getService(IAppointmentService.class).get();
		IFhirTransformerRegistry transformerRegistry = OsgiServiceUtil.getService(IFhirTransformerRegistry.class).get();
		transformer = (SlotTerminTransformer) transformerRegistry.getTransformerFor(Slot.class, IAppointment.class);
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
		TestUtil.setId(localAppointment, "test-SlotTerminTransformerTest-id");
		AllTransformerTests.getCoreModelService().save(localAppointment);
		return localAppointment;
	}

	private Slot createFHIRSlot(String slotId, LocalDateTime startTime, LocalDateTime endTime,
			String scheduleReference) {
		Slot fhirSlot = new Slot();
		fhirSlot.setId(slotId);
		fhirSlot.setStart(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
		fhirSlot.setEnd(Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()));
		fhirSlot.setSchedule(new Reference(scheduleReference));
		return fhirSlot;
	}

	@Test
	public void testAreaChange() throws Exception {
		String newScheduleName = "Neuer Bereich";
		appointmentService.addArea(newScheduleName);
		IAppointment localAppointment = setupLocalAppointment(LocalDateTime.now().withHour(15),
				LocalDateTime.now().withHour(16), "Initial Grund", "Initial Status", "Initial Bereich");
		Slot fhirSlot = createFHIRSlot("test-slot-id-112233", LocalDateTime.now().withHour(17),
				LocalDateTime.now().withHour(18), newScheduleName);
		Optional<IAppointment> result = transformer.updateLocalObject(fhirSlot, localAppointment);
		assertTrue(result.isPresent());
		IAppointment updatedAppointment = result.get();
		assertEquals(newScheduleName, updatedAppointment.getSchedule());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		String expectedHistoryEntry = Messages.AppointmentHistory_Move_From + " "
				+ LocalDateTime.now().withHour(15).format(formatter) + " (Initial Bereich) "
				+ Messages.AppointmentHistory_Move_To + " " + LocalDateTime.now().withHour(17).format(formatter) + " ("
				+ newScheduleName + ") [Unbekannt]";
		assertTrue(updatedAppointment.getStateHistory().contains(expectedHistoryEntry));
	}
}
