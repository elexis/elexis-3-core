package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import org.hl7.fhir.r4.model.Slot;
import org.hl7.fhir.r4.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IAppointmentSlotAttributeMapper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.util.TestUtil;

public class SlotTerminTransformerTest {

	private static SlotTerminTransformer transformer;
	private static IModelService coreModelService;
	private static IAppointmentService appointmentService;
	private static IAppointmentSlotAttributeMapper attributeMapper;

	@BeforeClass
	public static void beforeClass() throws Exception {
		coreModelService = AllTransformerTests.getCoreModelService();
		appointmentService = AllTransformerTests.getAppointmentService();
		transformer = new SlotTerminTransformer();
		setPrivateField(transformer, "coreModelService", coreModelService);
		setPrivateField(transformer, "appointmentService", appointmentService);
		attributeMapper = new IAppointmentSlotAttributeMapper(appointmentService);
		setPrivateField(transformer, "attributeMapper", attributeMapper);
	}

	private static void setPrivateField(Object object, String fieldName, Object value) throws Exception {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(object, value);
	}

	private IAppointment setupLocalAppointment(LocalDateTime startTime, LocalDateTime endTime, String reason,
			String state, String schedule) {
		IAppointmentBuilder appointmentBuilder = new IAppointmentBuilder(coreModelService, schedule, startTime, endTime,
				"TestType", state);
		IAppointment localAppointment = appointmentBuilder.build();
		localAppointment.setReason(reason);
		localAppointment.setState(state);
		localAppointment.setSubjectOrPatient("Test Patient");
		TestUtil.setId(localAppointment, "test-SlotTerminTransformerTest-id");
		coreModelService.save(localAppointment);
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
		Slot fhirSlot = createFHIRSlot("test-slot-id 112233", LocalDateTime.now().withHour(17),
				LocalDateTime.now().withHour(18),
				newScheduleName);
		Optional<IAppointment> result = transformer.updateLocalObject(fhirSlot, localAppointment);
		assertTrue(result.isPresent());
		IAppointment updatedAppointment = result.get();
		assertEquals(newScheduleName, updatedAppointment.getSchedule());
	}
}
