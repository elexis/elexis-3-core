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

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Abstrakte Basisklasse für die Speicherung applikationsspezifischer Daten.<br>
 * Änderungen der Settings sind immer volatil. Erst mit dem Auruf von flush() erfolgt eine
 * persistierende Speicherung (deren genaues Ziel Sache der konkreten Implementation ist. mit undo()
 * kann jeweils der Stand nach dem letzten flush() wiederhergestellt werden. Es erfolgt euch keine
 * implizite Speicherung beim Programmende! Alle Änderungen, die nicht explizit mit flush()
 * gesichert werden, sind verloren.
 */

public abstract class Settings implements Serializable, Cloneable {
	public static String Version(){
		return "4.2.2";
	}
	
	private static int SerializedVersion = 5;
	private static final long serialVersionUID = 0xdcb17fe20021006L + SerializedVersion;
	protected static Logger log = null;
	protected Hashtable node;
	private volatile String path = null;
	private volatile boolean dirty = false;
	private ISettingChangedListener settingChangedListener = null;
	// protected String name;
	
	static {
		if (log == null)
			log = LoggerFactory.getLogger("Settings"); //$NON-NLS-1$
	}
	
	protected Settings(){
		node = new Hashtable();
		dirty = false;
	}
	
	protected Settings(byte[] flat){
		node = StringTool.fold(flat, StringTool.NONE, null);
		dirty = true;
	}
	
	protected Settings(Hashtable n){
		node = (n == null) ? new Hashtable() : n;
		dirty = true;
	}
	
	public void setSettingChangedListener(ISettingChangedListener settingChangedListener){
		this.settingChangedListener = settingChangedListener;
	}
	
	public ISettingChangedListener getSettingChangedListener(){
		return settingChangedListener;
	}
	
	protected void cleaned(){
		dirty = false;
	}
	
	public boolean isDirty(){
		return dirty;
	}
	
	public String toString(){
		return StringTool.enPrintable(StringTool.flatten(node, StringTool.NONE, null));
	}
	
	protected String getPath(){
		if (path == null) {
			return "";
		}
		return path;
	}
	
	/*
	 * protected void finalize() { flush(); }
	 */
	public void clear(){
		node.clear();
		dirty = true;
	}
	
	public double get(String key, double defvalue){
		String res = get(key, null);
		if (res == null) {
			return defvalue;
		}
		try {
			return Double.parseDouble(res);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.error("Parse fehler für Double " + res);
			return defvalue;
		}
	}
	
	public String get(String key, String defvalue){
		Hashtable subnode = findParent(key, false);
		if (subnode == null) {
			return defvalue;
		}
		Object v = subnode.get(getLeaf(key));
		return (StringTool.isNothing(v)) ? defvalue : (String) v;
	}
	
	@SuppressWarnings("unchecked")
	public boolean set(String key, String value){
		if ((key == null) || (value == null)) {
			return false;
		}
		Hashtable subnode = findParent(key, true);
		dirty = true;
		if(settingChangedListener!=null) {
			settingChangedListener.settingChanged(key, value);
		}
		return (subnode.put(getLeaf(key), value) != null);
	}
	
	private String getLeaf(String key){
		int id = key.lastIndexOf('/');
		if (id != -1) {
			String leaf = key.substring(id + 1);
			return leaf;
		}
		return key;
	}
	
	@SuppressWarnings("unchecked")
	private Hashtable findParent(String key, boolean CreateIfNeeded){
		String[] path1 = key.split("/");
		Hashtable subnode = node;
		for (int i = 0; i < path1.length - 1; i++) {
			Object v = subnode.get(path1[i]);
			if ((v == null) || (!(v instanceof Hashtable))) {
				if (CreateIfNeeded == true) {
					v = new Hashtable();
					subnode.put(path1[i], v);
					dirty = true;
				} else {
					return null;
				}
			}
			subnode = (Hashtable) v;
		}
		return subnode;
	}
	
	@SuppressWarnings("unchecked")
	public String[] keys(String nod){
		Settings sn = getBranch(nod, false);
		if (sn == null) {
			return null;
		}
		ArrayList al = sn.keys();
		return (String[]) al.toArray(new String[0]);
	}
	
	@SuppressWarnings("unchecked")
	public String[] nodes(String nod){
		Settings sn = getBranch(nod, false);
		if (sn == null) {
			return null;
		}
		ArrayList al = sn.nodes();
		return (String[]) al.toArray(new String[0]);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList keys(){
		Enumeration en = node.keys();
		
		ArrayList dest = new ArrayList();
		while (en.hasMoreElements()) {
			Object k = en.nextElement();
			if (node.get(k) instanceof Hashtable) {
				continue;
			}
			dest.add(k);
		}
		return dest;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList nodes(){
		Enumeration en = node.keys();
		ArrayList dest = new ArrayList();
		while (en.hasMoreElements()) {
			Object k = en.nextElement();
			if (node.get(k) instanceof Hashtable) {
				dest.add(k);
			}
		}
		return dest;
	}
	
	/**
	 * Einen Zweig dieser Settings holen oder erstellen.<br>
	 * Die Implementation ist Sache der Unterklasse. In der Registry kann ein Zweig direkt auf einem
	 * Zweig abgebildet werden, ein XML-File kann einen entsprechenden Node erstellen, ein Flatfile
	 * wird einfach Einträge des Typs präfix/eintrag erstellen.
	 * 
	 * @param name
	 *            Der Name des Zweigs
	 * @param CreateIfNotExist
	 *            Der Zweig wird erstellt, wenn er noch nicht existiert
	 * @return ein neues Settings-Object, das den Zweig repräsentiert
	 */
	@SuppressWarnings("unchecked")
	public Settings getBranch(String name, boolean CreateIfNotExist){
		Hashtable parent = findParent(name, CreateIfNotExist);
		if (parent == null) {
			return null;
		}
		String id = getLeaf(name);
		Object k = parent.get(id);
		if ((k == null) || (!(k instanceof Hashtable))) {
			if (CreateIfNotExist) {
				k = new Hashtable();
				parent.put(id, k);
				dirty = true;
			} else {
				return null;
			}
		}
		try {
			Settings n = (Settings) this.clone();
			if (path == null) {
				n.path = name + "/";
			} else {
				n.path = path + name + "/";
			}
			n.node = (Hashtable) k;
			return n;
			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			ExHandler.handle(e);
			return null;
		}
	}
	
	public ArrayList<String> getAll(){
		ArrayList<String> ret = new ArrayList<String>();
		addNode(ret, "", node);
		return ret;
	}
	
	public Iterator<String> iterator(){
		ArrayList<String> al = getAll();
		Iterator<String> it = al.iterator();
		return it;
	}
	
	@SuppressWarnings("unchecked")
	private void addNode(ArrayList dest, String name, Hashtable n){
		Enumeration en = n.keys();
		while (en.hasMoreElements()) {
			String k = (String) en.nextElement();
			Object o = n.get(k);
			if (o instanceof Hashtable) {
				addNode(dest, name + k + "/", (Hashtable) o);
			} else {
				dest.add(name + k);
			}
		}
	}
	
	/**
	 * Einen Fingerprint über alle Einträge erstellen.<br>
	 * 
	 * @param ex
	 *            Eintrag, welcher nicht einbezogen wird
	 * @return den hashcode
	 * @todo Dies sollte auf einen MD5 oder SHA-hash umgstellt werden
	 */
	private long getHashCode(String ex){
		long hc = 0;
		ArrayList<String> keys = getAll();
		for (int i = 0; i < keys.size(); i++) {
			String a = (String) keys.get(i);
			if (a.equals(ex)) {
				continue;
			}
			String b = get(a, "");
			hc += b.hashCode();
			hc <<= 2;
			if (hc < 0)
				hc |= 1;
		}
		return hc;
	}
	
	public long createHashCode(String ex){
		long ret = getHashCode(ex);
		set(ex, Long.toString(ret));
		return ret;
	}
	
	/**
	 * Den mit createHashCode erstellten Fingerprint überprüfen.
	 * 
	 * @param ex
	 *            Eintrag, der den hashcode enthült
	 * @return true bei übereinstimmung
	 */
	public boolean checkHashCode(String ex){
		long oldval = Long.parseLong(get(ex, "-1"));
		long nval = getHashCode(ex);
		return (nval == oldval);
	}
	
	/**
	 * Einen Integer-Wert setzen.
	 * 
	 * @param key
	 *            Schlüssel
	 * @param value
	 *            Wert
	 */
	public void set(String key, int value){
		set(key, Integer.toString(value));
	}
	
	/**
	 * Ein Rechteck eintragen.
	 * 
	 * @param key
	 *            Schlüssel
	 * @param rec
	 *            Wert
	 */
	public void set(String key, Rectangle rec){
		if (rec == null)
			return;
		String v =
			Integer.toString(rec.x) + "," + Integer.toString(rec.y) + ","
				+ Integer.toString(rec.width) + "," + Integer.toString(rec.height);
		set(key, v);
	}
	
	/**
	 * Einen Datum/Zeitwert eintragen.
	 * 
	 * @param key
	 *            Schlüssel
	 * @param d
	 *            Datum/Zeit als ch.rgw.tools.timeTool
	 */
	public void set(String key, TimeTool d){
		set(key, d.toString(TimeTool.FULL_MYSQL));
	}
	
	/**
	 * Einen Schlüssel entfernen.
	 * 
	 * @param key
	 *            der Schlüssel
	 */
	public void remove(String key){
		Hashtable p = findParent(key, false);
		if (p != null) {
			if(settingChangedListener!=null) {
				settingChangedListener.settingRemoved(key);
			}
			p.remove(getLeaf(key));
			dirty = true;
		}
	}
	
	/**
	 * Einen Integerwert auslesen.
	 * 
	 * @param key
	 *            Schlüssel
	 * @param defvalue
	 *            Defaultwert, falls der Schlüssel nicht existiert
	 * @return der Wert resp. der Defaultwet
	 */
	public int get(String key, int defvalue){
		String v = get(key, Integer.toString(defvalue));
		try {
			return Integer.parseInt(v);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.debug("Int parse Fehler. Gebe Default zurück (" + defvalue + ")");
			set(key, defvalue);
			return defvalue;
		}
	}
	
	/**
	 * Einen String auslesen, dabei alle \ nach / wandeln.
	 * 
	 * @param key
	 *            Schlüssel
	 * @param defvalue
	 *            Defaultwert, falls der Schlüssel nicht existiert
	 * @return Wert
	 */
	public String getQuoted(String key, String defvalue){
		String vorl = get(key, defvalue);
		return vorl.replaceAll("\\\\", "/");
	}
	
	public String[] getStringArray(String key){
		String raw = get(key, null);
		if (StringTool.isNothing(raw)) {
			return null;
		}
		return raw.split(",");
	}
	
	/**
	 * Einen Datum/Zeitwert auslesen.
	 * 
	 * @param key
	 *            Schlüssel
	 * @return ein ch.rgw.timeTool oder null, wenn der Schlüssel nicht existiert oder ein ungültiges
	 *         Format hat.
	 */
	public TimeTool getDate(String key){
		String d = get(key, "");
		if (d.equals(""))
			return null;
		try {
			return new TimeTool(d);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Einen rechteck-Wert auslesen.
	 * 
	 * @param key
	 *            Schlüssel
	 * @return das Rectangle oder null, wenn der Schlüssel nicht existiert oder der Eintrag ein
	 *         ungültiges Format hat.
	 */
	public Rectangle get(String key){
		String v = get(key, "");
		if (v == null)
			return null;
		String[] r = v.split(",");
		if (r.length != 4)
			return null;
		
		return new Rectangle(Integer.parseInt(r[0]), Integer.parseInt(r[1]), Integer.parseInt(r[2]),
			Integer.parseInt(r[3]));
	}
	
	public boolean get(String key, boolean defvalue){
		String v = get(key, null);
		if (v == null) {
			return defvalue;
		}
		if (v.equals("1")) {
			return true;
		}
		if (v.equals("true")) {
			return true;
		}
		return false;
	}
	
	public void set(String key, boolean value){
		if (value == true) {
			set(key, "1");
		} else {
			set(key, "0");
		}
	}
	
	/**
	 * Alle Änderungen sichern. Bei Programmabbruch ohne flush werden alle Änderungen seit dem
	 * letzten flush() resp. Programmstart verworfen
	 * 
	 */
	public void flush(){
		if (dirty == true) {
			flush_absolute();
			dirty = false;
		}
	}
	
	protected abstract void flush_absolute();
	
	/**
	 * Alle Änderungen seit dem letzten flush() bzw. Programmstart verwerfen.
	 * 
	 */
	public abstract void undo();
	
	/**
	 * Ein anderes Settings-Objekt einfügen
	 */
	public static final int OVL_REPLACE = 1; // Alles löschen und aus other holen
	public static final int OVL_REPLACE_EXISTING = 2; // Existierende mit other überlagern
	public static final int OVL_ADD_MISSING = 4; // Nur fehlende aus other nehmen
	public static final int OVL_ALL = 6; // Alle Existierenden und neuen
	
	public void overlay(Settings other, int mode){
		ArrayList<String> otherEntries = other.getAll();
		
		if ((mode & OVL_REPLACE) != 0) {
			node.clear();
		}
		for (int i = 0; i < otherEntries.size(); i++) {
			String el = (String) otherEntries.get(i);
			if (get(el, null) != null) {
				if ((mode & OVL_REPLACE_EXISTING) != 0) {
					set(el, other.get(el, null));
				}
			} else {
				if ((mode & (OVL_ADD_MISSING | OVL_REPLACE)) != 0) {
					set(el, other.get(el, null));
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param key
	 * @return the results or an empty list
	 * @since 3.6
	 */
	public List<String> getAsList(String key){
		String string = get(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * 
	 * @param key
	 * @param values an empty collection will remove the resp. key
	 * @since 3.6
	 */
	public void setAsList(String key, List<String> values){
		Optional<String> value =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t);
		if (value.isPresent()) {
			set(key, value.get());
		} else {
			remove(key);
		}
	}
	
}