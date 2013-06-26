/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.data;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Eine Verordnung. Also ein Artikel zusmamen mit einer Einnahmevorschrift,
 * verkn√ºpft mit einem Patienten.
 */
public class Prescription extends PersistentObject {

	public static final String TERMS = "terms";
	public static final String DATE_UNTIL = "DatumBis";
	public static final String DATE_FROM = "DatumVon";
	public static final String COUNT = "Anzahl";
	public static final String REMARK = "Bemerkung";
	public static final String DOSAGE = "Dosis";
	public static final String REZEPT_ID = "RezeptID";
	private static final String ARTICLE_ID = "ArtikelID";
	public static final String ARTICLE = "Artikel";
	public static final String PATIENT_ID = "PatientID";
	private static final String TABLENAME = "PATIENT_ARTIKEL_JOINT";
	static {
		addMapping(TABLENAME, PATIENT_ID, ARTICLE, ARTICLE_ID, REZEPT_ID,
				"DatumVon=S:D:DateFrom", "DatumBis=S:D:DateUntil", DOSAGE,
				REMARK, COUNT, FLD_EXTINFO);
	}

	public Prescription(Artikel a, Patient p, String dosage, String remark) {
		create(null);
		String article = a.storeToString();
		set(new String[] { ARTICLE, PATIENT_ID, DOSAGE, REMARK, DATE_FROM },
				article, p.getId(), dosage, remark,
				new TimeTool().toString(TimeTool.DATE_GER));
	}

	public Prescription(Prescription other) {
		String[] fields = new String[] { ARTICLE, PATIENT_ID, DOSAGE, REMARK,
				ARTICLE_ID };
		String[] vals = new String[fields.length];
		if (other.get(fields, vals)) {
			create(null);
			set(fields, vals);
			addTerm(new TimeTool(), vals[2]);
		}
	}

	public static Prescription load(String id) {
		return new Prescription(id);
	}

	protected Prescription() {
	}

	protected Prescription(String id) {
		super(id);
	}

	/**
	 * Set the begin date of this prescription
	 * 
	 * @param date
	 *            may be null to set it as today
	 */
	public void setBeginDate(String date) {
		set(DATE_FROM,
				date == null ? new TimeTool().toString(TimeTool.DATE_GER)
						: date);
	}

	public String getBeginDate() {
		return checkNull(get(DATE_FROM));
	}

	public void setEndDate(String date) {
		set(DATE_UNTIL,
				date == null ? new TimeTool().toString(TimeTool.DATE_GER)
						: date);
	}

	public String getEndDate() {
		return checkNull(get(DATE_UNTIL));
	}

	@Override
	public String getLabel() {
		return getSimpleLabel() + " " + getDosis();
	}

	public String getSimpleLabel() {
		Artikel art = getArtikel();
		if (art != null) {
			return getArtikel().getLabel();
		} else {
			return "Fehler";
		}
	}

	/**
	 * return the article contained in this prescription. In earlier versions of
	 * elexis, this was the Article ID, now it is a String representation of the
	 * Article itself (which allows for reconstruction of the subclass used).
	 * For compatibility reasons we use the old technique for old prescriptions.
	 * 
	 * @return
	 */
	public Artikel getArtikel() {
		// compatibility layer
		String art = get(ARTICLE);
		if (StringTool.isNothing(art)) {
			return Artikel.load(get(ARTICLE_ID));
		}
		return (Artikel) CoreHub.poFactory.createFromString(art);

	}

	public String getDosis() {
		return checkNull(get(DOSAGE));
	}

	public void setDosis(String newDose) {
		String oldDose = getDosis();
		if (!oldDose.equals(newDose)) {
			addTerm(new TimeTool(), newDose);
		}
	}

	public String getBemerkung() {
		return checkNull(get(REMARK));

	}

	public void setBemerkung(String value) {
		set(REMARK, checkNull(value));
	}

	/**
	 * Ein Medikament stoppen
	 */
	@Override
	public boolean delete() {
		if (CoreHub.acl.request(AccessControlDefaults.MEDICATION_MODIFY)) {
			TimeTool today = new TimeTool();
			today.addHours(-24);
			addTerm(today, StringConstants.ZERO);
			return true;
		}
		return false;
	}

	/**
	 * Ein Medikament aus der Datenbank l√∂schen
	 * 
	 * @return
	 */
	public boolean remove() {
		if (CoreHub.acl.request(AccessControlDefaults.DELETE_MEDICATION)) {
			return super.delete();
		}
		return false;
	}

	/**
	 * Insert a new dosage term, defined by a beginning date and a dose We store
	 * the old dose and its beginning date in the field "terms".
	 * 
	 * @param dose
	 *            a dosage definition of the form "1-0-0-0" or "0" to stop the
	 *            article
	 */
	@SuppressWarnings("unchecked")
	public void addTerm(TimeTool begin, String dose) {
		Map extInfo = getMap(FLD_EXTINFO);
		String raw = (String) extInfo.get(TERMS);
		if (raw == null) {
			raw = "";
		}
		String lastBegin = get(DATE_FROM);
		String lastDose = get(DOSAGE);
		StringBuilder line = new StringBuilder();
		line.append(StringTool.flattenSeparator).append(lastBegin).append("::")
				.append(lastDose);
		raw += line.toString();
		extInfo.put(TERMS, raw);
		setMap(FLD_EXTINFO, extInfo);
		set(DATE_FROM, begin.toString(TimeTool.DATE_GER));
		set(DOSAGE, dose);
		if (dose.equals("0")) {
			set(DATE_UNTIL, begin.toString(TimeTool.DATE_GER));
		}
	}

	/**
	 * A listing of all adinistration periods of this prescription. This is to
	 * retrieve later when and how the article was prescribed
	 * 
	 * @return a Map of TimeTools and Doses (Sorted by date)
	 */
	public SortedMap<TimeTool, String> getTerms() {
		TreeMap<TimeTool, String> ret = new TreeMap<TimeTool, String>();
		Map extInfo = getMap(FLD_EXTINFO);
		String raw = (String) extInfo.get(TERMS);
		if (raw != null) {
			String[] terms = raw.split(StringTool.flattenSeparator);
			for (String term : terms) {
				if (term.length() > 0) {
					String[] flds = term.split("::");
					if (flds != null && flds.length > 0) {
						TimeTool date = new TimeTool(flds[0]);
						String dose = "n/a";
						if (flds.length > 1) {
							dose = flds[1];
						}
						ret.put(date, dose);
					}
				}
			}
		}
		ret.put(new TimeTool(get(DATE_FROM)), get(DOSAGE));
		return ret;
	}

	public static float calculateTagesDosis(String dosis)
			throws NumberFormatException {
		float num = 0f;
		if (dosis != null) {
			if (dosis.matches("[0-9]+[xX][0-9]+(/[0-9]+)?")) { //$NON-NLS-1$
				String[] dose = dosis.split("[xX]"); //$NON-NLS-1$
				int count = Integer.parseInt(dose[0]);
				num = getNum(dose[1]) * count;
			} else if (dosis.indexOf('-') != -1) {
				String[] dos = dosis.split("-"); //$NON-NLS-1$
				if (dos.length > 2) {
					for (String d : dos) {
						num += getNum(d);
					}
				} else {
					num = getNum(dos[1]);
				}
			} else {
				return 0f;
			}
		} else {
			return 0f;
		}
		return num;
	}

	private static float getNum(String num) {
		try {
			String n = num.trim();
			if (n.equalsIgnoreCase("½"))
				return 0.5F;
			if (n.equalsIgnoreCase("¼"))
				return 0.25F;
			if (n.equalsIgnoreCase("1½"))
				return 1.5F;

			if (n.indexOf('/') != -1) {
				String[] bruch = n.split(StringConstants.SLASH);
				float zaehler = Float.parseFloat(bruch[0]);
				float nenner = Float.parseFloat(bruch[1]);
				return zaehler / nenner;
			} else {
				return Float.parseFloat(n);
			}
		} catch (NumberFormatException e) {
			ElexisStatus status = new ElexisStatus(ElexisStatus.INFO,
					CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					e.getLocalizedMessage(), e);
			ElexisEventDispatcher.getInstance().fireElexisStatusEvent(status);
			return 0.0F;
		}
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	@Override
	public boolean isDragOK() {
		return true;
	}
}
