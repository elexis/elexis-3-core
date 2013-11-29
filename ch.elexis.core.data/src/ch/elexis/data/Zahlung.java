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

import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class Zahlung extends PersistentObject {
	public static final String DATE = "Datum";
	public static final String AMMOUNT_CENTS = "Betragx100";
	public static final String REMARK = "Bemerkung";
	public static final String BILL_ID = "RechnungsID";
	static final String TABLENAME = "ZAHLUNGEN";
	
	static {
		addMapping(TABLENAME, BILL_ID, "Betragx100=Betrag", "Datum=S:D:Datum", REMARK);
	}
	
	public Zahlung(Rechnung rn, Money Betrag, String text, TimeTool date){
		create(null);
		if (date == null) {
			date = new TimeTool();
		}
		String Datum = date.toString(TimeTool.DATE_GER);
		set(new String[] {
			BILL_ID, AMMOUNT_CENTS, DATE, REMARK
		}, rn.getId(), Betrag.getCentsAsString(), Datum, text);
		new AccountTransaction(this);
		rn.addTrace(Rechnung.PAYMENT, Betrag.getAmountAsString());
	}
	
	@Override
	public boolean delete(){
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM KONTO WHERE ID=").append(getWrappedId());
		getConnection().exec(sb.toString());
		Rechnung rn = getRechnung();
		if (rn != null) {
			rn.addTrace(Rechnung.CORRECTION, "Zahlung gelöscht");
		}
		return super.delete();
	}
	
	public String getBemerkung(){
		return get(REMARK);
	}
	
	public Rechnung getRechnung(){
		return Rechnung.load(get(BILL_ID));
	}
	
	public Money getBetrag(){
		return new Money(checkZero(get(AMMOUNT_CENTS)));
	}
	
	public String getDatum(){
		return get(DATE);
	}
	
	public static Zahlung load(String id){
		return new Zahlung(id);
	}
	
	protected Zahlung(){/* leer */}
	
	protected Zahlung(String id){
		super(id);
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		sb.append(getDatum()).append(": ").append(getBetrag().getAmountAsString());
		return sb.toString();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
}
