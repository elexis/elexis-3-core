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
package ch.elexis.data;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.rgw.io.Resource;
import ch.rgw.tools.JdbcLink;

/**
 * Metainformtionen über die Datenbank beschaffen
 * 
 * @author Gerry
 * 
 */
@SuppressWarnings("unchecked")
public class DBInfo {
	static Hashtable dbFields;
	static {
		dbFields = new Hashtable(200, 0.7f);
		Resource rsc = new Resource("ch.elexis.data");
		InputStream is = rsc.getInputStream("createDB.script");
		String sql;
		Pattern table = Pattern.compile("CREATE.+?TABLE\\s+([a-zA-Z0-9_]+)\\s*\\((.+)"); // (.+)\\);.*");
		Pattern line = Pattern.compile("([a-zA-Z_0-9]+).+\\(([0-9]+)\\).*");
		while ((sql = JdbcLink.readStatement(is)) != null) {
			Matcher m = table.matcher(sql);
			if (m.matches()) {
				String name = m.group(1);
				String flesh = m.group(2);
				String[] fields = flesh.split(",");
				for (String f : fields) {
					Matcher l = line.matcher(f.trim());
					if (l.matches()) {
						dbFields.put(name.toLowerCase() + "#" + l.group(1).toLowerCase(),
							Integer.parseInt(l.group(2)));
					}
				}
			}
		}
	}
	
	/**
	 * Länge eines Textfelds holen
	 * 
	 * @param table
	 *            Tabellenname
	 * @param field
	 *            Feldname
	 * @return die Länge oder 0 wenn das Feld nicht existiert oder unlimitiert ist.
	 */
	public static int getFieldLength(String table, String field){
		String key = table.toLowerCase() + "#" + field.toLowerCase();
		Integer ret = (Integer) dbFields.get(key);
		if (ret == null) {
			return 0;
		}
		return ret.intValue();
		
	}
	
	/**
	 * Fragen, ob ein Feld existiert
	 * 
	 * @param table
	 * @param field
	 * @return
	 */
	public static boolean fieldExists(String table, String field){
		String key = table.toLowerCase() + "#" + field.toLowerCase();
		return dbFields.containsKey(key);
	}
}
