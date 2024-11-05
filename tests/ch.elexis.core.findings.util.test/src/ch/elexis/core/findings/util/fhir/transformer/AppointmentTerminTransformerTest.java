package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.r4.model.Appointment;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.findings.util.fhir.transformer.mapper.IAppointmentAppointmentAttributeMapper;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.util.TestUtil;

public class AppointmentTerminTransformerTest {

	private static AppointmentTerminTransformer transformer;
	private static IModelService coreModelService;
	private static IAppointmentService appointmentService;
	private static IConfigService configService;
	private static IAppointmentAppointmentAttributeMapper attributeMapper;

	@BeforeClass
	public static void beforeClass() throws Exception {
		coreModelService = AllTransformerTests.getCoreModelService();
		appointmentService = AllTransformerTests.getAppointmentService();
		configService = AllTransformerTests.getConfigService();
		transformer = new AppointmentTerminTransformer();
		setPrivateField(transformer, "coreModelService", coreModelService);
		setPrivateField(transformer, "appointmentService", appointmentService);
		setPrivateField(transformer, "configService", configService);
		attributeMapper = new IAppointmentAppointmentAttributeMapper(appointmentService, coreModelService,
				configService);
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
		TestUtil.setId(localAppointment, "test-appointment-id");
		coreModelService.save(localAppointment);
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
		assertEquals(LocalDate.now().atTime(17, 0), updatedAppointment.getStartTime());
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
		assertEquals(LocalDate.now().atTime(17, 0), updatedAppointment.getEndTime());
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
		assertEquals("Updated Grund", updatedAppointment.getReason());
	}
}
