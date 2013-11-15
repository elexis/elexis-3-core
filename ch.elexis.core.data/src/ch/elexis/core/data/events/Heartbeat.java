/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.data.events;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.Log;

/**
 * Heartbeat is an event source, that fires events at user-definable intervals to all
 * HeartListeners. All actions that must be repeated regularly should be registered as
 * HeartListener. They will all be called at about the specified rate, but not in a guaranteed
 * particular order and not necessarily at exactly identical intervals.
 * 
 * Heartbeat löst das Pinger-Konzept ab. Der Heartbeat ist ein Singleton, das alle
 * CoreHub.localCfg.get(heartbeatrate,30) Sekunden einen Event feuert. Wer reglmässige Aktionen
 * durchführen will, kann sich als HeartbeatListener registrieren. Dieses Konzept hat gegenüber
 * individuellen update-Threads den Vorteil, dass die Netzwerk- und Datenbankbelastung, sowie die
 * Zahl der gleichzeitig laufenden Threads limitiert wird. Der Heartbeat sorgt dafür, dass die
 * listener der Reihe nach (aber ncht in einer definierten Reihenfolge) aufgerufen werden.
 * 
 * The client registering a listener can define the frequency, whether the listener should be called
 * at every single heart beat or at a lower frequency.
 * 
 * @author gerry
 * 
 */
public class Heartbeat {
	/**
	 * Registering a listener using FREQUENCY_HIGH, it is called at every single heartbeat.
	 */
	public static final int FREQUENCY_HIGH = 1;
	/**
	 * Registering a listener using FREQUENCY_MEDIUM, it is called at every 4th heartbeat.
	 */
	public static final int FREQUENCY_MEDIUM = 2;
	/**
	 * Registering a listener using FREQUENCY_LOW, it is called at every 16th heartbeat.
	 */
	public static final int FREQUENCY_LOW = 3;
	
	private beat theBeat;
	private Timer pacer;
	private boolean isSuspended;
	private static Heartbeat theHeartbeat;
	private CopyOnWriteArrayList<HeartListener> highFrequencyListeners;
	private CopyOnWriteArrayList<HeartListener> mediumFrequencyListeners;
	private CopyOnWriteArrayList<HeartListener> lowFrequencyListeners;
	private static Log log = Log.get("Heartbeat"); //$NON-NLS-1$
	
	private Heartbeat(){
		theBeat = new beat();
		highFrequencyListeners = new CopyOnWriteArrayList<HeartListener>();
		mediumFrequencyListeners = new CopyOnWriteArrayList<HeartListener>();
		lowFrequencyListeners = new CopyOnWriteArrayList<HeartListener>();
		pacer = new Timer(true);
		int interval = CoreHub.localCfg.get(Preferences.ABL_HEARTRATE, 30); //$NON-NLS-1$
		isSuspended = true;
		pacer.schedule(theBeat, 0, interval * 1000L);
	}
	
	/**
	 * Das Singleton holen
	 * 
	 * @return den Heartbeat der Anwendung
	 */
	public static Heartbeat getInstance(){
		if (theHeartbeat == null) {
			theHeartbeat = new Heartbeat();
		}
		return theHeartbeat;
	}
	
	/**
	 * Heartbeat (wieder) laufen lassen.
	 * 
	 * @param immediately
	 *            true: Sofort einen ersten beat losschicken, false: im normalen Rhythmus bleiben.
	 */
	public void resume(boolean immediately){
		isSuspended = false;
		log.log("resume", Log.DEBUGMSG); //$NON-NLS-1$
		if (immediately) {
			theBeat.run();
		}
	}
	
	/**
	 * Heartbeat aussetzen (geht im Hintergrund weiter, wird aber nicht mehr weitergeleitet)
	 */
	public void suspend(){
		log.log("suspending", Log.DEBUGMSG); //$NON-NLS-1$
		isSuspended = true;
	}
	
	/**
	 * Heartbeat stoppen (kann dann nicht mehr gestartet werden)
	 */
	public void stop(){
		log.log("stopping", Log.DEBUGMSG); //$NON-NLS-1$
		pacer.cancel();
	}
	
	/**
	 * Einen Listener registrieren. Achtung: Muss unbedingt mit removeListener deregistriert werden
	 * Calls addListener(listen, FREQUENCY_HIGH)
	 * 
	 * @param listen
	 *            der Listener
	 */
	public void addListener(HeartListener listen){
		addListener(listen, FREQUENCY_HIGH);
	}
	
	/**
	 * Add listener using the specified frequency. Must be de-regsitered again using removeListener
	 * 
	 * @param listener
	 * @param frequency
	 *            the frequency to call this listener. One of FREQUENCY_HIGH, FREQUENCY_MEDIUM,
	 *            FREQUENCY_LOW
	 */
	public void addListener(HeartListener listen, int frequency){
		if (!highFrequencyListeners.contains(listen) && !mediumFrequencyListeners.contains(listen)
			&& !lowFrequencyListeners.contains(listen)) {
			
			switch (frequency) {
			case FREQUENCY_HIGH:
				highFrequencyListeners.add(listen);
				break;
			case FREQUENCY_MEDIUM:
				mediumFrequencyListeners.add(listen);
				break;
			case FREQUENCY_LOW:
				lowFrequencyListeners.add(listen);
				break;
			}
		}
	}
	
	/**
	 * Einen Listener wieder austragen
	 * 
	 * @param listen
	 */
	public void removeListener(HeartListener listen){
		// remove the listener from the three lists
		// actually, it's contained in only one of them, but we don't know which
		// one
		highFrequencyListeners.remove(listen);
		mediumFrequencyListeners.remove(listen);
		lowFrequencyListeners.remove(listen);
	}
	
	/**
	 * we beat asynchronously, because most listeners will update their views
	 * 
	 * @author Gerry
	 * 
	 */
	private class beat extends TimerTask {
		private static final int FREQUENCY_HIGH_MULTIPLIER = 1;
		private static final int FREQUENCY_MEDIUM_MULTIPLIER = 4;
		private static final int FREQUENCY_LOW_MULTIPLIER = 16;
		
		// multiplier for resetting counter after each round
		private static final int RESET_MULTIPLIER = FREQUENCY_LOW_MULTIPLIER;
		
		private int counter = 0;
		
		@Override
		public void run(){
			if (!isSuspended) {
				// low frequency
				if (counter % FREQUENCY_LOW_MULTIPLIER == 0) {
					log.log("Heartbeat low", Log.DEBUGMSG); //$NON-NLS-1$
					for (HeartListener l : lowFrequencyListeners) {
						l.heartbeat();
					}
				}
				// medium frequency
				if (counter % FREQUENCY_MEDIUM_MULTIPLIER == 0) {
					log.log("Heartbeat medium", Log.DEBUGMSG); //$NON-NLS-1$
					for (HeartListener l : mediumFrequencyListeners) {
						l.heartbeat();
					}
				}
				// high frequency
				if (counter % FREQUENCY_HIGH_MULTIPLIER == 0) {
					log.log("Heartbeat high", Log.DEBUGMSG); //$NON-NLS-1$
					for (HeartListener l : highFrequencyListeners) {
						l.heartbeat();
					}
				}
			}
			counter++;
			counter %= RESET_MULTIPLIER;
		}
		
	}
	
	public interface HeartListener {
		/**
		 * Die Methode heartbeat wird in "einigermassen" regelmässigen (aber nicht garantiert immer
		 * genau identischen) Abständen aufgerufen
		 * 
		 */
		public void heartbeat();
	}
}
