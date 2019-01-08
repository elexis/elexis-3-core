package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

public class Test_Reminder extends AbstractPersistentObjectTest {
	
	private Anwender anwender;
	private Patient patient;
	
	private Reminder reminderA, reminderB, reminderC;
	
	public Test_Reminder(JdbcLink link){
		super(link);
		
		User user = User.load("Administrator");
		if (user.getAssignedContact() == null) {
			anwender = new Anwender("Name", "Vorname", (String) null, "w");
			user.setAssignedContact(anwender);
		} else {
			anwender = user.getAssignedContact();
		}
		// set user and Mandant in system
		ElexisEventDispatcher.getInstance()
			.fire(new ElexisEvent(user, User.class, ElexisEvent.EVENT_SELECTED));
		Mandant m = new Mandant("Mandant", "Erwin", "26.07.1979", "m");
		patient = new Patient("Mia", "Krank", "22041982", "w");
		CoreHub.setMandant(m);
	}
	
	@After
	public void cleanup(){
		if (reminderA != null) {
			reminderA.delete();
			reminderA = null;
		}
		if (reminderB != null) {
			reminderB.delete();
			reminderB = null;
		}
		if (reminderC != null) {
			reminderC.delete();
			reminderC = null;
		}
	}
	
	@Test
	public void testSetResponsibleUser() throws InterruptedException{
		reminderA = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		long lastUpdate = reminderA.getLastUpdate();
		assertNotSame(0L, reminderA.getLastUpdate());
		Thread.sleep(2);
		reminderA.setResponsible(Collections.singletonList(anwender));
		assertTrue(reminderA.getLastUpdate() > lastUpdate);
		assertEquals(1, reminderA.getResponsibles().size());
		assertEquals(StringConstants.EMPTY, reminderA.get(Reminder.FLD_RESPONSIBLE));
		lastUpdate = reminderA.getLastUpdate();
		Thread.sleep(2);
		reminderA.setResponsible(new ArrayList<Anwender>());
		assertTrue(reminderA.getLastUpdate() > lastUpdate);
		assertEquals(0, reminderA.getResponsibles().size());
		assertEquals(StringConstants.EMPTY, reminderA.get(Reminder.FLD_RESPONSIBLE));
		reminderA.setResponsible(null);
		assertTrue(reminderA.getLastUpdate() > lastUpdate);
		assertNull(reminderA.getResponsibles());
		assertEquals(Reminder.ALL_RESPONSIBLE, reminderA.get(Reminder.FLD_RESPONSIBLE));
	}
	
	@Test
	public void testFindOpenRemindersResponsibleFor(){
		reminderA = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderA.setResponsible(Collections.singletonList(CoreHub.actUser));
		reminderA.set(Reminder.FLD_STATUS, Integer.toString(ProcessStatus.CLOSED.numericValue()));
		
		reminderB = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderB.setResponsible(Collections.singletonList(CoreHub.actUser));
		List<Reminder> findOpenRemindersResponsibleFor =
			Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, false, null, false);
		assertEquals(1, findOpenRemindersResponsibleFor.size());
		
		reminderC = new Reminder(patient, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderC.setResponsible(null);
		assertEquals(2,
			Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, false, null, false).size());
		assertEquals(1,
			Reminder.findOpenRemindersResponsibleFor(null, false, patient, false).size());
		
		Reminder popupReminder = new Reminder(patient, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.POPUP_ON_PATIENT_SELECTION, "", "TestMessage");
		popupReminder.setResponsible(Collections.singletonList(CoreHub.actUser));
		assertEquals(3,
			Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, false, null, false).size());
		assertEquals(1,
			Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, false, null, true).size());
		assertEquals(1,
			Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, false, patient, true).size());
		
		TimeTool timeTool = new TimeTool(LocalDate.now().minusDays(1));
		Reminder dueReminder = new Reminder(null, timeTool.toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		dueReminder.setResponsible(Collections.singletonList(anwender));
		// is 120217
		List<Reminder> dueReminders =
			Reminder.findOpenRemindersResponsibleFor(anwender, true, null, false);
		assertEquals(2, dueReminders.size());
		
		dueReminder.delete();
		popupReminder.delete();
	}
	
	@Test
	public void testFindAllUserIsResponsibleFor(){
		reminderA = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderA.setResponsible(Collections.singletonList(CoreHub.actUser));
		reminderA.set(Reminder.FLD_STATUS, Integer.toString(ProcessStatus.CLOSED.numericValue()));
		
		reminderB = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderB.setResponsible(Collections.singletonList(CoreHub.actUser));
		
		reminderC = new Reminder(null, null, Visibility.ALWAYS, "", "TestMessage");
		reminderC.setResponsible(null);
		
		assertEquals(3, Reminder.findAllUserIsResponsibleFor(CoreHub.actUser, false).size());
		assertEquals(2, Reminder.findAllUserIsResponsibleFor(CoreHub.actUser, true).size());
	}
	
	@Test
	public void testFindRemindersDueFor(){
		reminderA = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderA.setResponsible(Collections.singletonList(CoreHub.actUser));
		reminderA.set(Reminder.FLD_DUE, new TimeTool().toString(TimeTool.DATE_GER));
		
		reminderB = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessage");
		reminderB.setResponsible(Collections.singletonList(CoreHub.actUser));
		TimeTool timeTool = new TimeTool();
		timeTool.addDays(-2);
		reminderB.set(Reminder.FLD_DUE, timeTool.toString(TimeTool.DATE_GER));
		
		reminderC = new Reminder(null, null, Visibility.ALWAYS, "", "TestMessage");
		reminderC.setResponsible(null);
		
		List<Reminder> findRemindersDueFor =
			Reminder.findRemindersDueFor(null, CoreHub.actUser, false);
		assertEquals(2, findRemindersDueFor.size());
	}
	
	@Test
	public void testFindDifferentialChangedReminders() throws InterruptedException, SQLException{
		reminderA = new Reminder(null, new TimeTool().toString(TimeTool.DATE_GER),
			Visibility.ALWAYS, "", "TestMessageA");
		
		long highestLastUpdate = PersistentObject.getHighestLastUpdate(Reminder.TABLENAME);
		Thread.sleep(5);
		
		// circumvent persistent object on updating element, 
		// emulating behavior of a third elexis instance
		String sql =
			"UPDATE " + Reminder.TABLENAME + " SET LASTUPDATE='" + System.currentTimeMillis()
				+ "', MESSAGE='TestMessageAUpdated' WHERE ID='" + reminderA.getId() + "'";
		PreparedStatement ps = Reminder.getDefaultConnection().getPreparedStatement(sql);
		int executeUpdate = ps.executeUpdate();
		assertEquals(1, executeUpdate);
		Reminder.getDefaultConnection().releasePreparedStatement(ps);
		
		Query<Reminder> qre = new Query<>(Reminder.class);
		qre.add(Reminder.FLD_LASTUPDATE, Query.GREATER, Long.toString(highestLastUpdate));
		List<Reminder> changed = qre.execute();
		
		assertEquals(1, changed.size());
		assertEquals("TestMessageA", changed.get(0).getMessage());
		// support deleted
		
		qre = new Query<>(Reminder.class, Reminder.TABLENAME, true, null);
		qre.add(Reminder.FLD_LASTUPDATE, Query.GREATER, Long.toString(highestLastUpdate));
		changed = qre.execute();
		
		assertEquals(1, changed.size());
		assertEquals("TestMessageAUpdated", changed.get(0).getMessage());
	}
	
}
