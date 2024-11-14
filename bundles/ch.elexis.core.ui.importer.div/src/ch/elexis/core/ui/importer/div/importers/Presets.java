/*******************************************************************************
 * Copyright (c) 2007-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    Niklaus Giger - Made universal importer really import patients
 *
 *******************************************************************************/

package ch.elexis.core.ui.importer.div.importers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.importer.div.importers.ExcelWrapper;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.KontaktMatcher.CreateMode;
import ch.elexis.data.Anschrift;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Xid;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Some statically defined import methods (all from Excel-files)
 *
 * @author gerry
 *
 */
public class Presets {
	// we'll use these local XID's to reference the external data
	private final static String IMPORT_XID = "elexis.ch/importPresets"; //$NON-NLS-1$
	public final static String KONTAKTID = IMPORT_XID + "/KID"; //$NON-NLS-1$
	private static Logger log = LoggerFactory.getLogger(Presets.class);
	public static final String INSURANCE = Messages.Core_Costbearer;
	public static final String INSURANCE_NUMBER = Messages.Core_Insurance_Number;

	/*
	 * 0 A ID 1 B IstPerson Natürliche Person oder Organisation 2 C IstPatient 3 D
	 * Titel 4 E Bezeichnung1 5 F Bezeichnung2 6 G Zusatz 7 H Geburtsdatum 8 I
	 * Geschlecht m oder M → männlich, sonst weiblich 9 J E-Mail 10 K Website 11 L
	 * Telefon 1 12 M Telefon 2 13 N Mobil 14 O Strasse 15 P Plz 16 Q Ort 17 R
	 * Postadresse 18 S EAN
	 */
	public static final boolean importUniversal(final ExcelWrapper exw, final boolean bKeepID,
			final IProgressMonitor moni) {
		exw.setFieldTypes(new Class[] { Integer.class, Integer.class, Integer.class, String.class, // ID, IstPerson,
																									// IstPatient,
																									// Titel
				String.class, String.class, String.class, // Bezeichnung1, Bezeichnung2, Zusatz
				TimeTool.class, String.class, String.class, // Geburtsdatum, Geschlecht, E-Mail
				String.class, String.class, String.class, // Website, Telefon 1, Telefon 2
				String.class, String.class, String.class, // Mobil, Strasse, Plz
				String.class, String.class, Integer.class
				// Ort, Postanschrift,
		});
		int first = exw.getFirstRow();
		int last = exw.getLastRow();
		moni.beginTask(Messages.Presets_ImportingContacts, last - first);
		int counter = 0;
		for (int i = exw.getFirstRow() + 1; i <= exw.getLastRow(); i++) {
			String[] row = exw.getRow(i).toArray(new String[0]);
			if (row == null) {
				continue;
			}
			/*
			 * if(row.length<18){ continue; }
			 */
			// Please keep in sync with doc/import.textile !!
			String ID = StringTool.getSafe(row, 0);
			String EAN = StringTool.getSafe(row, 18);
			if (StringTool.isNothing(ID)) {
				ID = EAN;
			}

			String typ = StringTool.getSafe(row, 1);
			String ispat = StringTool.getSafe(row, 2);
			String titel = StringTool.getSafe(row, 3);
			String bez1 = StringTool.getSafe(row, 4);
			String bez2 = StringTool.getSafe(row, 5);
			String zusatz = StringTool.getSafe(row, 6);
			String strasse = StringTool.getSafe(row, 14);
			String plz = StringTool.getSafe(row, 15);
			String ort = StringTool.getSafe(row, 16);
			String natel = StringTool.getSafe(row, 13);
			Kontakt k = null;
			if (StringTool.isNothing(typ) || typ.equals("0")) { //$NON-NLS-1$
				k = KontaktMatcher.findOrganisation(bez1, null, strasse, plz, ort, CreateMode.CREATE);
				if (k == null) {
					continue;
				}
				k.set("Zusatz1", bez2); //$NON-NLS-1$
				k.set("Bezeichnung3", zusatz); //$NON-NLS-1$
			} else {
				String sex = StringTool.getSafe(row, 8);
				String gebdat = StringTool.getSafe(row, 7);
				if (ispat.equalsIgnoreCase("1")) { //$NON-NLS-1$
					Patient pat = (Patient) Xid.findObject(KONTAKTID, ID);
					// avoid duplicate import
					if (pat == null) {
						pat = new Patient(bez1, bez2, gebdat,
								sex.toLowerCase().startsWith("m") ? Person.MALE : Person.FEMALE); //$NON-NLS-1$
						pat.set("PatientNr", ID); //$NON-NLS-1$
						pat.addXid(KONTAKTID, ID, false);
					}
				}
				k = KontaktMatcher.findPerson(bez1, bez2, gebdat, sex, strasse, plz, ort, natel, CreateMode.CREATE);
				if (k == null) {
					continue;
				}
				k.set("Titel", titel); //$NON-NLS-1$
				k.set("Zusatz", zusatz); //$NON-NLS-1$
			}
			moni.subTask(k.getLabel());
			k.set(new String[] { "E-Mail", "Website", "Telefon1", "Telefon2", "Natel", "Strasse", "Plz", "Ort", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
					"Anschrift" }, //$NON-NLS-1$
					StringTool.getSafe(row, 9), StringTool.getSafe(row, 10), StringTool.getSafe(row, 11),
					StringTool.getSafe(row, 12), natel, strasse, plz, ort, StringTool.getSafe(row, 17));
			if (EAN.matches("[0-9]{13,13}")) { //$NON-NLS-1$
				k.addXid(XidConstants.DOMAIN_EAN, EAN, true);
			}
			moni.worked(1);
			if (moni.isCanceled()) {
				return false;
			}
			if (counter++ > 200) {
				PersistentObject.clearCache();
				System.gc();
				try {
					Thread.sleep(100);
				} catch (Exception ex) {
					// no worries
				}
				counter = 0;
			}
		}
		moni.done();
		return true;
	}

	public static boolean importHertel(final ExcelWrapper exw, final boolean bKeepID, final IProgressMonitor moni) {
		exw.setFieldTypes(new Class[] { Integer.class, String.class, String.class, String.class, // Nr, Name, Vorname,
																									// Sex
				String.class, String.class, String.class, TimeTool.class, // Anrede, Zivilstand, Titel,
																			// Geburtsdatum
				String.class, String.class, String.class, String.class, // Arzt, Strasse, Tel Platz 1,
																		// Tel. Platz 2
				String.class, String.class, String.class, String.class, // Tel 3, Tel 4, E-Mail, Zusatz
				String.class, String.class, TimeTool.class, String.class, // Pat-select, Briefanrede,
																			// Letzter Kontakt, Freies
																			// Feld 1
				String.class, String.class, String.class, String.class, // Freies Feld 2, Freies Feld 3,
																		// Reiter, Ort
				String.class, String.class, String.class, String.class, // Plz, Land, UnfallNr, KK-Nr
				String.class, String.class, String.class, String.class, // IV-Nr, Zusatz-Nr, AHV-Nr,
																		// Covercard-Nr
				String.class, TimeTool.class, String.class, String.class, // Covercard-Zusatz, Erfasst
																			// am, Arzt, Unfallvers
				String.class, String.class
				// Krankenkasse, IV
		});
		int first = exw.getFirstRow();
		int last = exw.getLastRow();
		moni.beginTask("Import Patientendaten Hertel", last - first); //$NON-NLS-1$
		for (int i = first + 1; i < last; i++) {
			// Please keep this list in sync with doc/import.textile !!
			String[] row = exw.getRow(i).toArray(new String[0]);

			String name = StringTool.getSafe(row, 1);
			String vorname = StringTool.getSafe(row, 2);
			String sex = StringTool.getSafe(row, 3);

			String anrede = StringTool.getSafe(row, 4);
			String zivilstand = StringTool.getSafe(row, 5);
			String titel = StringTool.getSafe(row, 6);
			String gebdat = StringTool.getSafe(row, 7);

			String arzt = StringTool.getSafe(row, 8);
			String strasse = StringTool.getSafe(row, 9);
			String telp1 = StringTool.getSafe(row, 10);
			String telp2 = StringTool.getSafe(row, 11);

			String tel3 = StringTool.getSafe(row, 12);
			String tel4 = StringTool.getSafe(row, 13);
			String email = StringTool.getSafe(row, 14);
			String zusatz = StringTool.getSafe(row, 15);

			String patsel = StringTool.getSafe(row, 16);
			String briefanr = StringTool.getSafe(row, 17);
			String letzerk = StringTool.getSafe(row, 18);
			String frei1 = StringTool.getSafe(row, 19);

			String frei2 = StringTool.getSafe(row, 20);
			String frei3 = StringTool.getSafe(row, 21);
			String reiter = StringTool.getSafe(row, 22);
			String ort = StringTool.getSafe(row, 23);

			String plz = StringTool.getSafe(row, 24);
			String land = StringTool.getSafe(row, 25);
			String unfallnr = StringTool.getSafe(row, 26);
			String kknr = StringTool.getSafe(row, 27);

			String ivnr = StringTool.getSafe(row, 28);
			String zusatznr = StringTool.getSafe(row, 29);
			String ahvnr = StringTool.getSafe(row, 30);
			String covercardnr = StringTool.getSafe(row, 31);

			String covercardzus = StringTool.getSafe(row, 32);
			String erfasstam = StringTool.getSafe(row, 33);
			String arztn = StringTool.getSafe(row, 34);
			String unfallvers = StringTool.getSafe(row, 35);

			String kk = StringTool.getSafe(row, 36);
			String iv = StringTool.getSafe(row, 37);
			Patient pat = (Patient) Xid.findObject(KONTAKTID, row[0]); // avoid
			// duplicate
			// import
			if (pat == null) {
				pat = new Patient(name, vorname, gebdat,
						sex.toLowerCase().startsWith("m") ? Person.MALE : Person.FEMALE); //$NON-NLS-1$
				pat.set("PatientNr", row[0]); //$NON-NLS-1$
				pat.addXid(KONTAKTID, row[0], false);
			}
			moni.subTask(pat.getLabel());
			pat.set(new String[] { "Strasse", "Plz", "Ort", "Land", "Telefon1", "Telefon2", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					"Natel", "E-Mail", "Titel", "Gruppe", "Zusatz" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					strasse, plz, ort, land.equalsIgnoreCase(Messages.Presets_Switzerland) ? "CH" : StringUtils.EMPTY, //$NON-NLS-1$
					telp1, telp2, tel3, email, titel, arztn, zusatz);
			if (!StringTool.isNothing(ahvnr)) {
				pat.addXid(XidConstants.DOMAIN_AHV, ahvnr, true);
			}
			if (!StringTool.isNothing(kk)) {
				Query<Kontakt> qbe = new Query<>(Kontakt.class);
				qbe.add("Bezeichnung1", "=", kk); //$NON-NLS-1$ //$NON-NLS-2$
				List<Kontakt> res = qbe.execute();
				Kontakt k = null;
				if (!res.isEmpty()) {
					k = res.get(0);
				} else {
					k = new Organisation(kk, Messages.Core_KK_Short);
					k.set("Kuerzel", Messages.Core_KK_Short); //$NON-NLS-1$
				}
				Fall fall = pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(),
						Messages.Case_KVG_Short);
				fall.setGarant(pat);
				fall.setRequiredContact(INSURANCE, k);
				fall.setRequiredString(INSURANCE_NUMBER, kknr);
			}
			if (!StringTool.isNothing(unfallvers)) {
				Query<Kontakt> qbe = new Query<>(Kontakt.class);
				qbe.add("Bezeichnung1", "=", unfallvers); //$NON-NLS-1$ //$NON-NLS-2$
				List<Kontakt> res = qbe.execute();
				Kontakt k = null;
				if (!res.isEmpty()) {
					k = res.get(0);
				} else {
					k = new Organisation(unfallvers, Messages.Case_UVG_Short);
					k.set("Kuerzel", Messages.Case_UVG_Short); //$NON-NLS-1$
				}
				Fall fall = pat.neuerFall(Messages.Core_Accident, Messages.Core_Accident,
						Messages.Case_UVG_Short);
				fall.setGarant(k);
				fall.setRequiredContact(INSURANCE, k);
				fall.setRequiredString(Messages.Core_Accidentnumber, unfallnr);
			}
			moni.worked(1);
		}
		moni.done();
		return true;
	}

	public static boolean importRussi(final ExcelWrapper exw, final boolean bKeepID, final IProgressMonitor moni) {
		exw.setFieldTypes(new Class[] { Integer.class, String.class, TimeTool.class, String.class, Integer.class,
				String.class, String.class, String.class, String.class, String.class, String.class, String.class,
				String.class });
		int first = exw.getFirstRow();
		int last = exw.getLastRow();
		moni.beginTask("Import Patientendaten Russi", last - first); //$NON-NLS-1$
		for (int i = first + 1; i < last; i++) {
			String[] row = exw.getRow(i).toArray(new String[0]);
			if (Xid.findObject(KONTAKTID, row[0]) != null) { // avoid duplicate
				// import
				continue;
			}
			// Please keep this list in sync with doc/import.textile !!
			String[] name = StringTool.getSafe(row, 1).split("\\s", 2); //$NON-NLS-1$
			String gdraw = StringTool.getSafe(row, 2);
			String gebdat = new TimeTool(gdraw).toString(TimeTool.DATE_GER);
			String gender = StringTool.getSafe(row, 9).startsWith("W") ? "w" : "m"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Patient pat = new Patient(name[0], name.length > 1 ? name[1] : "-", gebdat, gender); //$NON-NLS-1$
			String patcode = new StringBuilder().append(pat.getLabel()).append(pat.getPatCode()).toString();
			moni.subTask(patcode);
			log.info(patcode);
			pat.addXid(KONTAKTID, row[0], false);
			Anschrift an = pat.getAnschrift();
			an.setStrasse(StringTool.getSafe(row, 3));
			an.setPlz(StringTool.getSafe(row, 4));
			an.setOrt(StringTool.getSafe(row, 5));
			pat.setAnschrift(an);
			pat.set("Telefon1", StringTool.getSafe(row, 6)); //$NON-NLS-1$
			pat.set("Natel", StringTool.getSafe(row, 7)); //$NON-NLS-1$
			pat.set("Telefon2", StringTool.getSafe(row, 8)); //$NON-NLS-1$
			if (!StringTool.isNothing(StringTool.getSafe(row, 10))) {
				Organisation org = KontaktMatcher.findOrganisation(row[10], null, StringUtils.EMPTY, StringUtils.EMPTY,
						StringUtils.EMPTY, CreateMode.CREATE); // $NON-NLS-1$ //$NON-NLS-2$
				Fall fall = pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(),
						Messages.Case_KVG_Short);
				fall.setRequiredContact(Messages.Core_Costbearer, org);
				fall.setGarant(pat);
			}
			if (!StringTool.isNothing(StringTool.getSafe(row, 11))) {
				Organisation org = KontaktMatcher.findOrganisation(row[11], null, StringUtils.EMPTY, StringUtils.EMPTY,
						StringUtils.EMPTY, CreateMode.CREATE); // $NON-NLS-1$ //$NON-NLS-2$
				Fall fall = pat.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(),
						Messages.Case_UVG_Short);
				fall.setRequiredContact(Messages.Core_Costbearer, org);
				fall.setGarant(org);
			}
			moni.worked(1);
		}
		moni.done();
		return true;
	}

}
