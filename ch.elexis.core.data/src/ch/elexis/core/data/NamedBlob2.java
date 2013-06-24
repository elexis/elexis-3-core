/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.data;

import java.util.Hashtable;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.TimeTool;


/**
 * Well, just a clone of NamedBlob, but using table HEAP2 - sort of a cheap load balancing
 * 
 * @author Gerry
 * 
 */
public class NamedBlob2 extends PersistentObject {
	public static final String FLD_DATUM = "Datum";
	public static final String FLD_CONTENTS = "Contents";
	public static final String TABLENAME = "HEAP2";
	
	/**
	 * return the contents as array of bytes
	 * 
	 * @return the contents
	 */
	public byte[] getBytes(){
		byte[] comp = getBinary(FLD_CONTENTS);
		if ((comp == null) || (comp.length == 0)) {
			return null;
		}
		return CompEx.expand(comp);
	}
	
	/**
	 * put the contents as array of bytes. the array will be stored in compressed form
	 * 
	 * @param the
	 *            contents that will override previous contents
	 * 
	 */
	public void putBytes(byte[] in){
		byte[] comp = CompEx.Compress(in, CompEx.ZIP);
		setBinary(FLD_CONTENTS, comp);
		set(FLD_DATUM, new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	/**
	 * return the contents as Hashtable (will probably fail if the data was not stored using
	 * put(Hashtable)
	 * 
	 * @return the previously stored Hashtable
	 */
	@SuppressWarnings("unchecked")
	// TODO weird
	public Hashtable getHashtable(){
		return (Hashtable) getMap(FLD_CONTENTS);
	}
	
	/**
	 * Put the contents as Hashtable. The Hashtable will be compressed
	 * 
	 * @param in
	 *            a Hashtable
	 */
	@SuppressWarnings("unchecked")
	public void put(final Hashtable in){
		setMap(FLD_CONTENTS, in);
		set(FLD_DATUM, new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	/**
	 * return the contents as String (will probably fail if the data was not stored using putString)
	 * 
	 * @return the previously stored string.
	 */
	public String getString(){
		byte[] comp = getBinary(FLD_CONTENTS);
		if ((comp == null) || (comp.length == 0)) {
			return "";
		}
		byte[] exp = CompEx.expand(comp);
		try {
			return new String(exp, "utf-8");
		} catch (Exception ex) {
			// should really not happen
			return null;
		}
	}
	
	/**
	 * Store a String. The String will be stored as compressed byte[]
	 * 
	 * @param string
	 */
	public void putString(final String string){
		byte[] comp = CompEx.Compress(string, CompEx.ZIP);
		setBinary(FLD_CONTENTS, comp);
		set(FLD_DATUM, new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	@Override
	public String getLabel(){
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return "HEAP2";
	}
	
	static {
		addMapping(TABLENAME, FLD_CONTENTS, "Datum=S:D:datum", "lastupdate");
	}
	
	/**
	 * creates or loads a NamedBlob2
	 * 
	 * @param name
	 *            the NamedBlob2 to get
	 * @param bFailIfExists
	 *            true - create if not exists, otherwise return null. false: if exists:_ return
	 *            existing
	 * 
	 */
	public static NamedBlob2 create(String name, boolean bFailIfExists){
		NamedBlob2 nb = load(name);
		if (nb == null) {
			nb = new NamedBlob2(name);
			if (nb.state() == PersistentObject.DELETED) {
				nb.undelete();
				nb.set(FLD_CONTENTS, null);
			} else {
				nb.create(name);
			}
		} else {
			if (bFailIfExists) {
				return null;
			}
		}
		return nb;
	}
	
	/**
	 * Load or create a NamedBlob with a given Name.
	 * 
	 * @return the NamedBlob with that Name or null if no such NamedBlob exists
	 */
	public static NamedBlob2 load(final String id){
		NamedBlob2 ni = new NamedBlob2(id);
		if (!ni.exists()) {
			return null;
		}
		return ni;
	}
	
	protected NamedBlob2(){};
	
	protected NamedBlob2(final String id){
		super(id);
	}
	
	/**
	 * remove all BLOBS with a given name prefix and a last write time older than the given value
	 * needs the administrative right AC_PURGE
	 * 
	 * @param prefix
	 * @param older
	 */
	public static void cleanup(final String prefix, final TimeTool older){
		if (CoreHub.acl.request(AccessControlDefaults.AC_PURGE)) {
			Query<NamedBlob2> qbe = new Query<NamedBlob2>(NamedBlob2.class);
			qbe.add(FLD_DATUM, "<", older.toString(TimeTool.DATE_COMPACT));
			for (NamedBlob2 nb : qbe.execute()) {
				if (nb.getId().startsWith(prefix)) {
					nb.delete();
				}
			}
		}
	}
}
