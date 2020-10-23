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

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.ISticker;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Artikel;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.NamedBlob;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.elexis.data.Script;
import ch.elexis.data.Sticker;
import ch.rgw.tools.ExHandler;

/**
 * Default implementation of IPatFilter. Will be called after all other filters returned DONT_HANDLE
 * 
 * @author Gerry
 */
public class PatFilterImpl implements IPatFilter {
	
	@Override
	public int accept(Patient p, PersistentObject o){
		if (o instanceof Kontakt) {
			Query<BezugsKontakt> qbe = new Query<BezugsKontakt>(BezugsKontakt.class);
			qbe.add(BezugsKontakt.MY_ID, Query.EQUALS, p.getId());
			qbe.add(BezugsKontakt.OTHER_ID, Query.EQUALS, o.getId());
			if (qbe.execute().size() > 0) {
				return ACCEPT;
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
			qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, p.getId());
			qbe.add(Prescription.FLD_ARTICLE, Query.EQUALS, o.storeToString());
			if (qbe.execute().size() > 0) {
				return ACCEPT;
			}
			return REJECT;
		} else if (o instanceof Prescription) {
			Artikel art = ((Prescription) o).getArtikel();
			Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
			qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, p.getId());
			qbe.add(Prescription.FLD_ARTICLE, Query.EQUALS, art.storeToString());
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
	
	@Override
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
	
	@Override
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
