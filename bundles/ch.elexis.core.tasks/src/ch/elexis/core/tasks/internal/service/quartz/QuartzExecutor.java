package ch.elexis.core.tasks.internal.service.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

public class QuartzExecutor {
	
	private Logger logger;
	
	private SchedulerFactory sf;
	private Scheduler sched;
	
	private final SimpleDateFormat FULL_ISO;
	
	public QuartzExecutor(){
		logger = LoggerFactory.getLogger(getClass());
		FULL_ISO = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		sf = new StdSchedulerFactory();
		try {
			sched = sf.getScheduler();
		} catch (SchedulerException e) {
			logger.error("Error getting scheduler", e);
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	public void incur(ITaskService taskService, ITaskDescriptor taskDescriptor)
		throws TaskException{
		
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
			logger.warn("#incur - " + taskDescriptor.getId(), e);
		}
		
	}
	
	public void release(ITaskDescriptor taskDescriptor) throws TaskException{
		
		JobKey jobKey = new JobKey(taskDescriptor.getId());
		try {
			if (sched.isShutdown()) {
				return;
			}
			
			boolean exists = sched.checkExists(jobKey);
			if (exists) {
				sched.deleteJob(jobKey);
			} else {
				logger.info("#release - job does not exist [" + jobKey + "]");
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
		sched.start();
	}
	
	public Set<String[]> getIncurred(){
		Set<String[]> incurred = new HashSet<String[]>();
		try {
			Set<JobKey> jobKeys = sched.getJobKeys(GroupMatcher.anyGroup());
			for (JobKey jobKey : jobKeys) {
				String taskDescriptorId = jobKey.getName();
				if (sched.checkExists(jobKey)) {
					Trigger trigger = sched.getTrigger(TriggerKey.triggerKey(taskDescriptorId));
					Date nextFireTime = trigger.getNextFireTime();
					incurred.add(new String[] {
						taskDescriptorId, FULL_ISO.format(nextFireTime)
					});
				}
			}
		} catch (SchedulerException e) {
			logger.warn("#getIncurredTasks", e);
		}
		return incurred;
	}
	
}
