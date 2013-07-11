/*******************************************************************************
 * Copyright (c) 2008-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.util.List;

import ch.elexis.core.data.Artikel;
import ch.elexis.core.data.BezugsKontakt;
import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.NamedBlob;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Prescription;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.Script;
import ch.elexis.core.data.Sticker;
import ch.elexis.core.data.Verrechnet;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.PatListFilterBox.IPatFilter;
import ch.rgw.tools.ExHandler;

/**
 * Default implementation of IPatFilter. Will be called after all other filters returned DONT_HANDLE
 * 
 * @author Gerry
 */
public class PatFilterImpl implements IPatFilter {
	
	public int accept(Patient p, PersistentObject o){
		if (o instanceof Kontakt) {
			Query<BezugsKontakt> qbe = new Query<BezugsKontakt>(BezugsKontakt.class);
			qbe.add(BezugsKontakt.MY_ID, Query.EQUALS, p.getId());
			qbe.add(BezugsKontakt.OTHER_ID, Query.EQUALS, o.getId());
			if (qbe.execute().size() > 0) {
				return ACCEPT;
			}
			return REJECT;
		} else if (o instanceof IVerrechenbar) {
			IVerrechenbar iv = (IVerrechenbar) o;
			Fall[] faelle = p.getFaelle();
			for (Fall fall : faelle) {
				Konsultation[] konsen = fall.getBehandlungen(false);
				for (Konsultation k : konsen) {
					List<Verrechnet> lv = k.getLeistungen();
					for (Verrechnet v : lv) {
						if (v.getVerrechenbar().equals(iv)) {
							return ACCEPT;
						}
					}
				}
			}
			return REJECT;
			
		} else if (o instanceof IDiagnose) {
			IDiagnose diag = (IDiagnose) o;
			Fall[] faelle = p.getFaelle();
			for (Fall fall : faelle) {
				Konsultation[] konsen = fall.getBehandlungen(false);
				for (Konsultation k : konsen) {
					List<IDiagnose> id = k.getDiagnosen();
					if (id.contains(diag)) {
						return ACCEPT;
					}
				}
			}
			return REJECT;
		} else if (o instanceof Artikel) {
			Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
			qbe.add(Prescription.PATIENT_ID, Query.EQUALS, p.getId());
			qbe.add(Prescription.ARTICLE, Query.EQUALS, o.storeToString());
			if (qbe.execute().size() > 0) {
				return ACCEPT;
			}
			return REJECT;
		} else if (o instanceof Prescription) {
			Artikel art = ((Prescription) o).getArtikel();
			Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
			qbe.add(Prescription.PATIENT_ID, Query.EQUALS, p.getId());
			qbe.add(Prescription.ARTICLE, Query.EQUALS, art.storeToString());
			if (qbe.execute().size() > 0) {
				return ACCEPT;
			}
			return REJECT;
		} else if (o instanceof Sticker) {
			List<ISticker> etis = p.getStickers();
			ISticker e = (ISticker) o;
			if (etis.contains(e)) {
				return ACCEPT;
			}
			return REJECT;
		} else if (o instanceof NamedBlob) {
			NamedBlob nb = (NamedBlob) o;
			String[] val = nb.getString().split("::");
			String test = p.get(val[0]);
			if (test == null) {
				return DONT_HANDLE;
			}
			String op = val[1];
			if (op.equals(Query.EQUALS)) {
				return test.equalsIgnoreCase(val[2]) ? ACCEPT : REJECT;
			} else if (op.equals("LIKE")) {
				return test.toLowerCase().contains(val[2].toLowerCase()) ? ACCEPT : REJECT;
			} else if (op.equals("Regexp")) {
				return test.matches(val[2]) ? ACCEPT : REJECT;
			}
		} else if (o instanceof Script) {
			Object ret;
			try {
				Script script = (Script) o;
				script.setVariable("patient", p);
				ret = script.execute(null, p);
				if (ret instanceof Integer) {
					return (Integer) ret;
				}
				
			} catch (Exception e) {
				return FILTER_FAULT;
			}
		}
		return DONT_HANDLE;
	}
	
	public boolean aboutToStart(PersistentObject filter){
		if (filter instanceof Script) {
			try {
				((Script) filter).init();
				return true;
			} catch (Exception e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler beim Initialisieren des Scripts", e.getMessage());
			}
		}
		return false;
		
	}
	
	public boolean finished(PersistentObject filter){
		if (filter instanceof Script) {
			try {
				((Script) filter).finished();
				return true;
			} catch (Exception e) {
				ExHandler.handle(e);
				SWTHelper.showError("Fehler beim Abschluss des Scripts", e.getMessage());
			}
		}
		return false;
	}
	
}
