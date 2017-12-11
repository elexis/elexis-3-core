package ch.elexis.core.scheduler;

import java.util.Set;

import org.quartz.Job;
import org.quartz.Trigger;

import ch.elexis.core.jdt.NonNull;

public abstract class AbstractElexisSchedulerJob {
	
	/**
	 * 
	 * @return the {@link Job} to be executed.
	 * @see <a
	 *      href="http://quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-02">http://quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-02</a>
	 */
	public abstract @NonNull
	Job getJob();
	
	/**
	 * 
	 * @return a {@link Set} of {@link Trigger} objects that determine when the {@link Job} is
	 *         executed
	 */
	public abstract Set<Trigger> getJobTriggers();
}
