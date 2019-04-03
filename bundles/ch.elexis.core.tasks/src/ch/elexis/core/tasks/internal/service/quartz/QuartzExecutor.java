package ch.elexis.core.tasks.internal.service.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

public class QuartzExecutor {
		
	private SchedulerFactory sf;
	private Scheduler sched;
	
	public QuartzExecutor(){
		sf = new StdSchedulerFactory();
	}
	
	public void incur(ITaskService taskService, ITaskDescriptor taskDescriptor) throws TaskException{
		
		// test if the runnable can be instantiated
		taskService.instantiateRunnableById(taskDescriptor.getIdentifiedRunnableId());
		
		String cron = taskDescriptor.getTriggerParameters().get("cron");
		CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cron);
		
		JobKey jobKey = new JobKey(taskDescriptor.getId());
		JobDataMap jobDataMap = new JobDataMap(taskDescriptor.getRunContext());
		jobDataMap.put("taskDescriptor", taskDescriptor);
		jobDataMap.put("taskService", taskService);
		
		JobDetail jobDetail = JobBuilder.newJob(TriggerTaskJob.class).withIdentity(jobKey).build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(taskDescriptor.getId())
			.withSchedule(cronSchedule).usingJobData(jobDataMap).build();
		
		try {
			sched.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void release(ITaskDescriptor taskDescriptor) throws TaskException{
		JobKey jobKey = new JobKey(taskDescriptor.getId());
		try {
			boolean exists = sched.checkExists(jobKey);
			if (exists) {
				sched.deleteJob(jobKey);
			} else {
				// TODO log warn
			}
		} catch (SchedulerException e) {
			throw new TaskException(TaskException.TRIGGER_REGISTER_ERROR, e);
		}
	}
	
	public void shutdown() throws SchedulerException{
		if (sched != null) {
			sched.shutdown();
		}
		
	}
	
	public void start() throws SchedulerException{
		sched = sf.getScheduler();
		sched.start();
	}
	
}
