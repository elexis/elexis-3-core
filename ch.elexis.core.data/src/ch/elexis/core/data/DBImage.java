/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
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

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.tools.StringTool;

/**
 * A DBImage is an Image stored in the database and retrievable by its name
 * 
 * @author gerry
 * 
 */
public class DBImage extends PersistentObject {
	public static final String DEFAULT_PREFIX = "ch.elexis.images";
	public static final String FLD_PREFIX = "Prefix";
	private static final String FLD_TITLE = "Titel";
	public static final String DATE = "Datum";
	public static final String FLD_IMAGE = "Bild";
	public static final String DBVERSION = "1.0.0";
	public static final String TABLENAME = "DBIMAGE";
	
	static {
		addMapping(TABLENAME, DATE_COMPOUND, FLD_PREFIX, "Titel=Title", FLD_IMAGE);
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		synchronized (sb) {
			sb.append(get(DATE)).append(" - ").append(get(FLD_TITLE))
				.append(StringConstants.OPENBRACKET).append(get(FLD_PREFIX))
				.append(StringConstants.CLOSEBRACKET);
			return sb.toString();
		}
	}
	
	public String getName(){
		return get(FLD_TITLE);
	}
	
	public DBImage(String prefix, final String name){
		create(null);
		
		if (StringTool.isNothing(prefix)) {
			prefix = DEFAULT_PREFIX;
		}
		
		set(new String[] {
				FLD_PREFIX, FLD_TITLE
			}, prefix, name);
	}
	
	public static DBImage find(String prefix, String name){
		Query<DBImage> qbe = new Query<DBImage>(DBImage.class);
		if (StringTool.isNothing(prefix)) {
			prefix = DEFAULT_PREFIX;
		}
		qbe.add(FLD_PREFIX, Query.EQUALS, prefix);
		qbe.add(FLD_TITLE, Query.EQUALS, name);
		List<DBImage> ret = qbe.execute();
		if (ret != null && ret.size() > 0) {
			return ret.get(0);
		}
		return null;
	}
	
	public static DBImage load(String id){
		DBImage ret = new DBImage(id);
		if (!ret.exists()) {
			return null;
		}
		return ret;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	protected DBImage(String id){
		super(id);
	}
	
	protected DBImage(){}
	
}
