/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
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

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AccountTransaction extends PersistentObject {
	public static final String FLD_REMARK = "Bemerkung"; //$NON-NLS-1$
	public static final String FLD_AMOUNT = "Betrag"; //$NON-NLS-1$
	public static final String FLD_BILL_ID = "RechnungsID"; //$NON-NLS-1$
	public static final String FLD_PAYMENT_ID = "ZahlungsID"; //$NON-NLS-1$
	public static final String FLD_PATIENT_ID = "PatientID"; //$NON-NLS-1$
	private static final String TABLENAME = "KONTO"; //$NON-NLS-1$
	
	static {
		addMapping(TABLENAME, FLD_PATIENT_ID, FLD_PAYMENT_ID, FLD_BILL_ID, FLD_AMOUNT,
			DATE_COMPOUND, FLD_REMARK);
	}
	
	public AccountTransaction(Patient pat, Rechnung r, Money betrag, String date, String bemerkung){
		create(null);
		if (date == null) {
			date = new TimeTool().toString(TimeTool.DATE_GER);
		}
		set(new String[] {
			FLD_AMOUNT, FLD_DATE, FLD_REMARK
		}, betrag.getCentsAsString(), date, bemerkung);
		
		if (pat != null && pat.exists()) {
			set(FLD_PATIENT_ID, pat.getId());
		}
		if (r != null) {
			set(FLD_BILL_ID, r.getId());
		}
	}
	
	public AccountTransaction(Zahlung z){
		create(null);
		Rechnung r = z.getRechnung();
		Patient p = r.getFall().getPatient();
		set(new String[] {
			FLD_PATIENT_ID, FLD_AMOUNT, FLD_DATE, FLD_REMARK, FLD_BILL_ID, FLD_PAYMENT_ID
		}, p.getId(), z.getBetrag().getCentsAsString(), z.getDatum(), z.getBemerkung(), r.getId(),
			z.getId());
	}
	
	public String getDate(){
		return get(FLD_DATE);
	}
	
	public Money getAmount(){
		try {
			return new Money(checkZero(get(FLD_AMOUNT)));
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new Money();
		}
	}
	
	public String getRemark(){
		return checkNull(get(FLD_REMARK));
	}
	
	public Patient getPatient(){
		return Patient.load(get(FLD_PATIENT_ID));
	}
	
	public Rechnung getRechnung(){
		return Rechnung.load(get(FLD_BILL_ID));
	}
	
	public Zahlung getZahlung(){
		String zi = get(FLD_PAYMENT_ID);
		if (StringTool.isNothing(zi)) {
			return null;
		}
		return Zahlung.load(zi);
	}
	
	@Override
	public boolean delete(){
		Zahlung z = getZahlung();
		if (z != null) {
			z.delete();
		}
		return super.delete();
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		sb.append(get(FLD_DATE)).append(StringTool.space).append(get(FLD_AMOUNT))
			.append(StringTool.space).append(get(FLD_REMARK));
		return sb.toString();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static AccountTransaction load(String id){
		return new AccountTransaction(id);
	}
	
	protected AccountTransaction(String id){
		super(id);
	}
	
	protected AccountTransaction(){}
	
}
