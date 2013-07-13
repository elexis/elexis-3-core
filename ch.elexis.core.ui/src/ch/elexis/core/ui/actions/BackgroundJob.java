/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
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

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.Log;

/**
 * Die Basis des Hintergrunddienst-Systems von Elexis. Ein Hintegrunddienst muss von BackgroundJob
 * abgeleitet werden und bei Hub.jobPool angemeldet werden. Er kann direkt oder vom JobPool aus
 * gestartet werden und meldet seine Beendigung an alle interessierten BackgroundJobListener.
 * 
 * @see JobPool
 * @author Gerry
 * 
 * @deprecated Seit Eclipse 3.0 gibt es direkt im Job-API die Möglichkeit, einen IJobChangeListener
 *             einzusetzen, um Über die Beendigung eines Hintergrundprozesses informiert zu werden.
 *             Neuer Code sollte lieber diese Technik verwenden anstelle der BackgroundJobs.
 * 
 */
@Deprecated
public abstract class BackgroundJob extends Job {
	
	/***********************************************************************************************
	 * Wer bei Beendigung eines BackgroundJobs benachrichtigt werden will, muss einen
	 * BackgroundJobListener anmelden.
	 * 
	 * @author Gerry
	 * 
	 */
	public interface BackgroundJobListener {
		public void jobFinished(BackgroundJob j);
	}
	
	protected String jobname;
	protected static Log log = Log.get(Messages.BackgroundJob_0); //$NON-NLS-1$
	// private boolean running;
	private BackgroundJob self;
	protected LinkedList<BackgroundJobListener> listeners = new LinkedList<BackgroundJobListener>();
	private boolean valid;
	protected Object result;
	
	/**
	 * Jeder Job braucht einen eindeutigen Namen
	 */
	protected BackgroundJob(String name){
		super(name);
		jobname = name;
		// running=false;
		self = this;
		valid = false;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor){
		// running=true;
		log.log(Messages.BackgroundJob_1 + jobname, Log.INFOS); //$NON-NLS-1$
		IStatus ret = execute(monitor);
		if (Status.OK_STATUS.equals(ret)) {
			valid = true;
			fireFinished();
			return Status.OK_STATUS;
		}
		valid = false;
		return ret;
	}
	
	/** Hier müssen abgeleitete Klassen die eigentliche Arbeit erledigen */
	abstract public IStatus execute(IProgressMonitor monitor);
	
	public Object getData(){
		if (isValid()) {
			return result;
		}
		return null;
	}
	
	public String getJobname(){
		return jobname;
	}
	
	protected void fireFinished(){
		log.log(Messages.BackgroundJob_2 + jobname, Log.INFOS); //$NON-NLS-1$
		UiDesk.getDisplay().syncExec(new Runnable() {
			public void run(){
				LinkedList<BackgroundJobListener> lCopy =
					new LinkedList<BackgroundJobListener>(listeners);
				for (BackgroundJobListener l : lCopy) {
					l.jobFinished(self);
				}
			}
		});
		// running=false;
	}
	
	/**
	 * Einen Listener zufügen, der bei Beendigung des Jobs benachrichtigt wird. Die Benachrichtigung
	 * aller angemeldeten Listener erfolgt nacheinander, aber in keiner garantierten Reihenfolge.
	 */
	public void addListener(BackgroundJobListener l){
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	/**
	 * Einen Listener wieder entfernen. Dies ist z.B. notwendig, wenn ein Listener gelöscht wird, da
	 * sonst der Versuch, diesen Listener zu benachrichtigen, eine Exception werfen würde.
	 */
	public void removeListener(BackgroundJobListener l){
		listeners.remove(l);
	}
	
	/** Anfragen, ob dieser Job mindestens einmal korrekt beendet wurde */
	public boolean isValid(){
		return valid;
	}
	
	/**
	 * Diesen Job auf ungültig setzen. Er muss dann zuerts mindestens einmal erfolgreich beendet
	 * worden sein, bevor er wieder "valid" ist
	 * 
	 */
	public void invalidate(){
		valid = false;
	}
	
	/**
	 * Elementzahl dieses Jobs erfragen
	 */
	public abstract int getSize();
	
}
