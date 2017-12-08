package ch.elexis.core.scheduler.internal;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.RegistryFactory;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.scheduler.AbstractElexisSchedulerJob;

public class ElexisSchedulerExtensionPoint {
	
	private static Logger log = LoggerFactory.getLogger(ElexisSchedulerExtensionPoint.class);
	private static final String SCHEDULED_JOB_EXTENSION_POINT =
		"ch.elexis.core.scheduler.scheduledJob";
	private static final String CLASS_PROPERTY = "class";
	
	private static HashMap<String, AbstractElexisSchedulerJob> schedulerJobClasses =
		new HashMap<>();
	
	public static void initialize(Scheduler scheduler){
		try {
			IExtensionPoint refDataExtensionPoint =
				RegistryFactory.getRegistry().getExtensionPoint(SCHEDULED_JOB_EXTENSION_POINT);
			IConfigurationElement[] extensionPoints =
				refDataExtensionPoint.getConfigurationElements();
			
			for (IConfigurationElement ePoint : extensionPoints) {
				Object o = ePoint.createExecutableExtension(CLASS_PROPERTY);
				
				if (o instanceof AbstractElexisSchedulerJob) {
					AbstractElexisSchedulerJob aesj = (AbstractElexisSchedulerJob) o;
					log.debug("Found AbstractElexisSchedulerJob for " + aesj.getJob());
					addJob(aesj, scheduler);
				}
			}
		} catch (CoreException e) {
			log.error("Exception occured trying to load AstractElexisScheduler extension points", e);
		}
		
	}
	
	private static void addJob(@NonNull AbstractElexisSchedulerJob aesj, Scheduler scheduler){
		if (aesj.getJob() == null || aesj.getJobTriggers() == null
			|| scheduler == null) {
			log.error("Invalid state in class " + aesj.getClass(), new IllegalArgumentException(
				"A required value is null"));
			return;
		}
		
		// create job detail and add job
		Class<? extends Job> jobClass = aesj.getJob().getClass();
		log.debug("Adding job " + jobClass.getName());
		
		schedulerJobClasses.put(jobClass.getName(), aesj);
		
		JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobClass.getName()).build();
		Set<Trigger> jobTriggers = aesj.getJobTriggers();
		
		try {
			scheduler.scheduleJob(jobDetail, jobTriggers, true);
		} catch (SchedulerException e) {
			log.error("Error replacing or adding job " + jobClass.getName(), e);
		}
	}
	
	public static Class<?> getClassByName(String name){
		return schedulerJobClasses.get(name).getClass();
	}
	
}
