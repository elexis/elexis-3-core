/*******************************************************************************
 * Copyright (c) 2005-2014, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    T. Huster - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.VersionInfo;

public class FreeTextDiagnose extends PersistentObject implements IDiagnose {
	
	private static Logger logger = LoggerFactory.getLogger(FreeTextDiagnose.class);
	
	public static final String TABLENAME = "at_medevit_elexis_freetextdiagnose"; //$NON-NLS-1$
	public static final String VERSION = "1.0.0"; //$NON-NLS-1$
	
	public static final String VERSIONID = "VERSION"; //$NON-NLS-1$
	
	public static final String FLD_TEXT = "text"; //$NON-NLS-1$
	
	// @formatter:off
	static final String create = 
			"CREATE TABLE " + TABLENAME + " (" + //$NON-NLS-1$ //$NON-NLS-2$
			"ID VARCHAR(25) primary key, " + //$NON-NLS-1$
			"lastupdate BIGINT," + //$NON-NLS-1$
			"deleted CHAR(1) default '0'," + //$NON-NLS-1$
			
			"text VARCHAR(255)" + //$NON-NLS-1$
			");" + //$NON-NLS-1$
			"INSERT INTO " + TABLENAME + " (ID," + FLD_TEXT + ") VALUES (" + JdbcLink.wrap(VERSIONID) + "," + JdbcLink.wrap(VERSION) + ");"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	// @formatter:on
	
	static {
		addMapping(TABLENAME, FLD_TEXT);
		
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(create);
		} else {
			FreeTextDiagnose version = load(VERSIONID);
			VersionInfo vi = new VersionInfo(version.get(FLD_TEXT));
			if (vi.isOlder(VERSION)) {
				// we should update eg. with createOrModifyTable(update.sql);
				// And then set the new version
				version.set(FLD_TEXT, VERSION);
			}
		}
	}
	
	public FreeTextDiagnose(){}
	
	private FreeTextDiagnose(String id){
		super(id);
	}
	
	public static FreeTextDiagnose load(final String id){
		return new FreeTextDiagnose(id);
	}
	
	public FreeTextDiagnose(String text, boolean create){
		create(null);
		set(FLD_TEXT, text);
	}
	
	@Override
	public String getLabel(){
		String ret = getText();
		if (ret.length() > 80) {
			ret = ret.substring(0, 77) + "..."; //$NON-NLS-1$
		}
		return ret;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public String getCodeSystemName(){
		return "freetext"; //$NON-NLS-1$
	}
	
	@Override
	public String getCodeSystemCode(){
		return "freetext"; //$NON-NLS-1$
	}
	
	@Override
	public String getCode(){
		return getId();
	}
	
	@Override
	public String getText(){
		return get(FLD_TEXT);
	}
	
	@Override
	public List<Object> getActions(Object context){
		// TODO Auto-generated method stub
		return null;
	}
	
}
