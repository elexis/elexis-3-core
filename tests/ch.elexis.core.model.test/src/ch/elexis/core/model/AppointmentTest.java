package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;

public class AppointmentTest extends AbstractTest {
	
	@Override
	@Before
	public void before(){
		super.before();
		super.createPatient();
	}
	
	@Override
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void createFindDelete(){
		LocalDateTime begin = LocalDateTime.of(2018, 9, 24, 13, 23);
		LocalDateTime end = begin.plus(Duration.ofMinutes(15));
		
		IAppointment appointment = coreModelService.create(IAppointment.class);
		appointment.setReason("reason");
		appointment.setStartTime(begin);
		appointment.setEndTime(end);
		appointment.setState("geplant");
		appointment.setType("gesperrt");
		appointment.setSchedule("Notfall");
		appointment.setSubjectOrPatient(patient.getId());
		appointment.setTreatmentReason(1);
		appointment.setCaseType(2);
		appointment.setInsuranceType(3);
		coreModelService.save(appointment);
		
		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__REASON, COMPARATOR.EQUALS, "reason");
		IAppointment stored = query.executeSingleResult().get();
		
		assertEquals(begin, stored.getStartTime());
		assertEquals(end, stored.getEndTime());
		assertEquals(patient.getLabel(), stored.getSubjectOrPatient());
		assertEquals(15, stored.getDurationMinutes().intValue());
		assertEquals("geplant", stored.getState());
		assertEquals("gesperrt", stored.getType());
		assertEquals("Notfall", stored.getSchedule());
		assertEquals(1, stored.getTreatmentReason());
		assertEquals(2, stored.getCaseType());
		assertEquals(3, stored.getInsuranceType());
		coreModelService.remove(appointment);
	}
	
	@Test
	public void createQueryDelete(){
		LocalDateTime begin = LocalDateTime.of(2018, 9, 24, 13, 23);
		LocalDateTime end = begin.plus(Duration.ofMinutes(15));
		
		IAppointment appointment = coreModelService.create(IAppointment.class);
		appointment.setReason("reason");
		appointment.setStartTime(begin);
		appointment.setEndTime(end);
		appointment.setState("geplant");
		appointment.setType("gesperrt");
		appointment.setSchedule("Notfall");
		appointment.setSubjectOrPatient(patient.getId());
		coreModelService.save(appointment);
		
		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		query.and("tag", COMPARATOR.GREATER_OR_EQUAL, begin.toLocalDate());
		assertNotNull(query.executeSingleResult().orElse(null));
		
		query = coreModelService.getQuery(IAppointment.class);
		query.and("tag", COMPARATOR.GREATER_OR_EQUAL, begin.plusDays(1).toLocalDate());
		assertNull(query.executeSingleResult().orElse(null));
		
		coreModelService.remove(appointment);
	}
	
	@Test
	public void setStateIncludesStateHistory() {
		LocalDateTime begin = LocalDateTime.of(2018, 9, 24, 13, 23);
		LocalDateTime end = begin.plus(Duration.ofMinutes(15));
		
		IAppointment appointment = coreModelService.create(IAppointment.class);
		appointment.setReason("reason");
		appointment.setStartTime(begin);
		appointment.setEndTime(end);
		appointment.setState("started");
		coreModelService.save(appointment);
		
		assertTrue(appointment.getStateHistory().contains("started"));
		
		appointment.setState("modified");
		coreModelService.save(appointment);
		
		assertTrue(appointment.getStateHistory().contains("started"));
		assertTrue(appointment.getStateHistory().contains("modified"));
			
		coreModelService.remove(appointment);
	}
}
