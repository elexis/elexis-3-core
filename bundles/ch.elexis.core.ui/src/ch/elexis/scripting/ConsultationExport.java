/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.views.IPatFilter;
import ch.elexis.core.ui.views.PatFilterImpl;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Sticker;
import ch.rgw.tools.IFilter;

public class ConsultationExport {

	public String doExport(String dir, String stickerName) {
		try {
			Query<Patient> qbe = new Query<>(Patient.class);
			if (stickerName != null) {
				List<Sticker> ls = new Query<Sticker>(Sticker.class, Sticker.FLD_NAME, stickerName).execute();
				if (ls != null && !ls.isEmpty()) {
					final Sticker sticker = ls.get(0);

					final PatFilterImpl pf = new PatFilterImpl();
					IFilter flt = new IFilter() {
						@Override
						public boolean select(Object element) {
							return pf.accept((Patient) element, sticker) == IPatFilter.ACCEPT;
						}

					};
					qbe.addPostQueryFilter(flt);
				} else {
					return "Sticker " + stickerName + " nicht gefunden.";
				}
			}
			for (Patient pat : qbe.execute()) {
				Element e = new Element("Patient");
				e.setAttribute("ID", pat.getId()); //$NON-NLS-1$
				e.setAttribute("Name", pat.get(Patient.FLD_NAME)); //$NON-NLS-1$
				e.setAttribute("Vorname", pat.get(Patient.FLD_FIRSTNAME)); //$NON-NLS-1$
				e.setAttribute("GebDat", pat.get(Patient.FLD_DOB)); //$NON-NLS-1$
				for (Fall fall : pat.getFaelle()) {
					Element f = new Element("Fall"); //$NON-NLS-1$
					e.addContent(f);
					f.setAttribute("ID", fall.getId()); //$NON-NLS-1$
					f.setAttribute("Bezeichnung", fall.getBezeichnung()); //$NON-NLS-1$
					f.setAttribute("BeginnDatum", fall.getBeginnDatum()); //$NON-NLS-1$
					f.setAttribute("EndDatum", fall.getEndDatum()); //$NON-NLS-1$
					f.setAttribute("Gesetz", fall.getConfiguredBillingSystemLaw().name()); //$NON-NLS-1$
					f.setAttribute("Abrechnungssystem", fall.getAbrechnungsSystem()); //$NON-NLS-1$
					Kontakt k = fall.getGarant();
					if (k != null) {
						f.setAttribute("Garant", fall.getGarant().getLabel()); //$NON-NLS-1$
					}
					Kontakt costBearer = fall.getCostBearer();
					if (costBearer != null) {
						f.setAttribute("Kostentraeger", costBearer.getLabel()); //$NON-NLS-1$
						f.setAttribute("Versicherungsnummer", fall.getRequiredString("Versicherungsnummer")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					for (Konsultation kons : fall.getBehandlungen(false)) {
						Element kel = new Element("Konsultation"); //$NON-NLS-1$
						f.addContent(kel);
						kel.setAttribute("ID", kons.getId()); //$NON-NLS-1$
						kel.setAttribute("Datum", kons.getDatum()); //$NON-NLS-1$
						kel.setAttribute("Label", kons.getVerboseLabel()); //$NON-NLS-1$
						Samdas samdas = new Samdas(kons.getEintrag().getHead());
						kel.setText(samdas.getRecordText());
					}
				}
				Document doc = new Document();
				doc.setRootElement(e);
				FileOutputStream fout = new FileOutputStream(new File(dir, pat.getId() + ".xml")); //$NON-NLS-1$
				OutputStreamWriter cout = new OutputStreamWriter(fout, "UTF-8"); //$NON-NLS-1$
				XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
				xout.output(doc, cout);
				cout.close();
				fout.close();
			}
			return "ok";
		} catch (Exception ex) {
			return ex.getClass().getName() + ":" + ex.getMessage(); //$NON-NLS-1$
		}
	}
}
