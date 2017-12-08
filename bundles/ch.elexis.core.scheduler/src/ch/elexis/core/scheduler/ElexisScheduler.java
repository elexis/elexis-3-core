package ch.elexis.core.scheduler;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.scheduler.internal.Activator;
import ch.elexis.core.scheduler.internal.ElexisSchedulerExtensionPoint;

public class ElexisScheduler {
	
	private static ElexisScheduler instance;
	private static Logger log = LoggerFactory.getLogger(ElexisScheduler.class);
	
	private Scheduler scheduler;
	private SchedulerListener schedulerListener;
	private Properties properties;
	
	private ElexisScheduler(){
		try {
			properties = Activator.getQuartzProperties();
			scheduler = new StdSchedulerFactory(properties).getScheduler();
			//			schedulerListener = new ElexisSchedulerListener();
		} catch (SchedulerException e) {
			log.error("Error initializing class", e);
		}
	};
	
	public static ElexisScheduler getInstance(){
		if (instance == null) {
			instance = new ElexisScheduler();
		}
		return instance;
	}
	
	public void startScheduler(){
		try {
			//			scheduler.getListenerManager().addSchedulerListener(schedulerListener);
			
			ElexisSchedulerExtensionPoint.initialize(scheduler);
			
			scheduler.start();
			
		} catch (SchedulerException e) {
			log.error("Error starting scheduler", e);
		}
	}
	
	public void shutdownScheduler(){
		try {
			scheduler.shutdown();
			//			scheduler.getListenerManager().removeSchedulerListener(schedulerListener);
		} catch (SchedulerException e) {
			log.error("Error on scheduler shutdown", e);
		}
	}
	
}
