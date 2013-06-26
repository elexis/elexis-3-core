/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that does not execute immediately on launch but waits if there comes another call - e.g a
 * key press of the user. The time the job waits is configurable but can also be adaptive - it
 * remembers the time between earlier calls and decides accordingly, how long it should wait next
 * time. The use if this class is to prevent lengthy operations to run unnecessarily - only the last
 * of a series of calls will be executed
 * 
 * @author gerry
 * 
 */
public class DelayableJob extends Job {
	private final IWorker worker;
	public static final int DELAY_ADAPTIVE = -1;
	private long lastCall = 0L;
	private int lastDelay = 500;
	private final HashMap<String, Object> privdata = new HashMap<String, Object>();
	private static final boolean bDebug = false;
	
	public DelayableJob(String name, IWorker worker){
		super(name);
		this.worker = worker;
	}
	
	/**
	 * Launch the job after a specified delay. If a re-launch occurs within the delay time, the
	 * counter is reset and will wait for the specified time again. If the Job was already launched
	 * when a re-launch occurs, the Job will be stopped if possible.
	 * 
	 * If the delay is DELAY_ADAPTIVE, the Job will try to find the optimal delay by analyzing
	 * earlier calls. So different typing speeds of different users can be handled.
	 * 
	 * @param delayMillis
	 */
	public void launch(int delayMillis){
		
		boolean canCancel = this.cancel();
		if (bDebug) {
			StringBuilder sb = new StringBuilder();
			sb.append("DelayableJob: ").append("delayMillis: ").append(delayMillis).append("\n")
				.append("lastDelay: ").append(lastDelay).append("\n").append("jobstate: ")
				.append(getState()).append("\n").append("lastcall: ").append(lastCall).append("\n")
				.append("canCancel " + canCancel);
			System.out.println(sb.toString());
		}
		
		if (delayMillis == DELAY_ADAPTIVE) {
			if (lastCall == 0) {
				delayMillis = lastDelay; // this is the first call; we start
				// with predefined value
			} else {
				int delay = (int) (System.currentTimeMillis() - lastCall);
				System.out.println("actual delay: " + delay);
				if ((delay > 20) && (delay < 1500)) { // we do not consider
					// delays <20 or >
					// 1500ms
					
					int diff = delay - lastDelay;
					if (bDebug) {
						System.out.println("diff: " + diff);
					}
					lastDelay = Math.max(150, lastDelay + (diff / 3));
				}
				delayMillis = lastDelay;
			}
			lastCall = System.currentTimeMillis();
			if (bDebug) {
				System.out.println("new Delay: " + delayMillis);
			}
		}
		this.schedule(delayMillis);
		if (bDebug) {
			System.out.println("schedule " + delayMillis);
			System.out.println("---------");
		}
	}
	
	/**
	 * set arbitrary data that can be retrieved at run time
	 * 
	 * @param key
	 *            a unique key
	 * @param value
	 *            an arbitrary object
	 */
	public void setRuntimeData(String key, Object value){
		privdata.put(key, value);
	}
	
	/**
	 * retrieve a formerly set data object
	 * 
	 * @param key
	 *            the unique key
	 * @return the object associated with this key. This can be null if no such object exists, or if
	 *         null was associated with this key
	 */
	public Object getRuntimeData(String key){
		return privdata.get(key);
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor){
		return worker.work(monitor, privdata);
	}
	
	public interface IWorker {
		public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params);
	}
}
