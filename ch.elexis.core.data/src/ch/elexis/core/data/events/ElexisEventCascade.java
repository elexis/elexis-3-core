/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Patient;

/**
 * This cascade makes sure, that the three central elements are always selected synchroneously:
 * Patient, case and consultation.
 * 
 * @author gerry
 * 
 */
public class ElexisEventCascade {
	private static ElexisEventCascade theInstance;
	private final static Lock cascadeLock = new ReentrantLock(true);
	
	private static ElexisEventCascade getInstance(){
		if (theInstance == null) {
			theInstance = new ElexisEventCascade();
		}
		return theInstance;
	}
	
	private final ElexisEventListenerImpl eeli_pat = new ElexisEventListenerImpl(Patient.class,
		ElexisEvent.EVENT_DESELECTED | ElexisEvent.EVENT_SELECTED) {
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			if (cascadeLock.tryLock()) {
				try {
					ElexisEventDispatcher.getInstance().waitUntilEventQueueIsEmpty(100);
					if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
						Patient pat = (Patient) ev.getObject();
						if (pat != null) {
							Konsultation k = pat.getLetzteKons(false);
							if (k == null) {
								ElexisEventDispatcher.getInstance().fire(
									new ElexisEvent(null, Konsultation.class,
										ElexisEvent.EVENT_DESELECTED));
								ElexisEventDispatcher.getInstance()
									.fire(
										new ElexisEvent(null, Fall.class,
											ElexisEvent.EVENT_DESELECTED));
							} else {
								ElexisEventDispatcher.fireSelectionEvents(k, k.getFall());
							}
						}
					} else if (ev.getType() == ElexisEvent.EVENT_DESELECTED) {
						ElexisEventDispatcher.getInstance().fire(
							new ElexisEvent(null, Fall.class, ElexisEvent.EVENT_DESELECTED));
						ElexisEventDispatcher.getInstance()
							.fire(
								new ElexisEvent(null, Konsultation.class,
									ElexisEvent.EVENT_DESELECTED));
					}
				} finally {
					cascadeLock.unlock();
				}
			}
		}
		
	};
	private final ElexisEventListenerImpl eeli_fall = new ElexisEventListenerImpl(Fall.class,
		ElexisEvent.EVENT_SELECTED) {
		public void catchElexisEvent(ElexisEvent ev){
			if (cascadeLock.tryLock()) {
				try {
					Fall fall = (Fall) ev.getObject();
					if (fall != null) {
						Patient pat = fall.getPatient();
						if (pat != null) {
							ElexisEventDispatcher.fireSelectionEvent(pat);
							Konsultation[] k = fall.getBehandlungen(true);
							if (k != null && k.length > 0) {
								ElexisEventDispatcher.fireSelectionEvents(k[0]);
							}
						}
					}
				} finally {
					cascadeLock.unlock();
				}
			}
		}
	};
	
	private final ElexisEventListenerImpl eeli_kons = new ElexisEventListenerImpl(
		Konsultation.class, ElexisEvent.EVENT_SELECTED) {
		public void catchElexisEvent(ElexisEvent ev){
			if (cascadeLock.tryLock()) {
				try {
					Konsultation k = (Konsultation) ev.getObject();
					Fall fall = k.getFall();
					if (fall != null) {
						Patient pat = fall.getPatient();
						ElexisEventDispatcher.fireSelectionEvents(pat, fall);
					}
				} finally {
					cascadeLock.unlock();
				}
			}
		}
	};
	
	private void start(){
		ElexisEventDispatcher.getInstance().addListeners(eeli_fall, eeli_kons, eeli_pat);
	}
	
	private void stop(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_fall, eeli_kons, eeli_pat);
	}
	
	private ElexisEventCascade(){}
}
