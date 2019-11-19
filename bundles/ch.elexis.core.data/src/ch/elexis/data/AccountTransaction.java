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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AccountTransaction extends PersistentObject {
	private static final String ACCOUNTS_CONFIG = "ch.elexis.core.data/accounttransaction/accounts"; //$NON-NLS-1$
	private static final String ACCOUNTS_SEPARATOR = "||"; //$NON-NLS-1$
	
	public static final String FLD_REMARK = "Bemerkung"; //$NON-NLS-1$
	public static final String FLD_AMOUNT = "Betrag"; //$NON-NLS-1$
	public static final String FLD_ACCOUNT = "account"; //$NON-NLS-1$
	public static final String FLD_BILL_ID = "RechnungsID"; //$NON-NLS-1$
	public static final String FLD_PAYMENT_ID = "ZahlungsID"; //$NON-NLS-1$
	public static final String FLD_PATIENT_ID = "PatientID"; //$NON-NLS-1$
	private static final String TABLENAME = "KONTO"; //$NON-NLS-1$
	
	static {
		addMapping(TABLENAME, FLD_PATIENT_ID, FLD_PAYMENT_ID, FLD_BILL_ID, FLD_AMOUNT,
			DATE_COMPOUND, FLD_REMARK, FLD_ACCOUNT);
	}
	
	/**
	 * Class for marking transaction with account information. The information is used for
	 * reporting.
	 * 
	 * @author thomas
	 *
	 */
	public static class Account {
		private int numeric;
		private String name;
		
		public static Account UNKNOWN = new Account(-1, StringTool.leer);
		private static HashMap<Integer, Account> localCache;
		
		private static List<Account> loadAccounts(){
			List<Account> ret = new ArrayList<>();
			ret.add(UNKNOWN);
			if (CoreHub.globalCfg != null) {
				String accountsString = CoreHub.globalCfg.get(ACCOUNTS_CONFIG, StringTool.leer); //$NON-NLS-1$
				if (accountsString != null && !accountsString.isEmpty()) {
					String[] accounts = accountsString.split("\\|\\|"); //$NON-NLS-1$
					for (String string : accounts) {
						String[] parts = string.split("\\|"); //$NON-NLS-1$
						if (parts.length == 2) {
							ret.add(new Account(Integer.parseInt(parts[0]), parts[1]));
						}
					}
				}
			}
			return ret;
		}
		
		/**
		 * Get the current map of accounts. The map is reloaded after
		 * {@link Account#setAccounts(List)} is called.
		 * 
		 * @return
		 */
		public static HashMap<Integer, Account> getAccounts(){
			if (localCache == null) {
				loadCache();
			}
			return localCache;
		}
		
		private static void loadCache(){
			localCache = new HashMap<>();
			List<Account> accounts = loadAccounts();
			for (Account account : accounts) {
				localCache.put(account.getNumeric(), account);
			}
		}
		
		public static void removeAccount(Account account){
			List<Account> accounts = loadAccounts();
			for (Iterator<Account> iterator = accounts.iterator(); iterator.hasNext();) {
				Account existingAccount = (Account) iterator.next();
				if (existingAccount.getNumeric() == account.getNumeric()) {
					iterator.remove();
				}
			}
			setAccounts(accounts);
		}
		
		public static void addAccount(Account newAccount){
			if (CoreHub.globalCfg != null) {
				String existingString = CoreHub.globalCfg.get(ACCOUNTS_CONFIG, StringTool.leer);
				StringBuilder sb = new StringBuilder();
				sb.append(existingString);
				if (sb.length() > 0) {
					sb.append(ACCOUNTS_SEPARATOR);
				}
				sb.append(newAccount.getNumeric()).append("|").append(newAccount.getName());
				CoreHub.globalCfg.set(ACCOUNTS_CONFIG, sb.toString());
				// reset local cache
				loadCache();
			}
		}
		
		public static void setAccounts(List<Account> accounts){
			if (CoreHub.globalCfg != null) {
				StringBuilder sb = new StringBuilder();
				for (Account account : accounts) {
					if (account.getNumeric() == -1) {
						continue;
					}
					if (sb.length() > 0) {
						sb.append(ACCOUNTS_SEPARATOR);
					}
					sb.append(account.getNumeric()).append("|").append(account.getName());
				}
				CoreHub.globalCfg.set(ACCOUNTS_CONFIG, sb.toString());
				// reset local cache
				loadCache();
			}
		}
		
		public static void initDefaults(){
			HashMap<Integer, Account> existingAccounts = getAccounts();
			if (!existingAccounts.containsKey(new Integer(1000))) {
				addAccount(new Account(1000, "Kasse"));
			}
			if (!existingAccounts.containsKey(new Integer(1100))) {
				addAccount(new Account(1100, "Post"));
			}
			if (!existingAccounts.containsKey(new Integer(1200))) {
				addAccount(new Account(1200, "BESR"));
			}
			if (!existingAccounts.containsKey(new Integer(1201))) {
				addAccount(new Account(1201, "Manuelle Bankeingänge"));
			}
			if (!existingAccounts.containsKey(new Integer(1209))) {
				addAccount(new Account(1209, "EFT-Zahlungen"));
			}
			if (!existingAccounts.containsKey(new Integer(4590))) {
				addAccount(new Account(4590, "Differenz"));
			}
			if (!existingAccounts.containsKey(new Integer(4595))) {
				addAccount(new Account(4595, "Debitorenverlust"));
			}
			if (!existingAccounts.containsKey(new Integer(6060))) {
				addAccount(new Account(6060, "Mahngebühren (Ertrag)"));
			}
			if (!existingAccounts.containsKey(new Integer(9999))) {
				addAccount(new Account(9999, "Diverses"));
			}
		}
		
		public Account(int numeric, String name){
			this.numeric = numeric;
			this.name = name;
		}
		
		public int getNumeric(){
			return numeric;
		}
		
		public String getName(){
			return name;
		}
		
		public void setNumeric(Integer numeric){
			this.numeric = numeric;
		}
		
		public void setName(String name){
			this.name = name;
		}
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
	
	/**
	 * Set the Account for the transaction.
	 * 
	 * @param account
	 * @since 3.2
	 */
	public void setAccount(Account account){
		set(FLD_ACCOUNT, Integer.toString(account.getNumeric()));
	}
	
	/**
	 * Get the account for the transaction.
	 * 
	 * @return the {@link Account}
	 * @since 3.2
	 */
	public Account getAccount(){
		String accountNumeric = get(FLD_ACCOUNT);
		if (accountNumeric != null && !accountNumeric.isEmpty()) {
			try {
				accountNumeric = accountNumeric.trim(); // care for postgres adding spaces
				return Account.getAccounts().get(Integer.parseInt(accountNumeric));
			} catch (NumberFormatException e) {}
		}
		return Account.UNKNOWN;
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
		Account account = getAccount();
		if(account != Account.UNKNOWN) {
			sb.append(StringTool.space).append(account.getName());
		}
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
