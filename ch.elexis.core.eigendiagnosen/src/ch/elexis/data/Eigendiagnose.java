/*******************************************************************************
 * Copyright (c) 2007-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/
package ch.elexis.data;

import java.util.List;

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VersionInfo;
import ch.elexis.core.eigendiagnosen.Messages;

public class Eigendiagnose extends PersistentObject implements IDiagnose {
	static final String VERSION = "0.1.1";//$NON-NLS-1$
	static final String TABLENAME = "CH_ELEXIS_EIGENDIAGNOSEN";//$NON-NLS-1$
	
	public static final String FLD_CODE = "Kuerzel";//$NON-NLS-1$
	public static final String FLD_TEXT = "Text";//$NON-NLS-1$
	public static final String FLD_COMMENT = "Kommentar";//$NON-NLS-1$
	
	public static final String CODESYSTEM_CODE = "ED";//$NON-NLS-1$
	public static final String CODESYSTEM_NAME = "Eigendiagnosen";//$NON-NLS-1$
	private static final String createDB = "CREATE TABLE " + TABLENAME + //$NON-NLS-1$
		"(" + //$NON-NLS-1$ 
		"ID  VARCHAR(25) primary key," + //$NON-NLS-1$
		"lastupdate   BIGINT," + //$NON-NLS-1$
		"deleted      CHAR(1) default '0'," + //$NON-NLS-1$
		"parent       VARCHAR(20)," + //$NON-NLS-1$
		"code         VARCHAR(20)," + //$NON-NLS-1$
		"title        VARCHAR(80)," + //$NON-NLS-1$
		"comment      TEXT," + //$NON-NLS-1$
		"ExtInfo      BLOB);" + //$NON-NLS-1$
		"CREATE INDEX " + TABLENAME + //$NON-NLS-1$
		"_idx1 on " + TABLENAME + "(parent,code);" + //$NON-NLS-1$//$NON-NLS-2$
		"INSERT INTO " + TABLENAME + " (ID,title) VALUES ('VERSION','" + VERSION + "');";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	
	/**
	 * Here we define the mapping between internal fieldnames and database fieldnames. (@see
	 * PersistentObject) then we try to load a version element. If this does not exist, we create
	 * the table. If it exists, we check the version
	 */
	static {
		addMapping(TABLENAME, "parent", FLD_TEXT + "=title", FLD_CODE + "=code", FLD_COMMENT//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			+ "=comment", PersistentObject.FLD_EXTINFO);//$NON-NLS-1$
		Eigendiagnose check = load("VERSION");//$NON-NLS-1$
		if (check.state() < PersistentObject.DELETED) { // Object never existed, so we have to
			// create the database
			initialize();
		} else { // found existing table, check version
			VersionInfo v = new VersionInfo(check.get(FLD_TEXT));
			if (v.isOlder("0.1.1")) {//$NON-NLS-1$
				createOrModifyTable("ALTER TABLE " + TABLENAME + " ADD lastupdate BIGINT;");//$NON-NLS-1$//$NON-NLS-2$
				check.set(FLD_TEXT, VERSION);
				
			}
		}
	}
	
	public Eigendiagnose(String parent, String code, String text, String comment){
		create(null);
		set(new String[] {
			"parent", FLD_CODE, FLD_TEXT, FLD_COMMENT//$NON-NLS-1$
		}, new String[] {
			parent == null ? "NIL" : parent, code, text, comment//$NON-NLS-1$
			});
	}
	
	public static void initialize(){
		createOrModifyTable(createDB);
	}
	
	@Override
	public String getLabel(){
		return get(FLD_CODE) + StringTool.space + get(FLD_TEXT);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Eigendiagnose load(String id){
		return new Eigendiagnose(id);
	}
	
	protected Eigendiagnose(String id){
		super(id);
	}
	
	protected Eigendiagnose(){}
	
	@Override
	public List<Object> getActions(Object context){
		return null;
	}
	
	public String getCode(){
		return getId();
	}
	
	public String getCodeSystemCode(){
		return CODESYSTEM_CODE;
	}
	
	public String getCodeSystemName(){
		return Messages.Eigendiagnosen_CodeSystemName;
	}
	
	public String getText(){
		return get(FLD_TEXT);
	}
	
	@Override
	public boolean isDragOK(){
		return !hasChildren();
	}
	
	public boolean hasChildren(){
		JdbcLink link = PersistentObject.getConnection();
		String theText = get(FLD_TEXT);
		int numOfChildren = link.queryInt("SELECT count(*) FROM " + TABLENAME//$NON-NLS-1$
			+ " WHERE deleted = '0' AND parent = " + JdbcLink.wrap(theText));//$NON-NLS-1$
		if (numOfChildren > 0) {
			return true;
		}
		return false;
	}
	
}
