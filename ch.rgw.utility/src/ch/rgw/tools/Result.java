/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Universelles Rückgabe-Objekt. Ein Result beinhaltet die Information, ob ein Fehler erfolgt ist,
 * ggf. den Schweregrad des Fehlers, ein Rückgabeobjekt (bei Erfolgreicher Ausführung), eine
 * Fehlerbeschreibung bei Fehler. Ein Result kann mehrere Fehlermeldungen aufnehmen (und so durch
 * mehrere Funktionen propagiert werden) Wenn ein Result mehr als ein Resultat enthält, so ist das
 * Gesamtesultat das "schlimmste", also das mit der höchsten severity. Wenn ein Result gar kein
 * Resultat enthält, so ist es "OK". Eine Methode kann entweder ein neues Result-Objekt erzeugen,
 * oder ein übergebenes Resultobjekt um eine Meldung erweitern.
 * 
 * @author Gerry
 * 
 */
public class Result<T> {
	static final Logger log = Logger.getLogger("Result");
	
	public enum SEVERITY {
		OK, WARNING, ERROR, FATAL
	};
	
	List<msg> list = new ArrayList<msg>();
	private SEVERITY severity = SEVERITY.OK;
	private int code;
	
	public SEVERITY getSeverity(){
		return severity;
	}
	
	public int getCode(){
		return code;
	}
	
	public List<msg> getMessages(){
		return list;
	}
	
	/**
	 * Kurze Abfrage, ob alles fehlerfrei war
	 * 
	 * @return true wenn ja
	 */
	public boolean isOK(){
		if (list.size() > 0) {
			for (msg m : list) {
				if (m.severity != SEVERITY.OK) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Den "eigentlichen" Rückgabewert der Methode abholen
	 * 
	 * @return
	 */
	public T get(){
		msg result = list.size() == 0 ? null : list.get(0);
		if (list.size() > 1) {
			for (msg m : list) {
				if (m.severity.ordinal() > result.severity.ordinal()) {
					result = m;
				}
			}
		}
		return result == null ? null : result.result;
	}
	
	/**
	 * Einen OK - Status abholen
	 * 
	 * @param result
	 * @return
	 */
	public Result(T result){
		add(SEVERITY.OK, 0, "Ok", result, false); //$NON-NLS-1$
		// return new Result<Object>(Log.NOTHING,0,"Ok",result,false);
	}
	
	/**
	 * Ein neues Resultat hinzufügen
	 * 
	 * @param severity
	 * @param code
	 * @param text
	 * @param result
	 * @param log
	 * @return
	 */
	public Result<T> add(SEVERITY severity, int code, String text, T result, boolean log){
		list.add(new msg(code, text, severity, result));
		if (severity.ordinal() > this.severity.ordinal()) {
			this.severity = severity;
			this.code = code;
		}
		if (log == true) {
			
		}
		return this;
	}
	
	/**
	 * Ein Result zu einem Result hinzufügen
	 * 
	 * @param r
	 * @return
	 */
	public Result<T> add(Result<T> r){
		list.addAll(r.list);
		return this;
	}
	
	public Result(){}
	
	public Result(SEVERITY sev, List<msg> msgs){
		list.addAll(msgs);
	}
	
	public Result(SEVERITY severity, int code, String text, T result, boolean bLog){
		add(severity, code, text, result, bLog);
	}
	
	public class msg {
		int code;
		String text;
		SEVERITY severity;
		T result;
		
		msg(int c, String t, SEVERITY s, T r){
			code = c;
			text = t;
			severity = s;
			result = r;
		}
		
		public SEVERITY getSeverity(){
			return severity;
		}
		
		public int getCode(){
			return code;
		}
		
		public String getText(){
			return text;
		}
	}
	
	/**
	 * Return the result as String, cr-separated list of entries
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder(200);
		for (msg m : list) {
			sb.append(m.text).append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}
	
}
