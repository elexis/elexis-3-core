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

import java.util.Objects;

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
	
	/**
	 * The {@link #Mandant()} is marked as being inactive, that is, no billings can be done on it
	 * 
	 * @since 3.7
	 */
	public static final String FLD_EXT_IS_INACTIVE = "isInactive";
	
	static {
		addMapping(Kontakt.TABLENAME, FLD_EXTINFO, FLD_IS_MANDATOR, "Label=Bezeichnung3");
	}
	
	public boolean isValid(){
		if (get(FLD_IS_MANDATOR).equals(StringConstants.ZERO)) {
			return false;
		}
		return super.isValid();
	}
	
	public Rechnungssteller getRechnungssteller(){
		Rechnungssteller ret = Rechnungssteller.load(getInfoString(BILLER));
		return ret.isValid() ? ret : Rechnungssteller.load(getId());
	}
	
	public void setRechnungssteller(Kontakt rs){
		setInfoElement(BILLER, rs.getId());
	}
	
	/**
	 * 
	 * @return see {@link #FLD_EXT_IS_INACTIVE}
	 * @since 3.7
	 */
	public boolean isInactive(){
		Object value = getExtInfoStoredObjectByKey(FLD_EXT_IS_INACTIVE);
		return Objects.equals(Boolean.TRUE, value);
	}
	
	/**
	 * 
	 * @param value
	 *            see {@link #FLD_EXT_IS_INACTIVE}
	 * @since 3.7
	 */
	public void setInactive(boolean value){
		setExtInfoStoredObjectByKey(FLD_EXT_IS_INACTIVE, Boolean.valueOf(value));
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
		return ret;
	}
	
	public Mandant(String name, String pwd){
		super(name, pwd, true);
	}

	/**
	 * @since 3.8
	 * @param name
	 * @param pwd
	 * @param email
	 */
	public Mandant(String name, String pwd, String email){
		super(name, pwd, true);
		set(new String[] {Person.FLD_E_MAIL}, email);
	}
	
	protected String getConstraint(){
		return new StringBuilder(FLD_IS_MANDATOR).append(Query.EQUALS)
			.append(JdbcLink.wrap(StringConstants.ONE)).toString();
		
	}
	
	@Override
	protected void setConstraint(){
		set(new String[] {
			FLD_IS_MANDATOR, FLD_IS_USER, FLD_IS_PERSON
		}, new String[] {
			StringConstants.ONE, StringConstants.ONE, StringConstants.ONE
		});
	}
	
	public String getMandantLabel(){
		return getName() + " " + getVorname() + " (" + getLabel() + ")";
	}
	
	@Override
	protected String getTableName(){
		return Kontakt.TABLENAME;
	}
	
	public PersistentObject getReferencedObject(String fieldl){
		if (fieldl != null) {
			if ("Responsible".equals(fieldl)) {
				String responsibleId =
					(String) getExtInfoStoredObjectByKey("ch.elexis.tarmedprefs.responsible");
				if (responsibleId != null && !responsibleId.isEmpty()) {
					return Mandant.load(responsibleId);
				}
			}
		}
		return null;
	}
}
