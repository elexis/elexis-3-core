package ch.rgw.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
		assertTrue(duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(-1);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getDaysFormat(-1));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(timeTool.toString(),
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
		assertTrue(timeTool.toString() + ": " + duration + " | " + split[0] + " | " + split[1],
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
		assertTrue(timeTool.toString(),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
		
		timeTool = new TimeTool();
		timeTool.addDays(64);
		duration = timeTool.getDurationToNowString();
		assertNotNull(timeTool.toString(), duration);
		assertTrue(containsDigit(duration));
		split = splitByFormat(timeTool.getMonthsFormat(2));
		assertTrue(split.length == 2);
		assertNotNull(timeTool.toString(), split[0]);
		assertNotNull(timeTool.toString(), split[1]);
		assertTrue(timeTool.toString(),
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
		assertTrue(timeTool.toString(),
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
		assertTrue(timeTool.toString(),
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
		assertTrue(timeTool.toString(),
			duration.startsWith(split[0]) && duration.endsWith(split[1]));
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
