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

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;

import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.rgw.tools.ExHandler;

/**
 * Ein Sammelbecken für Background-Jobs. Der JobPool steuert den Ablauf der Jobs: Er achtet darauf,
 * dass derselbe Job nicht mehrmals gleichzeitig gestartet wird, und dass nicht zuviele Jobs
 * parallel laufen. Dafür können Jobs in eine Warteschlange eingereiht werden, wo sie nacheinander
 * abgearbeitet werden. Die Priorität der Jobs wird aus org.eclipse.core.runtime.jobs.Job entnommen.
 * Die Werte bedeuten:
 * <ul>
 * <li>Job.INTERACTIVE: Höchste Priorität, nur für kurzlaufende Jobs, die ummittelbare Auswirkung
 * auf die Benutzeroberfläche haben.</li>
 * <li>Job.SHORT: Hohe Priorität, für Jobs die höchstens ein bis zwei Sekunden laufen, und auf die
 * der Anwender wartet</li>
 * <li>Job.LONG: Mittlere Priorität für Jobs, die mehrere Sekunden lang laufen, und die deshalb die
 * Benutzeroberfläche nicht beeinträchtigen sollen</li>
 * <li>Job.DECORATE; Niedrigste Priorität für allgemeine Hintergrundjobs, die nur laufen, wenn sonst
 * nichts zu tun ist</li>
 * </ul>
 * 
 * @see BackgroundJob
 * @author gerry
 * 
 * @deprecated Neuer Code sollte das Eclipse Job API verwenden
 * @see BackgroundJob
 */
@Deprecated
public class JobPool implements BackgroundJobListener {
	private Hashtable<String, BackgroundJob> pool = new Hashtable<String, BackgroundJob>();
	private Vector<String> running = new Vector<String>();
	private Vector<String> waiting = new Vector<String>();
	private Vector<String> queued = new Vector<String>();
	private ILock changeLock;
	private static JobPool thePool;
	
	private JobPool(){
		IJobManager jobman = Job.getJobManager();
		changeLock = jobman.newLock();
	}
	
	public void dispose(){
		for (BackgroundJob job : pool.values()) {
			try {
				if (job.cancel() == false) {
					job.getThread().interrupt();
				}
			} catch (Throwable t) {
				ExHandler.handle(t);
			}
		}
	}
	
	/**
	 * Den JobPool erzeugen bzw. holen. Es soll nur einen geben, deswegen als Singleton
	 * implementiert
	 */
	public static JobPool getJobPool(){
		if (thePool == null) {
			thePool = new JobPool();
		}
		return thePool;
	}
	
	/**
	 * Einen neuen Job hinzufügen. Ein Job bleibt im Pool bis zum Programmende. Er hat entweder den
	 * Status running, waiting oder queued. Jobs, die waiting oder queued sind, brauchen keine
	 * Systemressourcen. addJob lässt den Job zunächst im status waiting
	 * 
	 * @param job
	 *            der Job
	 * @return true wenn erfolgreich, false wenn dieser Job oder ein Job gleichen Namens schon im
	 *         Pool ist oder bei einem sonstigen Fehler.
	 */
	public boolean addJob(BackgroundJob job){
		try {
			changeLock.acquire();
			if (pool.get(job.getJobname()) != null) {
				return false;
			}
			job.addListener(this);
			pool.put(job.getJobname(), job);
			
			waiting.add(job.getJobname());
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		} finally {
			changeLock.release();
		}
	}
	
	/**
	 * Einen Job anhand seines Namens finden
	 * 
	 * @param name
	 *            Name des Jobs
	 * @return den Job oder null, wenn nicht vorhanden.
	 */
	public BackgroundJob getJob(String name){
		BackgroundJob ret = pool.get(name);
		return ret;
	}
	
	/**
	 * Einen Job starten
	 * 
	 * @param name
	 *            Name des Jobs
	 * @param priority
	 *            gewpnschte Priorität (Job.INTERACTIVE bis JOB.DECORATIONS)
	 * @return true wenn der Job gestartet wurde, d.h. er läuft dann noch bei Rückkehr dieser
	 *         Funktion. false, wenn der Job schon lief, oder wenn er nicht gefunden wurde.
	 */
	public boolean activate(String name, int priority){
		try {
			changeLock.acquire();
			if (waiting.remove(name) == true) {
				BackgroundJob job = pool.get(name);
				if (job == null) {
					return false;
				}
				running.add(name);
				job.setPriority(priority);
				job.schedule();
				return true;
			}
			return false;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		} finally {
			changeLock.release();
		}
	}
	
	/**
	 * Einen Job in die Warteschlange setzen. Er wird gestartet, sobald ein eventuell schon
	 * laufender Job beendet ist.
	 * 
	 * @param name
	 *            Name des Jobs
	 */
	public void queue(String name){
		try {
			changeLock.acquire();
			if (running.isEmpty()) {
				activate(name, Job.BUILD);
			} else {
				queued.add(name);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		} finally {
			changeLock.release();
		}
		
	}
	
	/**
	 * Diese Funktion ist für internen Gebrauch. Organisation der Warteschlange
	 */
	@Override
	public void jobFinished(BackgroundJob j){
		try {
			changeLock.acquire();
			running.remove(j.getJobname());
			waiting.add(j.getJobname());
			if (!queued.isEmpty()) {
				String nextJob = queued.remove(0);
				activate(nextJob, Job.BUILD);
			}
		} catch (Exception e) {
			ExHandler.handle(e);
		} finally {
			changeLock.release();
		}
	}
	
}
