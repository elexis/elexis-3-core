package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.model.agenda.EndingType;
import ch.elexis.core.model.agenda.SeriesType;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.AppointmentState;
import ch.elexis.core.types.AppointmentType;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IAppointmentServiceTest extends AbstractServiceTest {

	private IAppointmentService appointmentService = OsgiServiceUtil.getService(IAppointmentService.class).get();
	private IAppointment savedAppointment;

	@BeforeClass
	public static void beforeClass() {
		IConfigService iConfigService = OsgiServiceUtil.getService(IConfigService.class).get();
		iConfigService.set("agenda/bereiche", "Notfall,MPA,OP,Arzt 1,Arzt 2");
		iConfigService.set("agenda/bereich/Arzt 1/type", "CONTACT/be5370812884c8fc5019123");
		iConfigService.set("agenda/TerminTypen",
				"frei,gesperrt,Notfall,Selbstzahler,Neuer Pat,Kontrolle,Termin,Checkup,OP,24h-BD / ApneaLink,Medicosearch,Sperrung,Sitzung,Reminder,Sonografie");
	}

	@Before
	public void before() {
		savedAppointment = new IAppointmentBuilder(coreModelService, "Notfall", LocalDateTime.of(2018, 01, 02, 9, 0),
				LocalDateTime.of(2018, 01, 02, 9, 30), appointmentService.getType(AppointmentType.BOOKED),
				appointmentService.getState(AppointmentState.DEFAULT)).buildAndSave();
	}

	@After
	public void after() {
		// cleanup
		coreModelService.remove(coreModelService.getQuery(IAppointment.class).execute());
		assertEquals(0, coreModelService.getQuery(IAppointment.class).execute().size());
	}

	@Test
	public void testCommon() {
		Optional<IAppointment> load = coreModelService.load(savedAppointment.getId(), IAppointment.class);
		assertTrue(load.isPresent());
		assertEquals(30, load.get().getDurationMinutes().intValue());
		assertEquals(appointmentService.getType(AppointmentType.BOOKED), load.get().getType());
		assertEquals(1, coreModelService.getQuery(IAppointment.class).execute().size());
		assertEquals(15, appointmentService.getTypes().size());
	}

	@Test
	public void testUpdateBoundaries() {
		// change type to OP - boundaries should be created
		savedAppointment.setType("OP");
		coreModelService.save(savedAppointment);
		appointmentService.updateBoundaries("Notfall", LocalDate.of(2018, 01, 02));
		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		query.and("tag", COMPARATOR.EQUALS, LocalDate.of(2018, 01, 02), false);
		List<IAppointment> results = query.execute();

		// check boundaries sorted by start time
		assertEquals(3, results.size());
		results = results.stream().sorted((p1, p2) -> p1.getStartTime().compareTo(p2.getStartTime()))
				.collect(Collectors.toList());
		assertEquals(LocalDateTime.of(2018, 01, 02, 0, 0), results.get(0).getStartTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 8, 0), results.get(0).getEndTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 9, 0), results.get(1).getStartTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 9, 30), results.get(1).getEndTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 18, 0), results.get(2).getStartTime());
		assertEquals(LocalDateTime.of(2018, 01, 02, 23, 59), results.get(2).getEndTime());
	}

	@Test
	public void testUpdateNoBoundaries() {
		// check Bereich of Notfall and type is gesperrt - in that case no boundaries
		// created
		assertEquals(1, coreModelService.getQuery(IAppointment.class).execute().size());
		appointmentService.updateBoundaries("Notfall", LocalDate.of(2018, 01, 02));
		// @todo on server its always 3
		assertEquals(1, coreModelService.getQuery(IAppointment.class).execute().size());
	}

	@Test
	public void testClone() {
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
	public void testDelete() {
		// delete single
		assertEquals(1, coreModelService.getQuery(IAppointment.class).execute().size());
		appointmentService.delete(savedAppointment, false);
		assertEquals(0, coreModelService.getQuery(IAppointment.class).execute().size());
	}

	@Test
	public void series() {
		// create series
		LocalDate previousMonday = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
		LocalDateTime seriesStart = previousMonday.atTime(10, 30);
		LocalTime seriesEnd = LocalTime.of(11, 00);

		IAppointmentSeries series = appointmentService.createAppointmentSeries();
		series.setStartTime(seriesStart);
		series.setSeriesEndTime(seriesEnd);
		series.setReason("test");

		series.setSeriesType(SeriesType.WEEKLY);
		series.setSeriesPatternString("1,246");
		series.setEndingType(EndingType.AFTER_N_OCCURENCES);
		series.setEndingPatternString("2");
		List<IAppointment> appointments = appointmentService.saveAppointmentSeries(series);
		assertNotNull(appointments);
		assertEquals(6, appointments.size());
		assertTrue(appointments.get(0).isRecurring() && appointments.get(3).isRecurring());
		assertEquals("test", appointments.get(0).getReason());
		assertEquals("test", appointments.get(3).getReason());

		// delete series, only first
		appointmentService.delete(appointments.get(0), false);
		assertTrue(appointments.get(0).isRecurring() && appointments.get(3).isRecurring());
		assertTrue(appointments.get(0).isDeleted());
		assertFalse(appointments.get(3).isDeleted());
		// delete series, all
		appointmentService.delete(appointments.get(3), true);
		assertTrue(appointments.get(2).isDeleted());
		assertTrue(appointments.get(3).isDeleted());
		assertTrue(appointments.get(4).isDeleted());
	}

	@Test
	public void setAreaType() {
		appointmentService.setAreaType("Arzt 1", AreaType.GENERIC, null);
		appointmentService.setAreaType("Notfall", AreaType.CONTACT, "be5370812884c8fc5019123");

		List<Area> areas = appointmentService.getAreas();
		assertEquals(5, areas.size());
		for (Area area : areas) {
			if ("Notfall".equals(area.getName())) {
				assertEquals(AreaType.CONTACT, area.getType());
				assertEquals("be5370812884c8fc5019123", area.getContactId());
			} else {
				assertEquals(AreaType.GENERIC, area.getType());
			}
		}

		appointmentService.setAreaType("Notfall", AreaType.GENERIC, null);
		appointmentService.setAreaType("Arzt 1", AreaType.CONTACT, "be5370812884c8fc5019123");
	}

	@Test
	public void getAreas() {
		List<Area> areas = appointmentService.getAreas();
		assertEquals(5, areas.size());
		for (Area area : areas) {
			if ("Arzt 1".equals(area.getName())) {
				assertEquals(AreaType.CONTACT, area.getType());
				assertEquals("be5370812884c8fc5019123", area.getContactId());
			} else {
				assertEquals(AreaType.GENERIC, area.getType());
			}
		}
	}

}
