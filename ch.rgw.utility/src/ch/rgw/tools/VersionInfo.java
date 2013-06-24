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

/**
 * Einfaches API zum Vergleich von Versionsnummern des Typs maior.minor.rev
 * 
 * @author G. Weirich
 * @version 1.6.0
 */

public class VersionInfo implements Comparable<VersionInfo> {
	
	public static String Version(){
		return "1.8.0";
	}
	
	String orig;
	String[] spl;
	
	public VersionInfo(){
		this(Version());
	}
	
	public VersionInfo(final String v){
		if (StringTool.isNothing(v)) {
			orig = null;
			spl = null;
		} else {
			orig = v;
			spl = orig.split("\\.");
		}
	}
	
	public String getMaior(){
		if ((spl == null) || (spl.length < 1)) {
			return "0";
		}
		if (StringTool.isNothing(spl[0])) {
			return "0";
		}
		return spl[0];
	}
	
	public String getMinor(){
		if ((spl == null) || (spl.length < 2)) {
			return "0";
		}
		return spl[1];
	}
	
	public String getRevision(){
		if ((spl == null) || (spl.length < 3)) {
			return "0";
		}
		return spl[2];
	}
	
	public String getBuildTag(){
		if ((spl == null) || (spl.length < 4)) {
			return "";
		} else {
			return spl[3];
		}
	}
	
	public String version(){
		return orig;
	}
	
	public boolean matches(VersionInfo pattern){
		for (int i = 0; i < 3; i++) {
			if (i >= spl.length) {
				if (i >= pattern.spl.length) {
					return true;
				}
				return false;
			}
			if (i >= pattern.spl.length) {
				return false;
			}
			if (!matchElements(spl[0], pattern.spl[0])) {
				return false;
			}
		}
		return true;
	}
	
	private boolean matchElements(final String a, final String b){
		if (a.equals("*") || b.equals("*")) {
			return true;
		}
		if (compareElem(a, b) == 0) {
			return true;
		}
		return false;
	}
	
	public boolean isNewer(final String other){
		VersionInfo vo = new VersionInfo(other);
		return isNewer(vo);
	}
	
	public boolean isOlder(final String other){
		VersionInfo vn = new VersionInfo(other);
		return isOlder(vn);
	}
	
	/**
	 * Ist diese Version neuer, als die andere?
	 * 
	 * @param vo
	 *            die andere
	 * @return true:ja, false: nein
	 */
	public boolean isNewer(final VersionInfo vo){
		return (compareTo(vo) > 0);
	}
	
	public boolean isOlder(final VersionInfo vo){
		return (compareTo(vo) < 0);
	}
	
	public boolean isNewerMaior(final VersionInfo vo){
		return compareElem(this.getMaior(), vo.getMaior()) > 0;
	}
	
	public boolean isNewerMinor(final VersionInfo vo){
		if (isNewerMaior(vo)) {
			return true;
		}
		if (isOlder(vo)) {
			return false;
		}
		return compareElem(this.getMinor(), vo.getMinor()) > 0;
	}
	
	public boolean isNewerRev(final VersionInfo vo){
		return isNewerMaior(vo) ? true : isNewerMinor(vo) ? true : compareElem(this.getRevision(),
			vo.getRevision()) > 0;
	}
	
	public boolean isNewerBuild(final VersionInfo vo){
		return isNewerRev(vo) ? true : compareElem(this.getBuildTag(), vo.getBuildTag()) > 0;
	}
	
	public boolean isEqual(final VersionInfo vo){
		return (compareTo(vo) == 0);
	}
	
	public int compareTo(final VersionInfo vo){
		int c = compareElem(this.getMaior(), vo.getMaior());
		if (c != 0) {
			return c;
		}
		c = compareElem(this.getMinor(), vo.getMinor());
		if (c != 0) {
			return c;
		}
		c = compareElem(this.getRevision(), vo.getRevision());
		if (c != 0) {
			return c;
		}
		return compareElem(this.getBuildTag(), vo.getBuildTag());
	}
	
	private int compareElem(final String a, final String b){
		int al = a.length();
		int bl = b.length();
		if (al == bl) {
			return a.compareToIgnoreCase(b);
		}
		int diff = Math.abs(al - bl);
		String x = StringTool.pad(StringTool.LEFT, '0', a, al + diff + 1);
		String y = StringTool.pad(StringTool.LEFT, '0', b, al + diff + 1);
		return x.compareToIgnoreCase(y);
	}
}