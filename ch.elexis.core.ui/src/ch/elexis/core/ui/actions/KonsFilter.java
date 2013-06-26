/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.text.model.Samdas;

/**
 * Ein Filter für Konsultationen
 * 
 * @author gerry
 * 
 */
public class KonsFilter {
	public static final int AND = 1;
	public static final int OR = 2;
	public static final int NOT = 4;
	private Fall fall;
	private boolean caseSensitive;
	private boolean asRegEx;
	private List<Constraint> lc = new LinkedList<Constraint>();
	
	public void setFall(Fall f){
		fall = f;
	}
	
	public void addConstraint(int link, String word){
		lc.add(new Constraint(link, word));
	}
	
	public void clear(){
		lc.clear();
	}
	
	/**
	 * Festlegen, ob Suchausdrücke case-sensitive betrachtet werden sollen
	 */
	public void setCaseSensitive(boolean caseSensitive){
		this.caseSensitive = caseSensitive;
	}
	
	/**
	 * Festlegen, ob Suchausdrücke als Regular Expressions betrachtet werden sollen
	 */
	public void setAsRegEx(boolean asRegEx){
		this.asRegEx = asRegEx;
	}
	
	/**
	 * Entscheiden, ob eine bestimmte Konsultation durch den Filter geht
	 * 
	 * @param k
	 *            die Konsultation, die getestet werden soll
	 * @return true: Kons. geht durch (default)
	 */
	public boolean pass(Konsultation k){
		if (k == null) {
			return false;
		}
		if (!k.exists()) {
			return false;
		}
		Fall kf = k.getFall();
		if ((fall != null) && (kf != null)) {
			if (!fall.getId().equals(kf.getId())) {
				return false;
			}
		}
		if (lc.size() == 0) {
			return true;
		}
		boolean nVal = true;
		boolean lastVal = false;
		String tx = new Samdas(k.getEintrag().getHead()).getRecordText();
		for (Constraint c : lc) {
			boolean matchVal = false;
			if (asRegEx) {
				Pattern pat = Pattern.compile(c.word);
				Matcher match = pat.matcher(tx);
				matchVal = match.find();
			} else if (caseSensitive) {
				matchVal = (tx.indexOf(c.word) != -1);
			} else {
				matchVal = (tx.toLowerCase().indexOf(c.word.toLowerCase()) != -1);
			}
			boolean neg = (c.mode & NOT) != 0;
			nVal = (matchVal) ^ neg;
			if ((c.mode & KonsFilter.OR) != 0) {
				if ((lastVal == false) && (nVal == false)) {
					return false;
				}
				lastVal = true;
			} else if ((c.mode & AND) != 0) {
				if ((lastVal == false) || (nVal == false)) {
					return false;
				}
				lastVal = true;
			} else {
				lastVal = nVal;
			}
		}
		return lastVal;
	}
	
	private static class Constraint {
		int mode;
		String word;
		
		Constraint(int m, String w){
			mode = m;
			word = w;
		}
	}
}
