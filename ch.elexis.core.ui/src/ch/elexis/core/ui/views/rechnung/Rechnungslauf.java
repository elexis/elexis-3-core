/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.views.rechnung;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.commands.Handler;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

/**
 * Aktion für das "Zauberstab"-Icon in der KonsZumVerrechnen View -> Dialog mit verschiedenen
 * Kriterien zur Konsultationsauswahl und Rechnungslauf anhand dieser Auswahl
 * 
 * @author gerry
 * 
 */
public class Rechnungslauf implements IRunnableWithProgress {
	private static Logger log = LoggerFactory.getLogger(Rechnungslauf.class);
	
	private KonsZumVerrechnenView kzv;
	private Mandant mandant;
	
	private List<Konsultation> kons;
	private List<Konsultation> subResults;
	private List<Fall> skipCase;
	private TimeTool tmpTime, now, ttFirstBefore, ttLastBefore, quarterLimit, ttFrom, ttTo;
	private boolean quarterFilter, billFlagged, skip;
	private Money lowerLimit;
	private String accountSys;
	
	public Rechnungslauf(KonsZumVerrechnenView kzv, boolean billFlagged, TimeTool ttFirstBefore,
		TimeTool ttLastBefore, Money lowerLimit, boolean quarterFilter, boolean skip,
		TimeTool ttFrom, TimeTool ttTo, String accountSys){
		this.ttFirstBefore = ttFirstBefore;
		this.ttLastBefore = ttLastBefore;
		this.ttFrom = ttFrom;
		this.ttTo = ttTo;
		this.lowerLimit = lowerLimit;
		this.quarterFilter = quarterFilter;
		this.skip = skip;
		this.accountSys = accountSys;
		
		this.billFlagged = billFlagged;
		this.kzv = kzv;
		
		now = new TimeTool();
		calcQuarterLimit();
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
		InterruptedException{
		mandant = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
		
		List<Konsultation> dbList = getAllKonsultationen(monitor);
		kons = skipInvalidConsultations(dbList);
		subResults = new ArrayList<Konsultation>();
		skipCase = new ArrayList<Fall>();
		tmpTime = new TimeTool();
		
		applyBillingFlagFilter(monitor);
		applyAccountSystemFilter(monitor);
		applyStartedFilter(monitor);
		applyFinishedFilter(monitor);
		applyMinPaymentLimit(monitor);
		applyQuarterFilter(monitor);
		applyTimespanFilter(monitor);
		
		// make sure any kons that could be from a skip case is removed
		for (Fall f : skipCase) {
			for (Konsultation k : f.getBehandlungen(false)) {
				kons.remove(k);
			}
		}
		
		monitor.subTask(Messages.Rechnungslauf_creatingLists); //$NON-NLS-1$
		for (Konsultation k : kons) {
			kzv.selectKonsultation(k);
			monitor.worked(1);
		}
		
		if (skip) {
			monitor.subTask(Messages.Rechnungslauf_creatingBills); //$NON-NLS-1$
			Handler.executeWithProgress(kzv.getViewSite(), "bill.create", kzv.tSelection, monitor); //$NON-NLS-1$
		}
		monitor.done();
	}
	
	/**
	 * skip invalid consultations
	 * 
	 * @param dbList
	 * @return a list of relevant (valid)konsultations
	 */
	private List<Konsultation> skipInvalidConsultations(List<Konsultation> dbList){
		// get Rechnungssteller of current Mandant
		String rsId = mandant.getRechnungssteller().getId();
		List<Konsultation> list = new ArrayList<Konsultation>();
		
		for (Konsultation k : dbList) {
			// skip if no valid mandant is set
			if (k.getMandant() == null) {
				ElexisStatus status =
					new ElexisStatus(ElexisStatus.WARNING, "ch.elexis",
						ElexisStatus.CODE_NOFEEDBACK, Messages.Rechnungslauf_warnInvalidMandant,
						ElexisStatus.LOG_ERRORS);
				StatusManager.getManager().handle(status);
				log.warn("...skip Kons [" + k.getId() + "] with invalid mandant");
				continue;
			}
			
			// skip if fall is not set or inexisting
			Fall fall = k.getFall();
			if ((fall == null) || (!fall.exists())) {
				log.warn("...skip Kons [" + k.getId() + "] fall is null/inexisting");
				continue;
			}
			
			Patient pat = fall.getPatient();
			if ((pat == null) || (!pat.exists())) {
				log.warn("...skip Kons [" + k.getId() + "] patient is null/inexisting");
				continue;
			}
			
			if (rsId.equals(k.getMandant().getRechnungssteller().getId())) {
				list.add(k);
			} else {
				log.debug("... skip Kons [" + k.getId() + "] as rechnungssteller is divergent");
			}
		}
		return list;
	}
	
	/**
	 * get all kons. that are not billed yet
	 * 
	 * @param monitor
	 * @return list of all not yet billed konsultationen
	 */
	private List<Konsultation> getAllKonsultationen(IProgressMonitor monitor){
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.FLD_BILL_ID, StringConstants.EMPTY, null);
		monitor.beginTask(Messages.Rechnungslauf_analyzingConsultations, IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		monitor.subTask(Messages.Rechnungslauf_readingConsultations); //$NON-NLS-1$
		
		return qbe.execute();
	}
	
	/**
	 * calculate the past quarter
	 */
	private void calcQuarterLimit(){
		String today = now.toString(TimeTool.DATE_COMPACT).substring(4);
		quarterLimit = new TimeTool();
		
		if (today.compareTo("0930") > 0) {
			quarterLimit.set(TimeTool.MONTH, 9);
		} else if (today.compareTo("0630") > 0) {
			quarterLimit.set(TimeTool.MONTH, 6);
		} else if (today.compareTo("0331") > 0) {
			quarterLimit.set(TimeTool.MONTH, 3);
		} else {
			quarterLimit.set(TimeTool.MONTH, 1);
		}
	}
	
	/**
	 * removes all kons. that are not flagged for billing
	 * 
	 * @param monitor
	 */
	private void applyBillingFlagFilter(IProgressMonitor monitor){
		if (billFlagged) {
			log.debug("filter all that are flagged for billing");
			monitor.subTask("Filtern zum Abrechnen vorgemerkter Fälle ...");
			for (Konsultation k : kons) {
				if (accepted(k)) {
					Fall fall = k.getFall();
					tmpTime = fall.getBillingDate();
					
					if ((tmpTime != null) && tmpTime.isBeforeOrEqual(now)) {
						for (Konsultation k2 : kons) {
							String fid = k2.get(Konsultation.FLD_CASE_ID);
							if ((fid != null) && (fid.equals(fall.getId()))) {
								if (!subResults.contains(k2)) {
									subResults.add(k2);
								}
							}
						}
					}
				}
			}
			updateKonsList();
			if (tmpTime == null) {
				tmpTime = new TimeTool();
			}
		}
	}
	
	/**
	 * only keeps the ones that match the selected account system
	 * 
	 * @param monitor
	 */
	private void applyAccountSystemFilter(IProgressMonitor monitor){
		if (accountSys != null) {
			log.debug("apply filter for accounting system: " + accountSys);
			monitor.subTask("Filtern nach Abrechnungssystem ...");
			for (Konsultation k : kons) {
				if (accepted(k)) {
					Fall fall = k.getFall();
					
					if (fall != null && fall.getAbrechnungsSystem().equals(accountSys)) {
						subResults.add(k);
					}
				}
			}
			updateKonsList();
		}
	}
	
	/**
	 * all series which started before a specific date
	 * 
	 * @param monitor
	 */
	private void applyStartedFilter(IProgressMonitor monitor){
		if (ttFirstBefore != null) {
			log.debug("apply start time [" + ttFirstBefore.toString(TimeTool.DATE_COMPACT)
				+ "] filter");
			monitor.subTask("Filtern nach Anfangsdatum ...");
			List<Fall> treated = new ArrayList<Fall>();
			
			for (Konsultation k : kons) {
				if (accepted(k)) {
					tmpTime.set(k.getDatum());
					Fall kCase = k.getFall();
					if (tmpTime.isBefore(ttFirstBefore)) {
						if (kCase != null && !(treated.contains(kCase))
							&& !(skipCase.contains(kCase))) {
							treated.add(kCase);
							
							Konsultation[] caseKons = kCase.getBehandlungen(false);
							for (Konsultation cK : caseKons) {
								if (kons.contains(cK)) {
									if (!subResults.contains(cK)) {
										subResults.add(cK);
									}
								}
							}
						}
					} else {
						skipCase.add(kCase);
					}
				}
			}
			updateKonsList();
		}
	}
	
	/**
	 * all series which finished after a specific date
	 * 
	 * @param monitor
	 */
	private void applyFinishedFilter(IProgressMonitor monitor){
		if (ttLastBefore != null) {
			log.debug("apply finish time [" + ttLastBefore.toString(TimeTool.DATE_COMPACT)
				+ "] filter");
			monitor.subTask("Filtern Enddatum ...");
			for (Konsultation k : kons) {
				if (accepted(k)) {
					tmpTime.set(k.getDatum());
					if (tmpTime.isBefore(ttLastBefore)) {
						for (Konsultation k2 : kons) {
							String fId = k.get(Konsultation.FLD_CASE_ID);
							if ((fId != null) && (fId.equals(k2.getFall().getId()))) {
								tmpTime.set(k2.getDatum());
								if (tmpTime.isAfter(ttLastBefore)) {
									skipCase.add(k.getFall());
									break;
								} else {
									if (!subResults.contains(k2)) {
										subResults.add(k2);
									}
								}
							}
						}
					}
				}
			}
			updateKonsList();
		}
	}
	
	/**
	 * all series between the given timespan
	 * 
	 * @param monitor
	 */
	private void applyTimespanFilter(IProgressMonitor monitor){
		if (ttFrom != null && ttTo != null) {
			log.debug("apply filter for timestpan [" + ttFrom.toString(TimeTool.DATE_COMPACT)
				+ " - " + ttTo.toString(TimeTool.DATE_COMPACT));
			monitor.subTask("Filtern nach Zeitspanne ...");
			for (Konsultation k : kons) {
				if (accepted(k)) {
					tmpTime.set(k.getDatum());
					if (tmpTime.isAfterOrEqual(ttFrom) && tmpTime.isBeforeOrEqual(ttTo)) {
						subResults.add(k);
					}
				}
			}
			updateKonsList();
		}
		
	}
	
	/**
	 * minimal payment amount filter
	 * 
	 * @param monitor
	 */
	private void applyMinPaymentLimit(IProgressMonitor monitor){
		if (lowerLimit != null) {
			log.debug("apply filter for minimal payment amount");
			monitor.subTask("Filtern nach Betragshöhe ...");
			for (Konsultation k : kons) {
				if (accepted(k)) {
					Money sum = new Money();
					String konsFallId = k.get(Konsultation.FLD_CASE_ID);
					List<Konsultation> matchingKons = new ArrayList<Konsultation>();
					
					for (Konsultation k2 : kons) {
						String fallId = k2.get(Konsultation.FLD_CASE_ID);
						
						if ((fallId != null) && (fallId.equals(konsFallId))) {
							matchingKons.add(k2);
							List<Verrechnet> leistungen = k2.getLeistungen();
							for (Verrechnet vr : leistungen) {
								sum.addMoney(vr.getNettoPreis().multiply(vr.getZahl()));
							}
						}
					}
					
					if (sum.isMoreThan(lowerLimit)) {
						for (Konsultation match : matchingKons) {
							if (!subResults.contains(match)) {
								subResults.add(match);
							}
						}
					} else {
						if (!skipCase.contains(k.getFall())) {
							skipCase.add(k.getFall());
						}
					}
				}
			}
			updateKonsList();
		}
	}
	
	/**
	 * applies the filter for the past quarter
	 * 
	 * @param monitor
	 */
	private void applyQuarterFilter(IProgressMonitor monitor){
		if (quarterFilter) {
			log.debug("applying quarter filter");
			monitor.subTask("Filtern nach Quartal ...");
			for (Konsultation k : kons) {
				if (accepted(k)) {
					tmpTime.set(k.getDatum());
					if (tmpTime.isBefore(quarterLimit)) {
						subResults.add(k);
					}
				}
			}
			updateKonsList();
		}
	}
	
	/**
	 * check if it isn't already in the list or on the skip list
	 * 
	 * @param k
	 * @return
	 */
	private boolean accepted(Konsultation k){
		if (subResults.contains(k) || skipCase.contains(k.getFall())) {
			return false;
		}
		return true;
	}
	
	private void updateKonsList(){
		kons.clear();
		kons.addAll(subResults);
		subResults.clear();
	}
}
