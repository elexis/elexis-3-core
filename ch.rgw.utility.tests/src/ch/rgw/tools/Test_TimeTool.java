package ch.rgw.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

public class Test_TimeTool {
	
	@Test
	public void testGetDurationToNowString(){
		TimeTool timeTool = new TimeTool();
		
		String duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertFalse(containsDigit(duration));
		
		timeTool = new TimeTool();
		timeTool.addDays(1);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		String[] split = splitByFormat(timeTool.getDaysFormat(1));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(-1);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getDaysFormat(-1));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(15);
		// in order not to break the week / day barrier in 
		// timeTool.getDurationToNowString() due to the required time for test execution
		timeTool.addSeconds(10);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getWeeksFormat(2));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(-15);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getWeeksFormat(-2));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		Date d = timeTool.getTime();
		timeTool.addDays(64);
		LocalDateTime now = LocalDateTime.now();
		
		int days = (int) now.until(timeTool.toLocalDateTime(), ChronoUnit.DAYS);
		duration = timeTool.getDurationToTimeAsString(now);
		
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		if (days == 64) {
			split = splitByFormat(timeTool.getMonthsFormat(2));
		} else if (days == 63) {
			// if the execution of this testcase is too slow we have after adding of 64 days only 63 days and 23:59 left
			split = splitByFormat(timeTool.getWeeksFormat(2));
		}
		else {
			fail("invalid until date calculation: " + days);
		}
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(-64);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getMonthsFormat(-2));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(732);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getYearsFormat(2));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(-732);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getYearsFormat(-2));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(printFailure(timeTool, duration, split),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
	}
	
	@Test
	public void testNullSafeCompareTo() {
		TimeTool timeTool = new TimeTool();
		timeTool.addDays(-732);
		assertEquals(1, TimeTool.compare(null, timeTool));
		assertEquals(-1, TimeTool.compare(timeTool, null));
		assertEquals(0, TimeTool.compare(null, null));
		assertEquals(0, TimeTool.compare(timeTool, timeTool));
		assertEquals(1, TimeTool.compare(timeTool, new TimeTool()));
		assertEquals(-1, TimeTool.compare(new TimeTool(), timeTool));
	}
	
	private String printFailure(TimeTool timeTool, String duration, String[] split){
		return timeTool.toDBString(true) + ": duration=" + duration + ", " + Arrays.toString(split);
	}
	
	private boolean containsDigit(String duration){
		for (int i = 0; i < duration.length(); i++) {
			char c = duration.charAt(i);
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}
	
	private String[] splitByFormat(String format){
		return format.split("%d");
	}
	
}
