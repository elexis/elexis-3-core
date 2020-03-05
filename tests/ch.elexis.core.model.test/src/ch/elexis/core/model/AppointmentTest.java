package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;

public class AppointmentTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		super.createPatient();
	}
	
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
		coreModelService.save(appointment);
		
		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__REASON, COMPARATOR.EQUALS, "reason");
		IAppointment stored = query.executeSingleResult().get();
		
		assertEquals(begin, stored.getStartTime());
		assertEquals(end, stored.getEndTime());
		assertEquals(patient.getId(), stored.getSubjectOrPatient());
		assertEquals(15, stored.getDurationMinutes().intValue());
		assertEquals("geplant", stored.getState());
		assertEquals("gesperrt", stored.getType());
		assertEquals("Notfall", stored.getSchedule());
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
}
