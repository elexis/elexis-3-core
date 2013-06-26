/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.UiDesk;
import ch.rgw.tools.TimeTool;

/**
 * Standardisiertes Log. Ein Programm kann das Log mit Log.get(präfix) anfordern und fortan Ausgaben
 * ins Log mittels Log(Text,level) machen. Ob eine bestimmte Ausgabe ins Log gelangt, hängt vom
 * LogLevel und dem Text-Level ab. Wenn der Level einer Meldung gleich oder niedriger ist, als der
 * aktuell eingestellte LogLevel, wird die Ausgabe gemacht, andernfalls wird sie verworfen.
 * Ausserdem kann festgelegt werden, ab welchem level eine Nachricht zu einer direkten
 * Benachrichtigung des Anwenders mittels MessageBox führt (setAlert und setAlertLevel
 * 
 * @author G. Weirich
 */

public class Log {
	/** Experimentell */
	public static final int NOTHING = 0;
	/** Fatale Fehler, Programmabbruch */
	public static final int FATALS = 1;
	/** Nichtfatale Fehler, Programm kann weiterlaufen */
	public static final int ERRORS = 2;
	/**
	 * Warnung, Programm läuft normal weiter, es könnten aber Probleme auftreten
	 */
	public static final int WARNINGS = 3;
	/** Reine Informationen, kein Einfluss aufs Programm */
	public static final int INFOS = 4;
	/** Für Debugzwecke gedachte Meldungen */
	public static final int DEBUGMSG = 5;
	/** Immer auszugebende Meldungen, die aber keinem Fehler entsprechen */
	public static final int TRACE = 6;
	/** Immer auszugebende Meldungen, automatisch mit einem Timestamp versehen */
	public static final int SYNCMARK = -1;
	
	private static String[] Levels = {
		"OK", "FATAL", "ERROR", "WARNING", "INFO", "DEBUG", "TRACE"
	};
	
	private static Logger out = null;
	
	String prefix;
	private static int LogLevel;
	private static int alertLevel;
	private static String lastError;
	private static Shell doAlert;
	
	static {
		LogLevel = 2;
		doAlert = null;
		alertLevel = Log.FATALS;
	}
	
	/**
	 * AlertLevel einstellen. wenn der Level einer Nachricht unter diesem Level liegt, wird eine
	 * Alertbox zur Nazeige der Nachricht geöffnet (Anstatt nur zu loggen). Dies geht nur, wenn mit
	 * setAlert auch eine parent-Shell gesetzt worden ist.
	 */
	static public void setAlertLevel(int l){
		alertLevel = l;
	}
	
	/**
	 * Alert inetellen oder löschen. Wenn cmp nicht null ist, wird bei jeder
	 * Fehlermeldung>Log.Errors eine Alertbox mit der Fehlermeldung ausgegeben.
	 * 
	 * @param cmp
	 *            die Paent-Komponente für die Alertbox
	 */
	static public void setAlert(Shell cmp){
		doAlert = cmp;
	}
	
	/**
	 * Das Log anfordern. Es gibt pro Programm nur ein Log.
	 * 
	 * @param prefix
	 *            Ein String, der allen Log-Ausgaben dieser Instanz vorangestellt wird
	 * @return eine Log-Instanz
	 */
	static public Log get(String prefix){
		return new Log(prefix);
	}
	
	/**
	 * Eine Log-Nachricht ausgeben.
	 * 
	 * @param message
	 *            die Nachricht
	 * @param level
	 *            der level
	 */
	public void log(String message, int level){
		if (out == null)
			out = LoggerFactory.getLogger(Log.class);
		synchronized (out) {
			switch (level) {
			case NOTHING:
				break;
			case SYNCMARK:
				out.info("SYNC: " + message);
				break;
			case FATALS: // slf4j does not know fatal. Use error instead
			case ERRORS:
				out.error(message);
				break;
			case WARNINGS:
				out.error(message);
				break;
			case INFOS:
				out.trace(message);
				break;
			case DEBUGMSG:
				out.debug(message);
				break;
			default:
				out.debug(message);
				break;
			}
			if (level <= LogLevel) {
				if (level <= alertLevel && PlatformUI.isWorkbenchRunning()) {
					if (level != SYNCMARK) {
						if (doAlert == null) {
							doAlert = UiDesk.getTopShell();
						}
						if (doAlert != null) {
							UiDesk.asyncExec(new Runnable() {
								public void run(){
									MessageBox msg =
										new MessageBox(doAlert, SWT.ICON_ERROR | SWT.OK);
									msg.setMessage(lastError);
									msg.open();
								}
							});
							
						}
					}
				}
			}
		}
	}
	
	/**
	 * Eine Exception als Log-Nachricht ausgeben.
	 * 
	 * @param t
	 *            die Exception
	 * @param message
	 *            die Nachricht
	 * @param level
	 *            der level
	 */
	public void log(final Throwable t, String message, final int level){
		if (message == null || message.length() == 0) {
			message = t.getMessage();
			if (message == null || message.length() == 0) {
				message = t.getClass().toString();
			}
		}
		log(message, level);
		t.printStackTrace();
	}
	
	/**
	 * Eine Exception als Log-Nachricht ausgeben.
	 * 
	 * @param t
	 *            die Exception
	 * @param level
	 *            der level
	 */
	public void log(final Throwable t){
		log(t, null, Log.ERRORS);
	}
	
	public static void trace(String msg){
		StringBuffer mark = new StringBuffer(100);
		mark.append("--TRACE: "); //$NON-NLS-1$
		mark.append(new TimeTool().toString(TimeTool.FULL_GER));
		mark.append(": ").append(msg); //$NON-NLS-1$
		out.trace(mark.toString());
	}
	
	public boolean isDebug(){
		return DEBUGMSG <= LogLevel;
	}
	
	public boolean isInfo(){
		return INFOS <= LogLevel;
	}
	
	public boolean isWarn(){
		return WARNINGS <= LogLevel;
	}
	
	public boolean isError(){
		return ERRORS <= LogLevel;
	}
	
	private Log(){
		if (out == null)
			out = LoggerFactory.getLogger(Log.class);
	}
	
	private Log(String p){
		prefix = p;
	}
	
}