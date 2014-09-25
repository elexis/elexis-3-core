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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.commands.Handler;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
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
	
	String accountSys;
	TimeTool ttFirstBefore, ttLastBefore, ttHeute, limitQuartal, ttFrom, ttTo;
	Money mLimit;
	boolean bQuartal, bMarked, bSkip;
	Hashtable<Konsultation, Patient> hKons;
	KonsZumVerrechnenView kzv;
	
	public Rechnungslauf(KonsZumVerrechnenView kzv, boolean bMarked, TimeTool ttFirstBefore,
		TimeTool ttLastBefore, Money mLimit, boolean bQuartal, boolean bSkip, TimeTool ttFrom,
		TimeTool ttTo, String accountSys){
		this.ttFirstBefore = ttFirstBefore;
		this.ttLastBefore = ttLastBefore;
		this.ttFrom = ttFrom;
		this.ttTo = ttTo;
		this.mLimit = mLimit;
		this.bQuartal = bQuartal;
		this.bSkip = bSkip;
		this.accountSys = accountSys;
		hKons = new Hashtable<Konsultation, Patient>(1000);
		ttHeute = new TimeTool();
		limitQuartal = new TimeTool();
		String heute = ttHeute.toString(TimeTool.DATE_COMPACT).substring(4);
		if (heute.compareTo("0930") > 0) { //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH, 9); // 1.10.
		} else if (heute.compareTo("0630") > 0) { //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH, 6);
		} else if (heute.compareTo("0331") > 0) { //$NON-NLS-1$
			limitQuartal.set(TimeTool.MONTH, 3);
		} else {
			limitQuartal.set(TimeTool.MONTH, 1);
		}
		this.bMarked = bMarked;
		this.kzv = kzv;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
		InterruptedException{
		String kMandantID = CoreHub.actMandant.getId();
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.FLD_BILL_ID, StringConstants.EMPTY, null);
		monitor.beginTask(Messages.Rechnungslauf_analyzingConsultations, IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		monitor.subTask(Messages.Rechnungslauf_readingConsultations); //$NON-NLS-1$
		List<Konsultation> dblist = qbe.execute();
		// filter the list of Konsultationen based on the Rechnungssteller of the current Mandant
		String rsId = CoreHub.actMandant.getRechnungssteller().getId();
		ArrayList<Konsultation> list = new ArrayList<Konsultation>();
		for (Konsultation kons : dblist) {
			// skip kons if it has no valid mandant
			if (kons.getMandant() == null) {
				ElexisStatus status =
					new ElexisStatus(ElexisStatus.WARNING, "ch.elexis",
						ElexisStatus.CODE_NOFEEDBACK, Messages.Rechnungslauf_warnInvalidMandant,
						ElexisStatus.LOG_ERRORS);
				StatusManager.getManager().handle(status);
				continue;
			}
			
			if (kons.getMandant().getRechnungssteller().getId().equals(rsId)) {
				if (accountSys != null) {
					if (kons.getFall() != null
						&& kons.getFall().getAbrechnungsSystem().equals(accountSys)) {
						list.add(kons);
					}
				} else {
					list.add(kons);
				}
			}
		}
		
		ArrayList<Konsultation> listbasic = new ArrayList<Konsultation>(list);
		HashMap<Fall, Object> hSkipCase = new HashMap<Fall, Object>();
		TimeTool now = new TimeTool();
		TimeTool cmp = new TimeTool();
		Iterator<Konsultation> it = listbasic.iterator();
		while (it.hasNext()) {
			Konsultation k = it.next();
			monitor.worked(1);
			if (hKons.get(k) != null) {
				continue;
			}
			Fall kFall = k.getFall();
			if ((kFall == null) || (!kFall.exists())) {
				continue;
			}
			if (accountSys != null) {
				if (!kFall.getAbrechnungsSystem().equals(accountSys)) {
					continue;
				}
			}
			if (hSkipCase.get(kFall) != null) {
				continue;
			}
			String kfID = kFall.getId();
			Patient kPatient = kFall.getPatient();
			
			if ((kPatient == null) || (!kPatient.exists())) {
				continue;
			}
			
			if (bMarked) { // Alle zur Verrechnung markierten Fälle abrechnen
				TimeTool bd = kFall.getBillingDate();
				if ((bd != null) && (bd.isBeforeOrEqual(now))) {
					Iterator<Konsultation> i2 = list.iterator();
					while (i2.hasNext()) {
						Konsultation k2 = i2.next();
						String fid = k2.get(Konsultation.FLD_CASE_ID);
						if ((fid != null) && (fid.equals(kfID))) {
							hKons.put(k2, kPatient);
							i2.remove();
						}
					}
				}
			}
			if (ttFrom != null && ttTo != null) { // alle serien zwischen xy datum und yz datum
				cmp.set(k.getDatum());
				if (cmp.isAfterOrEqual(ttFrom) && cmp.isBeforeOrEqual(ttTo)) {
					hKons.put(k, kPatient);
				}
			}
			
			if (ttFirstBefore != null) { // Alle Serien mit Beginn vor einem
				// bestimmten Datum
				cmp.set(k.getDatum());
				if (cmp.isBefore(ttFirstBefore)) {
					Iterator<Konsultation> i2 = list.iterator();
					while (i2.hasNext()) {
						Konsultation k2 = i2.next();
						String fid = k2.get(Konsultation.FLD_CASE_ID);
						if ((fid != null) && (fid.equals(kfID))) {
							hKons.put(k2, kPatient);
							i2.remove();
						}
					}
				}
			}
			
			if (ttLastBefore != null) { // Alle Serien mit letzter Kons vor einem bestimmten Datum
				cmp.set(k.getDatum());
				if (cmp.isBefore(ttLastBefore)) {
					Iterator<Konsultation> i2 = list.iterator();
					while (i2.hasNext()) {
						Konsultation k2 = i2.next();
						String fid = k2.get(Konsultation.FLD_CASE_ID);
						if ((fid != null) && (fid.equals(kfID))) {
							cmp.set(k2.getDatum());
							if (cmp.isAfter(ttLastBefore)) {
								hSkipCase.put(kFall, "1"); //$NON-NLS-1$
								i2.remove();
								break;
							} else {
								hKons.put(k2, kPatient);
								i2.remove();
							}
						}
					}
				}
			}
			if (mLimit != null) {
				Money sum = new Money();
				Map<Konsultation, Patient> list2 = new HashMap<Konsultation, Patient>(100);
				for (Konsultation k2 : list) {
					String fid = k2.get(Konsultation.FLD_CASE_ID);
					if ((fid != null) && (fid.equals(kfID))) {
						list2.put(k2, kPatient);
						List<Verrechnet> lstg = k2.getLeistungen();
						for (Verrechnet v : lstg) {
							sum.addMoney(v.getNettoPreis().multiply(v.getZahl()));
						}
					}
				}
				if (sum.isMoreThan(mLimit)) {
					hKons.putAll(list2);
				}
			}
			
			if (bQuartal) {
				cmp.set(k.getDatum());
				if (cmp.isBefore(limitQuartal)) {
					hKons.put(k, kPatient);
				}
			}
		}
		if (ttLastBefore != null) {
			for (Fall fall : hSkipCase.keySet()) {
				for (Konsultation kd : fall.getBehandlungen(false)) {
					hKons.remove(kd);
				}
			}
		}
		
		monitor.subTask(Messages.Rechnungslauf_creatingLists); //$NON-NLS-1$
		for (Konsultation konsultation : hKons.keySet()) {
			System.out.println(konsultation.getFall().getAbrechnungsSystem());
		}
		Enumeration<Konsultation> en = hKons.keys();
		while (en.hasMoreElements()) {
			kzv.selectKonsultation(en.nextElement());
			monitor.worked(1);
		}
		if (bSkip) {
			monitor.subTask(Messages.Rechnungslauf_creatingBills); //$NON-NLS-1$
			Handler.executeWithProgress(kzv.getViewSite(), "bill.create", kzv.tSelection, monitor); //$NON-NLS-1$
		}
		monitor.done();
		
	}
}
