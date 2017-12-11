/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
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

import ch.rgw.tools.StringTool;

public class Anschrift {
	private static final String COUNTRY = "Land"; //$NON-NLS-1$
	private static final String PLACE = "Ort"; //$NON-NLS-1$
	private static final String ZIP = "Plz"; //$NON-NLS-1$
	private static final String STREET = "Strasse"; //$NON-NLS-1$
	String Strasse, Plz, Ort, Land;
	String[] fields = {
		STREET, ZIP, PLACE, COUNTRY
	};
	Kontakt mine;
	
	public Anschrift(Kontakt k){
		mine = k;
		String[] values = new String[fields.length];
		k.get(fields, values);
		Strasse = values[0];
		Plz = values[1];
		Ort = values[2];
		Land = values[3];
	}
	
	public Anschrift(){}
	
	/**
	 * Eine Etikette der Anschrift liefern
	 * 
	 * @param withName
	 *            TODO
	 * @param multiline
	 *            Wenn true wird die Etikette mehrzeilig, sonst einzeilig
	 */
	public String getEtikette(boolean withName, boolean multiline){
		String sep = StringTool.lf;
		if (multiline == false) {
			sep = ", "; //$NON-NLS-1$
		}
		StringBuilder ret = new StringBuilder(100);
		if (withName == true) {
			ret.append(mine.getLabel(false)).append(sep);
		}
		if (Strasse != null) {
			ret.append(Strasse).append(sep);
		}
		if (!StringTool.isNothing(Land)) {
			ret.append(Land).append(" - "); //$NON-NLS-1$
		}
		if ((Plz != null) && (Ort != null)) {
			ret.append(Plz).append(StringTool.space).append(Ort);
		}
		if (multiline) {
			// append trailing newline
			ret.append(StringTool.lf);
		}
		return ret.toString();
	}
	
	public String getLabel(){
		return getEtikette(true, false);
	}
	
	public String getStrasse(){
		return StringTool.unNull(Strasse);
	}
	
	public String getPlz(){
		return StringTool.unNull(Plz);
	}
	
	public String getOrt(){
		return StringTool.unNull(Ort);
	}
	
	public String getLand(){
		return StringTool.unNull(Land);
	}
	
	public void setStrasse(String s){
		Strasse = s;
	}
	
	public void setPlz(String plz){
		if (plz != null) {
			Plz = plz.length() > 6 ? plz.substring(0, 6) : plz;
		} else {
			Plz = StringTool.leer;
		}
	}
	
	public void setOrt(String ort){
		Ort = ort;
	}
	
	public void setLand(String land){
		Land = land.length() > 3 ? land.substring(0, 3) : land;
	}
	
	public boolean write(Kontakt k){
		return k.set(fields, Strasse, Plz, Ort, Land);
	}
}
