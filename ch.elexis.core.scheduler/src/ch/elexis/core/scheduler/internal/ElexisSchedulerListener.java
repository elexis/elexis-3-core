package ch.elexis.core.scheduler.internal;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

public class ElexisSchedulerListener implements SchedulerListener {
	
	//	private static Logger log = LoggerFactory.getLogger(ElexisSchedulerListener.class);
	
	@Override
	public void jobAdded(JobDetail arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobDeleted(JobKey arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobPaused(JobKey arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobResumed(JobKey arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobScheduled(Trigger arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobUnscheduled(TriggerKey arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobsPaused(String arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void jobsResumed(String arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void schedulerError(String arg0, SchedulerException arg1){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void schedulerInStandbyMode(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void schedulerShutdown(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void schedulerShuttingdown(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void schedulerStarted(){
		// TODO
	}
	
	@Override
	public void schedulerStarting(){
		// TODO
	}
	
	@Override
	public void schedulingDataCleared(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void triggerFinalized(Trigger arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void triggerPaused(TriggerKey arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void triggerResumed(TriggerKey arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void triggersPaused(String arg0){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void triggersResumed(String arg0){
		// TODO Auto-generated method stub
		
	}
	
}
