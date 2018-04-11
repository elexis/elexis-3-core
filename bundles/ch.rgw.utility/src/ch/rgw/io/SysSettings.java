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

package ch.rgw.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Settings-Implementation, die ein "Systemtypisches" Verfahren zur Speicherung verwendet. Unter
 * Windows ist das die Registry, unter Linux eine .datei in XML-Format. Es wird unterschieden
 * zwischen anwendnerspezifischen Settings (USER_SETTINGS) und systemweiten Settings
 * (SYSTEM_SETTINGS)
 */

public class SysSettings extends Settings {
	private static final long serialVersionUID = -7855039763450972263L;
	
	public static final String Version(){
		return "1.0.2";
	}
	
	public static final int USER_SETTINGS = 0;
	public static final int SYSTEM_SETTINGS = 1;
	volatile int typ;
	volatile Class clazz;
	
	/**
	 * Settings neu Anlegen oder einlesen
	 * 
	 * @param type
	 *            USER_SETTINGS oder SYSTEM_SETTINGS
	 * @param cl
	 *            Basisklasse für den Settings-zweig
	 */
	public SysSettings(int type, Class cl){
		super();
		typ = type;
		clazz = cl;
		undo();
	}
	
	private Preferences getRoot(){
		Preferences pr = null;
		if (typ == USER_SETTINGS) {
			pr = Preferences.userNodeForPackage(clazz);
		} else {
			pr = Preferences.systemNodeForPackage(clazz);
		}
		String[] nodes = (getPath().split("/"));
		Preferences sub = pr;
		// Preferences[] plist=new Preferences[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			sub = sub.node(nodes[i]);
		}
		return sub;
	}
	
	/**
	 * Diese Settings als XML-Datei exportieren
	 * 
	 * @param file
	 *            Dateiname
	 * @throws Exception
	 */
	public void write_xml(String file){
		String errMsg = "\nSysSettings: Error writing: " + file;
		try {
			FileOutputStream os = new FileOutputStream(file);
			getRoot().exportSubtree(os);
			os.close();
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage() + errMsg);
		} catch (BackingStoreException e) {
			log.warn(e.getMessage() + errMsg);
		} catch (IOException e) {
			log.warn(e.getMessage() + errMsg);
		}
	}
	
	/**
	 * Settings aus XML-Datei importieren
	 * 
	 * @param file
	 *            Dateiname
	 * @throws Exception
	 */
	public void read_xml(String file){
		FileInputStream is;
		String errMsg = "\nSysSettings: Error reading: " + file;
		try {
			is = new FileInputStream(file);
			Preferences.importPreferences(is);
			is.close();
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage() + errMsg);
		} catch (InvalidPreferencesFormatException e) {
			log.warn(e.getMessage() + errMsg);
		} catch (IOException e) {
			log.warn(e.getMessage() + errMsg);
		}
	}
	
	/**
	 * @see ch.rgw.IO.Settings#flush()
	 */
	protected void flush_absolute(){
		Iterator it = iterator();
		Preferences pr = getRoot();
		while (it.hasNext()) {
			String a = (String) it.next();
			String[] nodes = a.split("/");
			String key = nodes[nodes.length - 1];
			Object value = get(a, null);
			Preferences sub = pr;
			Preferences[] plist = new Preferences[nodes.length];
			for (int i = 0; i < plist.length - 1; i++) {
				sub = sub.node(nodes[i]);
			}
			if (StringTool.isNothing(value)) {
				sub.remove(key);
				if(getSettingChangedListener() != null) {
					getSettingChangedListener().settingRemoved(key);
				}
			} else {
				sub.put(key, (String) value);
				if(getSettingChangedListener() != null) {
					getSettingChangedListener().settingWritten(key, (String) value);
				}
			}
		}
		try {
			pr.flush();
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
	}
	
	public void undo(){
		clear();
		loadTree(getRoot(), "");
		
	}
	
	private void loadTree(Preferences root, String path){
		try {
			String[] subnodes = root.childrenNames();
			path = path.replaceFirst("^/", "");
			for (int s = 0; s < subnodes.length; s++) {
				Preferences sub = root.node(subnodes[s]);
				loadTree(sub, path + "/" + subnodes[s]);
			}
			String[] keys = root.keys();
			for (int i = 0; i < keys.length; i++) {
				if (path.equals(""))
					set(keys[i], root.get(keys[i], ""));
				else
					set(path + "/" + keys[i], root.get(keys[i], ""));
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		
	}
}