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
package ch.elexis.data;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.compress.CompEx;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.TimeTool;

/**
 * A named Blob is just that: An arbitrarly named piece of arbitrary data. The name must be unique
 * (among NamedBlobs). We provide methods to store and retrieve data as Hashtables and Strings (Both
 * will be stored in zip-compressed form)
 * 
 * @author Gerry
 * 
 */
public class NamedBlob extends PersistentObject {
	
	public static final String TABLENAME = "HEAP";
	public static final String CONTENTS = "inhalt";
	
	/**
	 * return the contents as Hashtable (will probably fail if the data was not stored using
	 * put(Hashtable)
	 * 
	 * @return the previously stored Hashtable
	 */
	@SuppressWarnings("unchecked")
	// TODO weird
	public Hashtable getHashtable(){
		return (Hashtable) getMap(CONTENTS);
	}
	
	/**
	 * Put the contents as Hashtable. The Hashtable will be compressed
	 * 
	 * @param in
	 *            a Hashtable
	 */
	@SuppressWarnings("unchecked")
	public void put(final Hashtable in){
		setMap(CONTENTS, in);
		set("Datum", new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	/**
	 * return the contents as String (will probably fail if the data was not stored using putString)
	 * 
	 * @return the previously stored string.
	 */
	public String getString(){
		byte[] comp = getBinary(CONTENTS);
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
		setBinary(CONTENTS, comp);
		set("Datum", new TimeTool().toString(TimeTool.DATE_GER));
	}
	
	@Override
	public String getLabel(){
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	static {
		addMapping(TABLENAME, CONTENTS, "Datum=S:D:datum");
	}
	
	/**
	 * Ask if this NamedBlob exists
	 * 
	 * @param id
	 *            the unique name of the NamedBlob to query
	 * @return true if a NamedBlob with this name exists
	 */
	public static boolean exists(final String id){
		StringBuilder path = new StringBuilder();
		path.append(CoreHub.getTempDir());
		path.append(File.separator);
		String idr = id.replaceAll("\\:", "\\\\");
		path.append(idr);
		File file = new File(path.toString());
		if (file.exists()) {
			return true;
		}
		NamedBlob ni = new NamedBlob(id);
		return ni.exists();
	}
	
	/**
	 * Load or create a NamedBlob with a given Name. Caution: This will never return an inexistent
	 * NamedBlob, because it will be created if necessary. Use exists() to check, whether a
	 * NamedBlob exists.
	 * 
	 * @param id
	 *            the unique name
	 * @return the NamedBlob with that Name (which may be just newly created)
	 */
	public static NamedBlob load(final String id){
		NamedBlob ni = new NamedBlob(id);
		if (ni.state() < DELETED) {
			if (!ni.create(id)) {
				return null;
			}
			File file = new File("c:\\temp" + File.separator + id.replaceAll(":", "\\\\"));
			if (file.exists()) {
				String fi = null;
				try {
					fi = FileTool.readTextFile(file);
					ni.putString(fi);
				} catch (IOException e) {
					ExHandler.handle(e);
				}
			}
		}
		return ni;
	}
	
	protected NamedBlob(){};
	
	protected NamedBlob(final String id){
		super(id);
	}
	
	/**
	 * find all NamedBlox with a name with a given prefix. Muxh faster than findSimilar.
	 * 
	 * @param prefix
	 *            the orefix to look for
	 * @return list with all NamedBlobs wohse name matches (case sensitively) the prefix
	 */
	public static List<NamedBlob> findFromPrefix(final String prefix){
		Query<NamedBlob> qbe = new Query<NamedBlob>(NamedBlob.class);
		qbe.add("ID", "Like", prefix + "%");
		return qbe.execute();
	}
	
	/**
	 * Find all namedBlobs whose name match the given regular expression
	 * 
	 * @param name
	 *            a regular expression to match
	 * @return a list of all NamedBlobs with a name that matches the regular expression
	 */
	public static List<NamedBlob> findSimilar(final String name){
		Query<NamedBlob> qbe = new Query<NamedBlob>(NamedBlob.class);
		qbe.addPostQueryFilter(new IFilter() {
			public boolean select(Object toTest){
				NamedBlob nb = (NamedBlob) toTest;
				if (nb.getId().matches(name)) {
					return true;
				}
				return false;
			}
			
		});
		return qbe.execute();
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
			Query<NamedBlob> qbe = new Query<NamedBlob>(NamedBlob.class);
			qbe.add("Datum", "<", older.toString(TimeTool.DATE_COMPACT));
			for (NamedBlob nb : qbe.execute()) {
				if (nb.getId().startsWith(prefix)) {
					nb.delete();
				}
			}
		}
	}
	
	public static void createTable(){
		String create =
			"CREATE TABLE HEAP(" + "ID			VARCHAR(80) primary key,"
				+ "deleted		CHAR(1) default '0'," + "inhalt		BLOB," + "datum		CHAR(8),"
				+ "lastupdate   BIGINT" + ");";
		createOrModifyTable(create);
	}
}
