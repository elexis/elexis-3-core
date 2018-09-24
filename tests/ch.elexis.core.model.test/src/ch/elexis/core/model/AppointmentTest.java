package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class AppointmentTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		super.createPatient();
	}
	
	@After
	public void after(){
		super.removePatient();
		super.after();
	}
	
	@Test
	public void createFindDelete(){
		LocalDateTime begin = LocalDateTime.of(2018, 9, 24, 13, 23);
		LocalDateTime end = begin.plus(Duration.ofMinutes(15));
		
		IAppointment appointment = modelService.create(IAppointment.class);
		appointment.setReason("reason");
		appointment.setStart(begin);
		appointment.setEnd(end);
		appointment.setSubjectOrPatient(patient.getId());
		modelService.save(appointment);
		
		IQuery<IAppointment> query = modelService.getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__REASON, COMPARATOR.EQUALS, "reason");
		IAppointment stored = query.executeSingleResult().get();
		
		assertEquals(begin, stored.getStart());
		assertEquals(end, stored.getEnd());
		assertEquals(patient.getId(), stored.getSubjectOrPatient());
		
		modelService.remove(appointment);
	}
}
