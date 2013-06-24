/*******************************************************************************
 * Copyright (c) 2006-2012, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    N. Giger   - porting to slf4j and qos.logback
 *
 *    
 *******************************************************************************/
package ch.rgw.tools;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class Log {
	
	/** Experimentell */
	public static final int NOTHING = 0;
	/** Fatale Fehler, Programmabbruch */
	public static final int FATALS = 1;
	/** Nichtfatale Fehler, Programm kann weiterlaufen */
	public static final int ERRORS = 2;
	/** Warnung, Programm läuft normal weiter, es könnten aber Probleme auftreten */
	public static final int WARNINGS = 3;
	/** Reine Informationen, kein Einfluss aufs Programm */
	public static final int INFOS = 4;
	/** Für Debugzwecke gedachte Meldungen */
	public static final int DEBUGMSG = 5;
	/** Immer auszugebende Meldungen, die aber keinem Fehler entsprechen */
	public static final int TRACE = 6;
	/** Immer auszugebende Meldungen, automatisch mit einem Timestamp versehen */
	public static final int SYNCMARK = -1;
	private static Logger l;
	private static boolean printedConf;
	
	private static void internalLogging(Level level, String msg){
		if (level == Level.SEVERE)
			l.error(msg);
		else if (level == Level.WARNING)
			l.warn(msg);
		else if (level == Level.INFO)
			l.info(msg);
		else if (level == Level.WARNING)
			l.warn(msg);
		else
			l.debug(msg);
	}
	
	public static Log get(String name){
		l = LoggerFactory.getLogger(name);
		return new Log();
	}
	
	public void log(String message, int level){
		internalLogging(translate(level), message);
	}
	
	public void log(Level level, String message){
		internalLogging(level, message);
	}
	
	private Level translate(int logLevel){
		switch (logLevel) {
		case NOTHING:
			return Level.FINEST;
		case FATALS:
			return Level.SEVERE;
		case ERRORS:
			return Level.SEVERE;
		case WARNINGS:
			return Level.WARNING;
		case INFOS:
			return Level.INFO;
		case DEBUGMSG:
			return Level.FINE;
		case TRACE:
			return Level.ALL;
		case SYNCMARK:
			return Level.ALL;
		default:
			return Level.ALL;
		}
	}
	
}
