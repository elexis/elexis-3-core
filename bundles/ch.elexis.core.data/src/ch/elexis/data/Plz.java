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
// $Id: Plz.java 1256 2006-11-09 13:16:10Z rgw_ch $
package ch.elexis.data;

import ch.rgw.tools.StringTool;

// TODO is this used?
public class Plz extends PersistentObject {
	
	static {
		addMapping("PLZ", "Plz", "Ort", "Kanton");
	}
	
	public Plz(String plz, String Ort, String Kanton){
		create(null);
		set(new String[] {
			"Plz", "Ort", "Kanton"
		}, new String[] {
			plz, Ort, Kanton
		});
	}
	
	public static Plz load(String id){
		if (StringTool.isNothing(id)) {
			return null;
		}
		return new Plz(id);
	}
	
	public Plz(String id){
		super(id);
	}
	
	public String getLabel(){
		String[] f = new String[3];
		get(new String[] {
			"Plz", "Ort", "Kanton"
		}, f);
		StringBuilder ret = new StringBuilder();
		ret.append(f[0]).append(StringTool.space).append(f[1]).append(StringTool.space).append(f[2]);
		return ret.toString();
	}
	
	protected Plz(){ /* empty */}
	
	@Override
	protected String getTableName(){
		return "PLZ";
	}
	
	@Override
	public int getCacheTime(){
		return Integer.MAX_VALUE;
	}
	
}
