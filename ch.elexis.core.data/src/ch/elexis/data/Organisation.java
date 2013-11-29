/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import ch.elexis.core.constants.StringConstants;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

/**
 * Eine Organisation ist eine Kontakt, die ein Kollektiv darstellt. Also eine Firma, eine
 * Versicherung, ein Labor etc.
 * 
 * @author gerry
 * 
 */
public class Organisation extends Kontakt {
	private static final String ZUSATZ1 = "Zusatz1"; //$NON-NLS-1$
	private static final String NAME = "Name"; //$NON-NLS-1$
	public static final String[] DEFAULT_SORT = {
		NAME, ZUSATZ1
	};
	static {
		addMapping(Kontakt.TABLENAME, "Name	=Bezeichnung1", //$NON-NLS-1$
			"Zusatz1=Bezeichnung2", //$NON-NLS-1$
			"Zusatz2=ExtInfo", //$NON-NLS-1$
			"Ansprechperson=Bezeichnung3", Kontakt.FLD_IS_ORGANIZATION, //$NON-NLS-1$
			"Zusatz3=TITEL", //$NON-NLS-1$
			"Tel. direkt=NatelNr" //$NON-NLS-1$
		);
	}
	
	@Override
	public boolean isValid(){
		return super.isValid();
	}
	
	@Override
	protected String getTableName(){
		return Kontakt.TABLENAME;
	}
	
	Organisation(){/* leer */}
	
	protected Organisation(final String id){
		super(id);
	}
	
	/** Eine Organisation bei gegebener ID aus der Datenbank einlesen */
	public static Organisation load(final String id){
		return new Organisation(id);
	}
	
	/** Eine neue Organisation erstellen */
	public Organisation(final String Name, final String Zusatz1){
		create(null);
		set(new String[] {
			NAME, ZUSATZ1
		}, new String[] {
			Name, Zusatz1
		});
	}
	
	@Override
	protected String getConstraint(){
		return new StringBuilder(Kontakt.FLD_IS_ORGANIZATION).append(StringTool.equals)
			.append(JdbcLink.wrap(StringConstants.ONE)).toString();
	}
	
	@Override
	protected void setConstraint(){
		set(Kontakt.FLD_IS_ORGANIZATION, StringConstants.ONE);
	}
	
}
