/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.exchange;

import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINTSIZE;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_BIRTHDATE;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_FIRSTNAME;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_NAME;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_PATIENT;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_PLACE;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_SEX;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_STREET;
import static ch.elexis.core.ui.dialogs.KontaktSelektor.HINT_ZIP;

import java.util.List;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Class to match personal data to contacts
 * 
 * @author gerry
 */

public class KontaktMatcher {
	private static final String SEP = ", "; //$NON-NLS-1$
	
	public enum CreateMode {
		FAIL, CREATE, ASK
	};
	
	public static Kontakt findKontakt(final String name, final String strasse, final String plz,
		final String ort){
		Organisation o =
			findOrganisation(name, StringTool.leer, strasse, plz, ort, CreateMode.FAIL);
		if (o == null) {
			Person p =
				findPerson(name, StringTool.leer, StringTool.leer, StringTool.leer, strasse, plz,
					ort, StringTool.leer, CreateMode.FAIL);
			return p;
		} else {
			return o;
		}
	}
	
	/**
	 * Find the organization mathcing the given parameters
	 * 
	 * @param name
	 * @param strasse
	 * @param plz
	 * @param ort
	 * @param createIfNotExists
	 * @return the organization that matches best the given parameters or null if no such
	 *         organization was found
	 */
	public static Organisation findOrganisation(final String name, final String zusatz,
		final String strasse, final String plz, final String ort, final CreateMode createMode){
		String[] hints = new String[HINTSIZE];
		hints[HINT_NAME] = name;
		hints[HINT_STREET] = strasse;
		hints[HINT_ZIP] = plz;
		hints[HINT_PLACE] = ort;
		Query<Organisation> qbe = new Query<Organisation>(Organisation.class);
		
		if (!StringTool.isNothing(name)) {
			qbe.startGroup();
			qbe.add(Organisation.FLD_NAME1, "LIKE", name + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			String un = StringTool.unambiguify(name);
			if (!un.equalsIgnoreCase(name)) {
				qbe.or();
				qbe.add(Organisation.FLD_NAME1, "LIKE", un + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			}
			qbe.endGroup();
			qbe.and();
		}
		
		if (!StringTool.isNothing(zusatz)) {
			qbe.startGroup();
			qbe.add("Zusatz1", "LIKE", zusatz + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			String un = StringTool.unambiguify(zusatz);
			if (!un.equalsIgnoreCase(zusatz)) {
				qbe.or();
				qbe.add("Zusatz1", "LIKE", un + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			}
			qbe.endGroup();
		}
		
		List<Organisation> found = qbe.execute();
		if (found.size() == 0) {
			if (createMode == CreateMode.CREATE) {
				Organisation org = new Organisation(name, StringTool.unNull(zusatz));
				addAddress(org, strasse, plz, ort);
				return org;
			} else if (createMode == CreateMode.ASK) {
				return (Organisation) KontaktSelektor.showInSync(Organisation.class,
					Messages.KontaktMatcher_OrganizationNotFound, name + SEP + strasse + SEP + plz
						+ StringTool.space + ort, resolve1, hints);
			}
			return null;
		} else if (found.size() == 1) {
			return found.get(0);
		} else if (createMode == CreateMode.ASK) { // more than 1 hit
			return (Organisation) KontaktSelektor.showInSync(Organisation.class,
				Messages.KontaktMatcher_OrganizationNotUnique, name + SEP + strasse + SEP + plz
					+ StringTool.space + ort, resolve1, hints);
		} else {
			return (Organisation) matchAddress(found.toArray(new Kontakt[0]), strasse, plz, ort,
				null);
		}
	}
	
	public static Patient findPatient(final String name, final String vorname, final String gebdat,
		final String gender, final String strasse, final String plz, final String ort,
		final String natel, final CreateMode createMode){
		Person pat =
			findPerson(name, vorname, gebdat, gender, strasse, plz, ort, natel, createMode, true);
		if (pat != null)
			return Patient.load(pat.getId());
		else
			return null;
	}
	
	public static Person findPerson(final String name, final String vorname, final String gebdat,
		final String gender, final String strasse, final String plz, final String ort,
		final String natel, final CreateMode createMode){
		return findPerson(name, vorname, gebdat, gender, strasse, plz, ort, natel, createMode,
			false);
	}
	
	/**
	 * find the Person matching the given parameters
	 * 
	 * @param name
	 * @param vorname
	 * @param gebdat
	 * @param gender
	 * @param strasse
	 * @param plz
	 * @param ort
	 * @param natel
	 * @param createIfNotExists
	 * @return the found person or null if no matching person wasd found
	 */
	public static Person findPerson(final String name, final String vorname, final String gebdat,
		final String gender, final String strasse, final String plz, final String ort,
		final String natel, final CreateMode createMode, final boolean isPatient){
		String[] hints = new String[HINTSIZE];
		hints[HINT_NAME] = name;
		hints[HINT_FIRSTNAME] = vorname;
		hints[HINT_BIRTHDATE] = gebdat;
		hints[HINT_SEX] = gender;
		hints[HINT_STREET] = strasse;
		hints[HINT_ZIP] = plz;
		hints[HINT_PLACE] = ort;
		if (isPatient) {
			hints[HINT_PATIENT] = StringConstants.ONE;
		}
		Query<Person> qbe = new Query<Person>(Person.class);
		String sex = StringTool.leer;
		String birthdate = StringTool.leer;
		
		if (!StringTool.isNothing(name)) {
			qbe.startGroup();
			qbe.add(Person.NAME, "LIKE", name + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			String un = StringTool.unambiguify(name);
			if (!un.equalsIgnoreCase(name)) {
				qbe.or();
				qbe.add(Person.NAME, "LIKE", un + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			}
			qbe.endGroup();
			qbe.and();
		}
		
		if (!StringTool.isNothing(vorname)) {
			qbe.startGroup();
			qbe.add(Person.FIRSTNAME, "LIKE", vorname + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			String un = StringTool.unambiguify(vorname);
			if (!un.equalsIgnoreCase(vorname)) {
				qbe.or();
				qbe.add(Person.FIRSTNAME, "LIKE", un + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
			}
			qbe.endGroup();
			qbe.and();
		}
		if (!StringTool.isNothing(gebdat)) {
			TimeTool tt = new TimeTool();
			if (tt.set(gebdat)) {
				birthdate = tt.toString(TimeTool.DATE_GER);
				qbe.add(Person.BIRTHDATE, Query.EQUALS, tt.toString(TimeTool.DATE_COMPACT));
			}
		}
		if (!StringTool.isNothing(gender)) {
			String gl = gender.toLowerCase();
			if (gl.startsWith("f") || gl.startsWith("w")) { //$NON-NLS-1$ //$NON-NLS-2$
				sex = Person.FEMALE;
			} else if (gl.startsWith("m")) { //$NON-NLS-1$
				sex = Person.MALE;
			} else {
				if (StringTool.isNothing(vorname)) {
					sex = "?"; //$NON-NLS-1$
				} else {
					sex = StringTool.isFemale(vorname) ? Person.FEMALE : Person.MALE;
				}
			}
			qbe.add(Person.SEX, Query.EQUALS, sex);
		}
		List<Person> found = qbe.execute();
		if (found.size() == 0) {
			if (createMode == CreateMode.CREATE) {
				Person ret = new Person(name, vorname, birthdate, sex);
				addAddress(ret, strasse, plz, ort);
				return ret;
			} else if (createMode == CreateMode.ASK) {
				return (Person) KontaktSelektor.showInSync(Person.class,
					Messages.KontaktMatcher_PersonNotFound, name + StringTool.space + vorname
						+ (StringTool.isNothing(gebdat) ? StringTool.leer : SEP + gebdat) + SEP
						+ strasse + SEP + plz + " " + ort, resolve1, hints);
			}
			return null;
		}
		if (found.size() == 1) {
			return found.get(0);
		}
		// more than 1 hit
		if (createMode == CreateMode.ASK) {
			return (Person) KontaktSelektor.showInSync(Person.class,
				Messages.KontaktMatcher_PersonNotUnique,
				name + " " + vorname
					+ (StringTool.isNothing(gebdat) ? StringTool.leer : SEP + gebdat) + SEP
					+ strasse + SEP + plz + " " + ort, resolve1, hints);
		} else {
			return (Person) matchAddress(found.toArray(new Kontakt[0]), strasse, plz, ort, natel);
		}
	}
	
	/**
	 * Given an array of Kontakt, find the one that matches the given address best
	 * 
	 * @param kk
	 * @param strasse
	 * @param plz
	 * @param ort
	 * @param natel
	 * @return
	 */
	public static Kontakt matchAddress(final Kontakt[] kk, final String strasse, final String plz,
		final String ort, final String natel){
		
		int[] score = new int[kk.length];
		
		for (int i = 0; i < kk.length; i++) {
			
			// If we have the same mobile number, that's a strong hint
			if (!StringTool.isNothing(natel)) {
				if (normalizePhone(kk[i].get("NatelNr")).equals(normalizePhone(natel))) { //$NON-NLS-1$
					score[i] += 5;
				}
			}
			
			// If we have the same street address, that's also a good hint
			if (!StringTool.isNothing(strasse)) {
				if (isSameStreet(kk[i].get(Kontakt.FLD_STREET), strasse)) {
					score[i] += 3;
				} else {
					score[i] -= 2;
				}
			}
			
			// If we have the same zip or the same olace, that's a quite weak hint.
			if (!StringTool.isNothing(plz)) {
				if (plz.equals(kk[i].get(Kontakt.FLD_ZIP))) {
					score[i] += 2;
				} else {
					score[i] -= 1;
				}
			}
			if (!StringTool.isNothing(ort)) {
				if (ort.equals(kk[i].get(Kontakt.FLD_PLACE))) {
					score[i] += 1;
				} else {
					score[i] -= 1;
				}
			}
			
		}
		Kontakt found = kk[0];
		int scored = score[0];
		for (int i = 1; i < score.length; i++) { // we'll take the match with the highest score
			if (score[i] > scored) {
				found = kk[i];
				scored = score[i];
			}
		}
		return found;
	}
	
	/**
	 * try to figure out which part of a string is the zip and which is the place
	 * 
	 * @param str
	 *            a string containing possibly zip and possibly place
	 * @return always a two element array, [0] is zip or "", [1] is place or ""
	 */
	public static String[] normalizeAddress(String str){
		String[] ret = str.split("\\s+", 2); //$NON-NLS-1$
		if (ret.length < 2) {
			String[] rx = new String[2];
			rx[0] = "";
			rx[1] = ret[0];
			return rx;
		}
		return ret;
	}
	
	/**
	 * Remove all non-numbers out of phone strings
	 * 
	 * @param nr
	 * @return
	 */
	public static String normalizePhone(final String nr){
		return nr.replaceAll("[\\s-:\\.]", StringTool.leer); //$NON-NLS-1$
	}
	
	/**
	 * Try to figure out if two street strings denote the same street address
	 * 
	 * @return true if the streets seem to be equal
	 */
	public static boolean isSameStreet(final String s1, final String s2){
		String[] ns1 = normalizeStrasse(s1);
		String[] ns2 = normalizeStrasse(s2);
		if (!(ns1[0].matches(ns2[0]))) {
			return false;
		}
		if (!(ns1[1].matches(ns2[1]))) {
			return false;
		}
		return true;
	}
	
	static String[] normalizeStrasse(final String strasse){
		String[] m1 = StringTool.normalizeCase(strasse).split("\\s"); //$NON-NLS-1$
		int m1l = m1.length;
		StringBuilder m2 = new StringBuilder();
		m2.append(m1[0]);
		String nr = "0";
		if (m1l > 1) {
			if (m1[m1l - 1].matches("[0-9]+[a-zA-Z]")) { //$NON-NLS-1$
				nr = m1[m1l - 1];
				m1l -= 1;
			}
			if (m1l > 1) {
				for (int i = 1; i < m1l; i++) {
					m2.append(StringTool.space).append(m1[i]);
				}
			}
		}
		return new String[] {
			m2.toString(), nr
		};
		
	}
	
	public static void addAddress(final Kontakt k, String str, String plzort){
		String[] ort = plzort.split("[\\s+]"); //$NON-NLS-1$
		if (ort.length == 2) {
			addAddress(k, str, ort[0], ort[1]);
		} else if (ort.length > 2) {
			StringBuilder plz = new StringBuilder();
			for (int i = 1; i < ort.length; i++) {
				plz.append(ort[i]).append(StringTool.space);
			}
			addAddress(k, str, ort[0], plz.toString());
		} else {
			addAddress(k, str, ort[0], StringTool.leer);
		}
	}
	
	public static void addAddress(final Kontakt k, final String str, String plz, final String ort){
		Anschrift an = k.getAnschrift();
		if (!StringTool.isNothing(str)) {
			an.setStrasse(str);
		}
		if (!StringTool.isNothing(plz)) {
			if (plz.matches("[A-Z]{1,3}[\\s\\-]+[A-Za-z0-9]+")) { //$NON-NLS-1$
				String[] plzx = plz.split("[\\s\\-]+", 1); //$NON-NLS-1$
				if (plzx.length > 1) {
					plz = plzx[1];
					an.setLand(plzx[0]);
				}
			}
			an.setPlz(plz);
		}
		if (!StringTool.isNothing(ort)) {
			an.setOrt(ort);
		}
		k.setAnschrift(an);
		k.createStdAnschrift();
	}
	
	/**
	 * Decide whether a person is identical to given personal data. Normalize all names: Ulmlaute
	 * will be converted, accents will be eliminatet and double names will be reduced to their first
	 * part.
	 * 
	 * @return true if the given person seems to be the same than the given personalia
	 */
	public static boolean isSame(final Person a, final String nameB, final String firstnameB,
		final String gebDatB){
		try {
			String name1 = StringTool.unambiguify(simpleName(a.getName()));
			String name2 = StringTool.unambiguify(simpleName(nameB));
			if (name1.equals(name2)) {
				String vorname1 = StringTool.unambiguify(simpleName(a.getVorname()));
				String vorname2 = StringTool.unambiguify(simpleName(firstnameB));
				if (vorname1.equals(vorname2)) {
					if (StringTool.isNothing(a.getGeburtsdatum()) || StringTool.isNothing(gebDatB)) {
						return true;
					}
					TimeTool gd1 = new TimeTool(a.getGeburtsdatum());
					TimeTool gd2 = new TimeTool(gebDatB);
					if (gd1.equals(gd2)) {
						return true;
					}
				}
			}
			
		} catch (Throwable t) {
			ExHandler.handle(t);
			
		}
		return false;
	}
	
	static String simpleName(final String name){
		String[] ret = name.split("\\s*[- ]\\s*"); //$NON-NLS-1$
		return ret[0];
	}
	
	final static String resolve1 = Messages.KontaktMatcher_noauto1
		+ Messages.KontaktMatcher_noauto2 + Messages.KontaktMatcher_noauto3
		+ Messages.KontaktMatcher_noauto4 + Messages.KontaktMatcher_noauto5;
	
}
