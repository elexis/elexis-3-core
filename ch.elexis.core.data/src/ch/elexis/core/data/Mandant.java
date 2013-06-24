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

package ch.elexis.core.data;

import ch.elexis.core.constants.StringConstants;
import ch.rgw.tools.JdbcLink;

/**
 * Ein Mandant ist ein Anwender (und damit eine Person und damit ein Kontakt), der zusätzlich eigene
 * Abrechnungen führt.
 * 
 * @author gerry
 * 
 */
public class Mandant extends Anwender {
	
	public static final String BILLER = "Rechnungssteller";
	
	static {
		addMapping(Kontakt.TABLENAME, FLD_EXTINFO, FLD_IS_MANDATOR, "Label=Bezeichnung3");
	}
	
	public boolean isValid(){
		if (get(FLD_IS_MANDATOR).equals(StringConstants.ONE)) {
			return super.isValid();
		}
		return false;
	}
	
	public Rechnungssteller getRechnungssteller(){
		Rechnungssteller ret = Rechnungssteller.load(getInfoString(BILLER));
		return ret.isValid() ? ret : Rechnungssteller.load(getId());
	}
	
	public void setRechnungssteller(Kontakt rs){
		setInfoElement(BILLER, rs.getId());
	}
	
	protected Mandant(String id){
		super(id);
	}
	
	public Mandant(final String Name, final String Vorname, final String Geburtsdatum,
		final String s){
		super(Name, Vorname, Geburtsdatum, s);
	}
	
	protected Mandant(){/* leer */}
	
	public static Mandant load(String id){
		Mandant ret = new Mandant(id);
		String ism = ret.get(FLD_IS_MANDATOR);
		if (ism != null && ism.equals(StringConstants.ONE)) {
			return ret;
		}
		return null;
	}
	
	public Mandant(String name, String pwd){
		super(name, pwd);
	}
	
	protected String getConstraint(){
		return new StringBuilder(FLD_IS_MANDATOR).append(Query.EQUALS)
			.append(JdbcLink.wrap(StringConstants.ONE)).toString();
		
	}
	
	@Override
	protected void setConstraint(){
		set(new String[] {
			FLD_IS_MANDATOR, FLD_IS_USER
		}, new String[] {
			StringConstants.ONE, StringConstants.ONE
		});
	}
	
	public String getMandantLabel(){
		return getName() + " " + getVorname() + " (" + getLabel() + ")";
	}
}
