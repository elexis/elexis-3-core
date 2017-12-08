package ch.elexis.core.scheduler.sample;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashSet;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.scheduler.AbstractElexisSchedulerJob;

/**
 * Sample Job to demonstrate the scheduler; in order to activate this job, it would have to be added
 * to the extension point
 * 
 * 
 * @author Marco Descher <descher@medevit.at>
 * 
 */
public class SampleSchedulerJob extends AbstractElexisSchedulerJob implements Job {
	
	@Override
	public @NonNull Job getJob(){
		return this;
	}
	
	@Override
	public Set<Trigger> getJobTriggers(){
		Set<Trigger> ret = new HashSet<>();
		
		// Cron Trigger that fires every minute
		Trigger t =
			newTrigger().withIdentity(this.getClass().getName())
				.withSchedule(cronSchedule("0 0/1 * * * ?")).build();
		
		ret.add(t);
		
		return ret;
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		System.out.println("This method is called every time the scheduler fires.");
	}
	
}
