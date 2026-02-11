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

package ch.elexis.core.utils;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Class to match personal data to contacts
 *
 * @author gerry
 */

public class KontaktMatcher {
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
	public static IContact matchAddress(final IContact[] kk, final String strasse, final String plz, final String ort,
			final String natel) {

		int[] score = new int[kk.length];

		for (int i = 0; i < kk.length; i++) {

			// If we have the same mobile number, that's a strong hint
			if (!StringTool.isNothing(natel)) {
				if (normalizePhone(kk[i].getMobile()).equals(normalizePhone(natel))) { // $NON-NLS-1$
					score[i] += 5;
				}
			}

			// If we have the same street address, that's also a good hint
			if (!StringTool.isNothing(strasse)) {
				if (isSameStreet(kk[i].getStreet(), strasse)) {
					score[i] += 3;
				} else {
					score[i] -= 2;
				}
			}

			// If we have the same zip or the same olace, that's a quite weak hint.
			if (!StringTool.isNothing(plz)) {
				if (plz.equals(kk[i].getZip())) {
					score[i] += 2;
				} else {
					score[i] -= 1;
				}
			}
			if (!StringTool.isNothing(ort)) {
				if (ort.equals(kk[i].getCity())) {
					score[i] += 1;
				} else {
					score[i] -= 1;
				}
			}

		}
		IContact found = kk[0];
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
	 * @param str a string containing possibly zip and possibly place
	 * @return always a two element array, [0] is zip or StringUtils.EMPTY, [1] is
	 *         place or StringUtils.EMPTY
	 */
	public static String[] normalizeAddress(String str) {
		String[] ret = str.split("\\s+", 2); //$NON-NLS-1$
		if (ret.length < 2) {
			String[] rx = new String[2];
			rx[0] = StringUtils.EMPTY;
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
	public static String normalizePhone(final String nr) {
		return nr.replaceAll("[\\s-:\\.]", StringTool.leer); //$NON-NLS-1$
	}

	/**
	 * Try to figure out if two street strings denote the same street address
	 *
	 * @return true if the streets seem to be equal
	 */
	public static boolean isSameStreet(final String s1, final String s2) {
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

	static String[] normalizeStrasse(final String strasse) {
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
		return new String[] { m2.toString(), nr };

	}

	/**
	 * Decide whether a person is identical to given personal data. Normalize all
	 * names: Umlaute will be converted, accents will be eliminatet and double names
	 * will be reduced to their first part.
	 *
	 * @return true if the given person seems to be the same than the given
	 *         personalia
	 */
	public static boolean isSame(final IPerson a, final String nameB, final String firstnameB, final String gebDatB) {
		try {
			String name1 = StringTool.unambiguify(simpleName(a.getLastName()));
			String name2 = StringTool.unambiguify(simpleName(nameB));
			if (name1.equals(name2)) {
				String vorname1 = StringTool.unambiguify(simpleName(a.getFirstName()));
				String vorname2 = StringTool.unambiguify(simpleName(firstnameB));
				if (vorname1.equals(vorname2)) {
					if (StringTool.isNothing(a.getDateOfBirth()) || StringTool.isNothing(gebDatB)) {
						return true;
					}
					TimeTool gd1 = new TimeTool(a.getDateOfBirth());
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

	public static boolean isSame(IPatient a, String nameB, String firstnameB, String gebDatB) {
		try {
			String name1 = StringTool.unambiguify(simpleName(a.getDescription1()));
			String name2 = StringTool.unambiguify(simpleName(nameB));
			if (name1.equals(name2)) {
				String vorname1 = StringTool.unambiguify(simpleName(a.getDescription2()));
				String vorname2 = StringTool.unambiguify(simpleName(firstnameB));
				if (vorname1.equals(vorname2)) {
					if (StringTool.isNothing(a.getDateOfBirth()) || StringTool.isNothing(gebDatB)) {
						return true;
					}
					TimeTool gd1 = new TimeTool(a.getDateOfBirth());
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

	static String simpleName(final String name) {
		String[] ret = name.split("\\s*[- ]\\s*"); //$NON-NLS-1$
		return ret[0];
	}

}
