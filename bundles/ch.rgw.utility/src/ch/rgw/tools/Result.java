/*******************************************************************************
 * Copyright (c) 2005-2019, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT <office@medevit> - multiple adaptations
 *******************************************************************************/

package ch.rgw.tools;

import java.util.ArrayList;
import java.util.List;

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
		if (list.size() == 0)
			return null;
		
		msg result = list.get(0);
		if (list.size() > 1) {
			for (msg m : list) {
				if (m.severity.ordinal() > list.get(0).severity.ordinal()) {
					result = m;
				}
			}
		}
		return result.result;
	}
	
	/**
	 * Generate an OK if result is an object, or ERROR if result is a {@link Throwable}
	 * 
	 * @param result
	 * @return
	 * @since 3.8
	 */
	public Result(T result){
		if (result instanceof Throwable) {
			Throwable _result = (Throwable) result;
			add(SEVERITY.ERROR, 0, _result.getMessage(), null, false);
		} else {
			add(SEVERITY.OK, 0, "Ok", result, false); //$NON-NLS-1$
		}
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
		return this;
	}
	
	/**
	 * Add a result to the given result. If the added result has a higher severity than the existing
	 * one, the overal severity is raised to match.
	 * 
	 * @param r
	 * @return
	 */
	public Result<T> add(Result<T> r){
		if (r.severity.ordinal() > this.severity.ordinal()) {
			this.severity = r.severity;
			this.code = r.code;
		}
		list.addAll(r.list);
		return this;
	}
	
	public Result(){}
	
	public Result(SEVERITY severity, List<msg> msgs){
		if (severity.ordinal() > this.severity.ordinal()) {
			this.severity = severity;
		}
		if (msgs != null) {
			list.addAll(msgs);
		}
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
		return "Result " + severity + " "
			+ list.stream().map(x -> x.text).reduce((x, y) -> x + "," + y).get();
	}
	
	/**
	 * Convenience method.
	 * 
	 * @return an {@link #isOK()} == true result, no element contained (i.e. {@link #get()} returns
	 *         <code>null</code>)
	 */
	public static final <T> Result<T> OK(){
		return (Result<T>) new Result<T>(null);
	}
	
	public static final Result<String> OK(String text){
		return new Result<String>(SEVERITY.OK, 0, text, text, false);
	}
	
	public static final Result<String> ERROR(String text){
		return new Result<String>(SEVERITY.ERROR, 0, text, text, false);
	}
	
	public void addMessage(SEVERITY severity, String message){
		list.add(new msg(0, message, severity, null));
	}
	
}
