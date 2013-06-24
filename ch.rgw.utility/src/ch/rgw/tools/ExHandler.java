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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Einfacher catch-all Exeption handler. Kann Exceptions anzeigen oder loggen
 * 
 * @author G. Weirich
 */

public class ExHandler {
	static final String Version(){
		return "1.6.3";
	}
	
	private static PrintStream out;
	private static String[] mine = null;
	
	private ExHandler(){}
	
	static {
		out = System.err;
	}
	
	/**
	 * Ausgabestream für Exception-Meldungen setzen
	 * 
	 * @param name
	 *            der Ausgabestream
	 */
	public static void setOutput(String name){
		if ((name == null) || (name.equals("")) || (name.equals("none"))) {
			out = System.err;
		} else if (name.equals("sysout")) {
			out = System.out;
		} else {
			try {
				File f = new File(name);
				
				if (!f.exists()) {
					f.createNewFile();
				}
				out = new PrintStream(new FileOutputStream(f, true));
			} catch (Exception ex) {
				System.err.println(Messages.getString("ExHandler.cantRedirectOutput")); //$NON-NLS-1$
			}
		}
	}
	
	/** Aktuellen Output stream lesen */
	public static PrintStream output(){
		return out;
	}
	
	/**
	 * Interessierende Klassen setzen (Präfixe). (Nur die Klassen mit dieser Präfix werden im
	 * Stack-Trace ausgegeben. Wenn keine angegeben werden, werden alle angezeigt.
	 * 
	 * @param interest
	 *            Alle interessierenden Klassen.
	 */
	public static void setClasses(String[] interest){
		mine = interest;
	}
	
	/**
	 * Exception behandelt. Gibt standardmässig die Exeptions-Klasse, die message der Exception und
	 * einen Stack-Trace der interessierenden Klassen aus.
	 * 
	 * @param ex
	 *            die Exception
	 */
	public static void handle(Throwable ex){ // synchronized(out)
		out.flush();
		out.println("--------------Exception--------------");
		ex.printStackTrace(out);
		out.println("-----------End Exception handler-----");
		out.flush();
	}
}
