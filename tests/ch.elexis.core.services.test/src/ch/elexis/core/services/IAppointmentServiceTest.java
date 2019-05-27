package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IAppointmentServiceTest extends AbstractServiceTest {
	
	private IAppointmentService appointmentService = OsgiServiceUtil.getService(IAppointmentService.class).get();
	private IAppointment savedAppointment;
	
	@Before
	public void before(){	
		//cleanup
		coreModelService.remove(coreModelService.getQuery(IAppointment.class).execute());
		assertEquals(0,  coreModelService.getQuery(IAppointment.class).execute().size());
		
		savedAppointment = new IAppointmentBuilder(coreModelService, "Notfall", LocalDateTime.of(2018, 01, 02, 9, 0),  LocalDateTime.of(2018, 01, 02, 9, 30),
			"gesperrt", "geplant").buildAndSave();
	}
	
	@Test
	public void testCommon() {
		Optional<IAppointment> load = coreModelService.load(savedAppointment.getId(), IAppointment.class);
		assertTrue(load.isPresent());
		assertEquals(30, load.get().getDurationMinutes().intValue());
		assertEquals("gesperrt", load.get().getType());
		assertEquals(1,  coreModelService.getQuery(IAppointment.class).execute().size());
	}
	
	@Test
	public void testUpdateBoundaries(){
		// check Bereich of Notfall and type is gesperrt - in that case no boundaries created
		assertEquals(1,  coreModelService.getQuery(IAppointment.class).execute().size());
		appointmentService.updateBoundaries("Notfall", LocalDate.of(2018, 01, 02));
		assertEquals(1,  coreModelService.getQuery(IAppointment.class).execute().size());
		
		// change type to OP - boundaries should be created
		savedAppointment.setType("OP");
		coreModelService.save(savedAppointment);
		appointmentService.updateBoundaries("Notfall", LocalDate.of(2018, 01, 02));
		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		query.and("tag", COMPARATOR.EQUALS, LocalDate.of(2018, 01, 02), false);
		List<IAppointment> results = query.execute();
		
		// check boundaries sorted by start time
		assertEquals(3,  results.size());
		results = results.stream().sorted((p1, p2) -> p1.getStartTime().compareTo(p2.getStartTime())).collect(Collectors.toList());
		assertEquals(LocalDateTime.of(2018, 01, 02, 0, 0),  results.get(0).getStartTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 8, 0),  results.get(0).getEndTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 9, 0),  results.get(1).getStartTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 9, 30),  results.get(1).getEndTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 18, 0),  results.get(2).getStartTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 23, 59),  results.get(2).getEndTime());
	}
	
	@Test
	public void testClone(){
		IAppointment cloned = appointmentService.clone(savedAppointment);
		assertNotNull(cloned.getId());
		assertNotEquals(savedAppointment.getId(), cloned.getId());
		assertEquals(savedAppointment.getSubjectOrPatient(), cloned.getSubjectOrPatient());
		assertEquals(savedAppointment.getStartTime(), cloned.getStartTime());
		assertEquals(savedAppointment.getEndTime(), cloned.getEndTime());
		assertEquals(savedAppointment.getType(), cloned.getType());
		assertEquals(savedAppointment.getState(), cloned.getState());
		assertEquals(savedAppointment.getPriority(), cloned.getPriority());
	}
	
	@Test
	public void testDelete(){
		// delete single
		assertEquals(1,  coreModelService.getQuery(IAppointment.class).execute().size());
		appointmentService.delete(savedAppointment, false);
		assertEquals(0,  coreModelService.getQuery(IAppointment.class).execute().size());
		
		//@todo delete with linkgroup
	}
}
