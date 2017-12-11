/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.util.MultiplikatorList;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class Eigenleistung extends VerrechenbarAdapter {
	public static final String CODESYSTEM_NAME = "Eigenleistung";
	public static final String TIME = "Zeit";
	public static final String VK_PREIS = "VK_Preis";
	public static final String EK_PREIS = "EK_Preis";
	public static final String BEZEICHNUNG = "Bezeichnung";
	public static final String CODE = "Code";
	public static final String EIGENLEISTUNGEN = "EIGENLEISTUNGEN";
	public static final String XIDDOMAIN = "www.xid.ch/id/customservices";
	
	static {
		addMapping(EIGENLEISTUNGEN, CODE, BEZEICHNUNG, EK_PREIS, VK_PREIS, TIME);
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN, CODESYSTEM_NAME, Xid.ASSIGNMENT_LOCAL
			| Xid.QUALITY_GUID);
	}
	
	public String getXidDomain(){
		return XIDDOMAIN;
	}
	
	@Override
	protected String getTableName(){
		return EIGENLEISTUNGEN;
	}
	
	@Override
	public String getCode(){
		String ret = get(CODE);
		if (ret == null || ret.isEmpty()) {
			ret = getId();
		}
		return ret;
	}
	
	@Override
	public String getText(){
		return get(BEZEICHNUNG);
	}
	
	public String[] getDisplayedFields(){
		return new String[] {
			CODE, BEZEICHNUNG
		};
	}
	
	@Override
	public String getCodeSystemName(){
		return CODESYSTEM_NAME;
	}
	
	@Override
	public Money getKosten(final TimeTool dat){
		return new Money(checkZero(get(EK_PREIS)));
	}
	
	public Money getPreis(final TimeTool dat, final IFall fall){
		return new Money(checkZero(get(VK_PREIS)));
	}
	
	public Eigenleistung(final String code, final String name, final String ek, final String vk){
		create(null);
		set(new String[] {
			CODE, BEZEICHNUNG, EK_PREIS, VK_PREIS
		}, code, name, ek, vk);
	}
	
	protected Eigenleistung(){}
	
	protected Eigenleistung(final String id){
		super(id);
	}
	
	public static Eigenleistung load(final String id){
		return new Eigenleistung(id);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	public int getTP(final TimeTool date, final IFall fall){
		return getPreis(date, fall).getCents();
	}
	
	public double getFactor(final TimeTool date, final IFall fall){
		if (MultiplikatorList.isEigenleistungUseMulti(fall.getAbrechnungsSystem())) {
			return getVKMultiplikator(date, fall);
		}
		return 1.0;
	}
	
}
