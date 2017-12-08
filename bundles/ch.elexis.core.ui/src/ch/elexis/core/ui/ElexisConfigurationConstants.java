/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ch.elexis.core.ui.views.RezeptBlatt;

/**
 * <b>Poor mans dependency injection.</b> This file allows for the injection of an external
 * configuration. There are certain parts in code which are "hard-wired" for the application of
 * certain tasks. These may however be necessary to be overriden, if e.g. someone wants to select a
 * specific output plugin. <br>
 * <br>
 * If a certain code part is modified for external configuration it is marked with<br>
 * <code>//PMDI - Dependency Injection through {@link ElexisConfigurationConstants}</code><br>
 * so search for occurences of this, to find respective code parts. <br>
 * <br>
 * If you want to apply this, create a fragment with ch.elexis as host, that has the file
 * ElexisConfigurationConstants.properties in its root, with the respective configuration parameter.
 * If no such configuration file is found, Elexis simply sticks to its default behaviour!<br>
 * 
 * <b>Why?</b> We need to find a separation for instantiation of country specific implementations!
 * 
 * @author MEDEVIT - office AT medevit DOT at
 * 
 */
public class ElexisConfigurationConstants {
	public static String CONFIG_FILE_NAME = "ElexisConfigurationConstants.properties";
	public static boolean extConfigFile = false;
	private static Properties properties = null;
	
	/**
	 * Rezeptausgabe Selektion
	 */
	public static String rezeptausgabe = RezeptBlatt.ID;
	
	public static boolean init(){
		InputStream istream =
			ElexisConfigurationConstants.class.getClassLoader().getResourceAsStream(
				CONFIG_FILE_NAME);
		if (istream != null) {
			properties = new Properties();
			try {
				properties.load(istream);
				istream.close();
				extConfigFile = true;
				rezeptausgabe = properties.getProperty("rezeptausgabe");
				System.out
					.println("ATTENTION: External configuration file injected, overriding defaults!");
				
			} catch (IOException e) {
				extConfigFile = false;
				return extConfigFile;
			}
		}
		return extConfigFile;
	}
	
}
