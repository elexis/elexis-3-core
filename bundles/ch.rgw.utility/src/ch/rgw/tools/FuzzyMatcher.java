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

package ch.rgw.tools;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Title: Toolbox
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: rgw
 * </p>
 * 
 * @author G. Weirich
 * @version 1.0
 */

public class FuzzyMatcher {
	public static String Version(){
		return "1.1.1";
	}
	
	static final public int LITERAL = 0;
	static final public int LEVENSHTEIN = 1;
	static final public int SHIFT_AND = 2;
	static final public int REGEX = 3;
	static final public int SYNPHON = 4;
	
	static final public int EXACT = 0;
	static final public int SHARP = 1;
	static final public int MEDIUM = 2;
	static final public int BLURRED = 3;
	
	String pattern;
	
	int type;
	int level;
	boolean emptyMatches;
	private static Logger log;
	
	static {
		log = log.getLogger("Matcher");
	}
	
	private FuzzyMatcher(){
		emptyMatches = false;
	}
	
	public static FuzzyMatcher createWLDMatcher(String pattern, int level){
		FuzzyMatcher ret = new FuzzyMatcher();
		ret.type = LEVENSHTEIN;
		ret.pattern = pattern;
		ret.level = level;
		if (level > 2)
			ret.emptyMatches = true;
		log.log(Level.FINE, "create WLDMatcher level " + ret.level);
		return ret;
	}
	
	public static FuzzyMatcher createSynphonMatcher(String pattern, int level){
		FuzzyMatcher ret = new FuzzyMatcher();
		ret.type = SYNPHON;
		if (level > 2)
			ret.emptyMatches = true;
		switch (level) {
		case 1:
			ret.level = 5;
			break;
		case 2:
			ret.level = 3;
			break;
		default:
			ret.level = 2;
		}
		ret.pattern = SYPH_compile(pattern.trim(), ret.level);
		log.log(Level.FINE, "create SYPHMatcher level " + ret.level);
		log.log(Level.FINE, "pattern: " + ret.pattern);
		return ret;
	}
	
	public static FuzzyMatcher createLiteralMatcher(String pattern, int level){
		FuzzyMatcher ret = new FuzzyMatcher();
		ret.type = LITERAL;
		ret.level = level;
		ret.pattern = pattern;
		return ret;
	}
	
	public String getPattern(){
		return pattern;
	}
	
	public boolean match(String w1){
		if (StringTool.isNothing(w1)) {
			return emptyMatches;
		}
		
		String wort = w1.trim();
		if (level == EXACT) {
			return wort.equals(pattern);
		}
		String[] pat = wort.split("[\\s,\\.]");
		if ((pat.length < 1) || (StringTool.isNothing(pat[0])) || pat[0].equals(" ")) {
			return emptyMatches;
		}
		switch (type) {
		case LEVENSHTEIN:
			return (WLD(pat[0], pattern, '*', level) <= level);
		case SYNPHON:
			return SynPhon(pat[0], pattern, level);
		case LITERAL:
			return pat[0].equalsIgnoreCase(pattern);
		default:
			return false;
		}
	}
	
	/* Levenshtein */
	private static String formatierung(String wort, char modus){
		String res = wort.toUpperCase();
		res = res.replaceAll("ä", "ae");
		res = res.replaceAll("Ä", "Ae");
		res = res.replaceAll("ö", "oe");
		res = res.replaceAll("Ö", "Oe");
		res = res.replaceAll("ü", "ue");
		res = res.replaceAll("Ü", "Ue");
		if (modus == '*') {
			res = res.replaceAll("\\**", "\\*");
		}
		return res;
	}
	
	/**
	 * weighted levenshtein distance Gibt "Distanz" zwischen Wort und Muster
	 */
	public static int WLD(String wort, String muster, char modus, int limit){
		final int maxlen = 100;
		int spmin, p, q, r, d1, d2, i, k, x1, x2, x3;
		char c;
		String ww, mm;
		int[] d = new int[maxlen];
		
		if (modus == '+' || modus == '*') {
			// lw = formatierung (ww, wort, maxlen,modus);
			// lm = formatierung (mm,muster,maxlen,modus);
			ww = formatierung(wort, modus);
			mm = formatierung(muster, modus);
			
			if ((modus == '*') && (ww.length() < mm.length() - 1) && (ww.indexOf('*') != -1)) {
				/**** Wort und Muster tauschen ****/
				i = ww.length();
				wort = mm;
				muster = ww + "*";
				/**** Limit neu setzen ****/
				
				i = (i / 3);
				if (i < limit) {
					limit = i;
				}
			} else {
				wort = ww;
				muster = mm;
			}
		} // modus='*'
		
		/**** Anfangswerte berechnen ****/
		if (muster.charAt(0) == '*') {
			for (k = 0; k <= wort.length(); k++) {
				d[k] = 0;
			}
		} else {
			d[0] = (muster.equals("")) ? 0 : 1;
			i = (muster.charAt(0) == '?') ? 0 : 1;
			for (k = 1; k <= wort.length(); k++) {
				if (muster.charAt(0) == wort.charAt(k - 1)) {
					i = 0;
				}
				d[k] = k - 1 + i;
			}
		}
		
		spmin = (d[0] == 0 || wort.length() == 0) ? d[0] : d[1];
		if (spmin > limit) {
			return (maxlen);
		}
		
		/**** Distanzmatrix durchrechnen ****/
		for (i = 2; i <= muster.length(); i++) {
			c = muster.charAt(i - 1);
			p = (c == '*' || c == '?') ? 0 : 1;
			q = (c == '*') ? 0 : 1;
			r = (c == '*') ? 0 : 1;
			d2 = d[0];
			d[0] = d2 + q;
			spmin = d[0];
			
			for (k = 1; k <= wort.length(); k++) {
				/**** d[k] = Minimum dreier Zahlen ****/
				d1 = d2;
				d2 = d[k];
				x1 = d1 + ((c == wort.charAt(k - 1)) ? 0 : p);
				x2 = d2 + q;
				x3 = d[k - 1] + r;
				
				if (x1 < x2) {
					x2 = x1;
				}
				d[k] = (x2 < x3) ? x2 : x3;
				
				if (d[k] < spmin) {
					spmin = d[k];
				}
			}
			
			if (spmin > limit) {
				return (maxlen);
			}
		}
		return ((d[wort.length()] <= limit) ? d[wort.length()] : maxlen);
	}
	
	// SynPhon-Algorithmus: �hnlicher Klang; f�r deutsche SPrache optimiert
	
	private static final String Vokale = "[aeiou���yh]";
	private static final String[] grps = {
		"1,ei,ey,ay,ai,eu", "2,au,aw", "x,ks,gs", "b,p,mb,mp", "g,ck,k,q", "s,sch,sz,ts,cz,ch,c,z",
		"d,t,mt,md", "f,ph,v,w"
	};
	
	public static String SYPH_compile(String wort, int l){
		String r1 = (wort.toLowerCase());
		// 1. Gruppen ersetzen
		for (int i = 0; i < grps.length; i++) {
			String[] px = grps[i].split(",");
			for (int j = 1; j < px.length; j++) {
				r1 = r1.replaceAll(px[j], px[0]);
			}
		}
		// 2. verbleibende Vokale und h entfernen
		String r2 = r1.replaceAll(Vokale, "");
		// 3. Doppelzeichen entfernen
		
		String r3 = r2.replaceAll("(.)\\1", "$1");
		if (r3.length() > l) {
			return r3.substring(0, l);
		}
		return r3;
	}
	
	public static boolean SynPhon(String wort, String pattern, int l){
		String cw = SYPH_compile(wort, l);
		if (cw.equals(pattern)) {
			return true;
		}
		
		return false;
	}
}