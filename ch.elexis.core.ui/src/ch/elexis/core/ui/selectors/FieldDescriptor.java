/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.selectors;

import ch.elexis.data.PersistentObject;

public class FieldDescriptor<T extends PersistentObject> {
	
	public enum Typ {
		STRING, INT, CURRENCY, LIST, HYPERLINK, DATE, COMBO, BOOLEAN
	};
	
	String sAnzeige, sFeldname, sHashname;
	Typ tFeldTyp;
	Object ext;
	
	/** Retrieve the visible label of the field */
	public String getLabel(){
		return sAnzeige;
	}
	
	/** return the name of the database field backing this field */
	public String getFieldname(){
		return sFeldname;
	}
	
	/** return the name of this field in the Object's ExitInfo properties */
	public String getHashname(){
		return sHashname;
	}
	
	/** get the type of this field */
	public Typ getFieldType(){
		return tFeldTyp;
	}
	
	/** Return any object associated with this field */
	public Object getExtension(){
		return ext;
	}
	
	public FieldDescriptor(String anzeige, String feldname, Typ feldtyp, String hashname){
		sAnzeige = anzeige;
		sFeldname = feldname;
		tFeldTyp = feldtyp;
		sHashname = hashname;
		
	}
	
	public FieldDescriptor(String all){
		sAnzeige = all;
		sFeldname = all;
		tFeldTyp = Typ.STRING;
		sHashname = null;
	}
	
	public FieldDescriptor(String anzeige, String feldname, IObjectLink<T> cp){
		sAnzeige = anzeige;
		sFeldname = feldname;
		ext = cp;
		tFeldTyp = Typ.HYPERLINK;
	}
	
	public FieldDescriptor(String anzeige, String feldname, String hashname, String[] choices){
		sAnzeige = anzeige;
		sFeldname = feldname;
		sHashname = hashname;
		tFeldTyp = Typ.LIST;
		ext = choices;
	}
	
	public FieldDescriptor(String anzeige, String feldname, String hashname, String[] comboItems,
		boolean bDropDown){
		sAnzeige = anzeige;
		sFeldname = feldname;
		sHashname = hashname;
		tFeldTyp = Typ.COMBO;
		ext = comboItems;
	}
}
