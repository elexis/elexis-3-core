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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Zeitspanne, bestehnd aus zwei {@link TimeTool timeTools}
 * 
 * @author G. Weirich
 */

public class TimeSpan implements Comparable<TimeSpan> {
	public static String Version(){
		return "1.6.3";
	}
	
	public TimeTool from;
	public TimeTool until;
	
	/**
	 * timeSpan, die den Zeitraum von v bis b repräsentiert
	 * 
	 * @param v
	 *            Startzeit
	 * @param b
	 *            Endzeit
	 */
	public TimeSpan(TimeTool v, TimeTool b){
		from = v;
		until = b;
	}
	
	/**
	 * timeSpan, die den Zeitraum v bis v+d repräsentiert
	 * 
	 * @param v
	 *            Startzeit
	 * @param d
	 *            Dauer in Standardeinheiten von v
	 */
	public TimeSpan(TimeTool v, int d){
		from = v;
		until = new TimeTool(v);
		until.addUnits(d);
	}
	
	public TimeSpan(){
		from = new TimeTool();
		until = new TimeTool();
	}
	
	public TimeSpan(TimeSpan o){
		from = new TimeTool(o.from);
		until = new TimeTool(o.until);
	}
	
	/**
	 * timeSpan, deren Anfangs- und Endzeit in Stringform angegeben wird
	 * 
	 * @param ti
	 *            String der Form hh:mm[:ss]-hh:mm[:ss] oder einen String wie von toString()
	 *            geliefert.
	 */
	public TimeSpan(String ti){
		set(ti);
	}
	
	public boolean contains(TimeTool t){
		if (t.isBefore(from)) {
			return false;
		}
		if (t.isAfter(until)) {
			return false;
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0){
		if (arg0 instanceof TimeSpan) {
			TimeSpan o = (TimeSpan) arg0;
			if ((from.isEqual(o.from)) && (until.isEqual(o.until)))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return (until.getTimeInSeconds() - from.getTimeInSeconds());
	}
	
	public int compareTo(TimeSpan other){
		if (equals(other) == true)
			return 0;
		if (from.isEqual(other.from)) {
			if (until.isBefore(other.until))
				return -1;
			else
				return 1;
		}
		if (from.isBefore(other.from))
			return -1;
		
		return 1;
	}
	
	/**
	 * Dauer dieser timeSpan in Sekunden
	 * 
	 * @return
	 */
	public int getSeconds(){
		return from.secondsTo(until);
	}
	
	/**
	 * Feststellen, um wieviel diese timeSpan und eine andere überlappen
	 * 
	 * @param other
	 *            die andere timeSpan
	 * @return eine neue timeSpan, die den überlappungszeitraum enthält oder null, wenn keine
	 *         überlappung vorliegt
	 */
	public TimeSpan overlap(TimeSpan other){ /*
											 * Es sind 6 Fälle möglich: a) other ganz vor this b)
											 * other überlappt this.from c) other liegt ganz
											 * innerhalb this d) other überlappt this.until e) other
											 * liegt ganz nach this f) other überlappt this ganz
											 * ende oder Anfang auf selber Minute wird nicht als
											 * überlappung gerechnet. TimeSpan der Dauer null
											 * überlappt niemals
											 */
		if ((getSeconds() == 0) || (other.getSeconds() == 0)) {
			return null;
		}
		if (other.until.isBeforeOrEqual(from)) // Fall a)
		{
			return null;
		}
		if (other.from.isAfterOrEqual(until)) // Fall e)
		{
			return null;
		}
		if (other.from.isBeforeOrEqual(from)) {
			if (other.until.isAfterOrEqual(until)) // Fall f)
			{
				return new TimeSpan(from, until);
			} else // Fall b)
			{
				return new TimeSpan(from, other.until);
			}
		} else // other.from isafter(from)
		{
			if (other.until.isBeforeOrEqual(until)) // Fall c)
			{
				return new TimeSpan(other.from, other.until);
			} else // Fall d)
			{
				return new TimeSpan(other.from, until);
			}
		}
	}
	
	public static final int IS_BEFORE_OTHER = 1;
	public static final int IS_AFTER_OTHER = 2;
	public static final int IS_INSIDE_OTHER = 3;
	public static final int IS_AT_BEGIN_OF_OTHER = 4;
	public static final int IS_AT_END_OF_OTHER = 5;
	public static final int IS_OVER_OTHER = 6;
	public static final int IS_ZERO_LENGTH = 7;
	
	// public static final int IS_OUTSIDE_OTHER=6;
	
	/**
	 * Feststellen, wie dise timeSpan in Bezug auf eine andere liegt
	 * 
	 * @return <ul>
	 *         <li>IS_BEFORE-OTHER: Liegt ganz vor der anderen</li>
	 *         <li>IS_AFTER_OTHER: Liegt ganz nach der anderen</li>
	 *         <li>IS_INSIDE_OTHER: Liegt ganz innerhalb der anderen</li>
	 *         <li>IS_AT_BEGIN_OF_OTHER: überlappt den Anfang der anderen</li>
	 *         <li>IS_AT_END_OF_OTHER: überlappt das Ende der anderen</li>
	 *         <li>IS_OVER_OTHER: überlagert die andere ganz</li>
	 *         <li>IS_ZERO_LENGTH: Länge null sekunden</li>
	 *         </ul>
	 */
	public int positionTo(TimeSpan other){
		// Sonderfälle: Anfangszeit des einen = Endzeit des anderen
		// gilt nicht als überlappung
		// Und Zeiträume der Dauer null überlappen niemals
		if ((getSeconds() == 0) || (other.getSeconds() == 0)) {
			return IS_ZERO_LENGTH;
		}
		if (from.isBeforeOrEqual(other.from)) {
			if (until.isBeforeOrEqual(other.from)) {
				return IS_BEFORE_OTHER;
			} else if (until.isAfterOrEqual(other.until)) {
				return IS_OVER_OTHER;
			} else {
				return IS_AT_BEGIN_OF_OTHER;
			}
		} else // from.isafter(other.from)
		{
			if (until.isBeforeOrEqual(other.until)) {
				return IS_INSIDE_OTHER;
			} else if (from.isAfterOrEqual(other.until)) {
				return IS_AFTER_OTHER;
			} else {
				return IS_AT_END_OF_OTHER;
			}
		}
	}
	
	/**
	 * timeSpan auf einen als String angegebenen Zeitraum setzen
	 * 
	 * @param ti
	 *            ein String, der zwei mit - verbundene Zeiten wie bei {@link TimeTool#set}
	 *            spezifiziert enthält. z.B. 10:00-11:30, oder einen String im Kompaktformat wie
	 *            10001130, oder einen String wie von toString() geliefert
	 * @return true bei Erfolg.
	 */
	public boolean set(String ti){
		String[] vb = ti.split("-");
		if (vb.length == 2) {
			from = new TimeTool(vb[0]);
			until = new TimeTool(vb[1]);
			return true;
		}
		if (vb[0].length() == 8) {
			from = new TimeTool(vb[0].substring(0, 4));
			until = new TimeTool(vb[0].substring(4));
			return true;
		}
		if (vb[0].length() == 20) {
			try {
				from = new TimeTool(Long.parseLong(vb[0].substring(0, 10), 16));
				until = new TimeTool(Long.parseLong(vb[0].substring(10), 16));
			} catch (Exception ex) {
				ExHandler.handle(ex);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public String dump(){
		StringBuilder s = new StringBuilder(10);
		s.append(from.dump()).append(" - ").append(until.dump());
		return s.toString();
	}
	
	public String toString(){
		StringBuffer s = new StringBuffer(10);
		s.append(from.toString());
		s.append(until.toString());
		return s.toString();
	}
	
	public static String toString(Iterator<TimeTool> it){
		StringBuffer ret = new StringBuffer(200);
		while (it.hasNext()) {
			TimeTool tt = it.next();
			ret.append(tt.toString());
		}
		return ret.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static int create(Collection c, String s){
		int k = 0;
		for (int i = 0; i < s.length(); k++, i += 16) {
			TimeTool tt = new TimeTool(s.substring(i, i + 16));
			c.add(tt);
		}
		return k;
	}
	
	public static class TSComparator implements Comparator<TimeSpan> {
		
		public int compare(TimeSpan ts1, TimeSpan ts2){
			return ts1.compareTo(ts2);
		}
		
	}
}