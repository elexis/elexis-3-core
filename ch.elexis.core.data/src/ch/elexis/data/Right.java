/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.data;

import java.util.Locale;

import ch.elexis.admin.ACE;
import ch.elexis.core.jdt.NonNull;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * 
 *  @since 3.1
 */
public class Right extends PersistentObject {
	
	public static final String TABLENAME = "RIGHT_";
	public static final String FLD_NAME = "NAME";
	public static final String FLD_I18N_NAME = "I18N_NAME";
	public static final String FLD_PARENTID = "PARENTID";
	public static final String FLD_TREEPATH = "TREEPATH";
	
	public static final String FLD_EXT_I18N_LABEL = "LAB_" + Locale.getDefault().getLanguage();
	
	static {
		addMapping(TABLENAME, FLD_NAME, FLD_PARENTID, FLD_TREEPATH, FLD_I18N_NAME);
	}
	
	protected Right(){}
	
	protected Right(final String id){
		super(id);
	}
	
	protected Right(final ACE ace){
		create(ace.getUniqueHashFromACE());
		set(FLD_NAME, ace.getName());
		
		Right parentRight = Right.getOrCreateRightByACE(ace.getParent());
		set(FLD_PARENTID, parentRight.getId());
		setTranslatedLabel(ace.getLocalizedName());
	}
	
	public static Right load(final String id){
		return new Right(id);
	}
	
	public static @NonNull Right getOrCreateRightByACE(ACE ace){
		Right right = Right.load(ace.getUniqueHashFromACE());
		if (right.exists())
			return right;
		return new Right(ace);
	}
	
	/**
	 * Reset the table, effectively removing all rights
	 */
	public static void resetTable(){
		Stm stm = getConnection().getStatement();
		stm.exec("DELETE FROM " + TABLENAME + " WHERE ID NOT EQUALS 'root'");
		getConnection().releaseStatement(stm);
	}
	
	@Override
	public String getLabel(){
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public String getTranslatedLabel(){
		return get(FLD_I18N_NAME);
	}
	
	public void setTranslatedLabel(String translatedLabel){
		set(FLD_I18N_NAME, translatedLabel);
	}
}
