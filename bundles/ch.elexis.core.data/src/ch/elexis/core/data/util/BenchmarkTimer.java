package ch.elexis.core.data.util;

/**
 * This class can be used for simple benchmarks.
 */
import ch.rgw.tools.StringTool;
public class BenchmarkTimer {
	
	private long startTime = 0;
	private long endTime = 0;
	
	public void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	public void end(){
		this.endTime = System.currentTimeMillis();
	}
	
	public long getStartTime(){
		return this.startTime;
	}
	
	public long getEndTime(){
		return this.endTime;
	}
	
	public long getTotalTime(){
		return this.endTime - this.startTime;
	}
	
	public String geTotalTimeHumanReadableString(){
		return convertMS(getTotalTime());
	}
	
	private String convertMS(long ms){
		int seconds = (int) ((ms / 1000) % 60);
		int minutes = (int) (((ms / 1000) / 60) % 60);
		int hours = (int) ((((ms / 1000) / 60) / 60) % 24);
		
		String sec, min, hrs;
		if (seconds < 10)
			sec = "0" + seconds;
		else
			sec = StringTool.leer + seconds;
		if (minutes < 10)
			min = "0" + minutes;
		else
			min = StringTool.leer + minutes;
		if (hours < 10)
			hrs = "0" + hours;
		else
			hrs = StringTool.leer + hours;
		
		if (hours == 0)
			return min + " min, " + sec + " sec";
		else
			return hrs + " hrs, " + min + "min, " + sec + " sec";
	}
	
}
