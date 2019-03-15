package ch.elexis.core.tasks.internal.service;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

public class LogProgressMonitor implements IProgressMonitor {
	
	private Logger logger;
	
	private String name;
	private int totalWork;
	private int worked;
	private boolean cancelled;
	
	public LogProgressMonitor(Logger logger){
		this.logger = logger;
		worked = 0;
		cancelled = false;
	}
	
	@Override
	public void beginTask(String name, int totalWork){
		this.name = name;
		this.totalWork = (totalWork == UNKNOWN) ? 9999 : totalWork;
		
		log();
	}
	
	private void log(){
		if (logger.isDebugEnabled()) {
			String msg = name + " [" + worked + "/" + totalWork + "]";
			if (cancelled) {
				msg = "-CNCLD- " + msg;
			}
			logger.debug(msg);
		}
		
	}
	
	@Override
	public void done(){
		this.worked = this.totalWork;
		log();
	}
	
	@Override
	public void internalWorked(double work){
		// nothing to do
	}
	
	@Override
	public boolean isCanceled(){
		return cancelled;
	}
	
	@Override
	public void setCanceled(boolean value){
		this.cancelled = value;
		if (value) {
			log();
		}
	}
	
	@Override
	public void setTaskName(String name){
		this.name = name;
	}
	
	@Override
	public void subTask(String name){
		this.name = this.name + "/" + name;
	}
	
	@Override
	public void worked(int work){
		worked += work;
		log();
	}
	
}
