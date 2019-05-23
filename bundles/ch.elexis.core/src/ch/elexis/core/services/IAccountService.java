package ch.elexis.core.services;

import java.util.HashMap;
import java.util.List;

import ch.elexis.core.model.IAccount;

public interface IAccountService {
	
	/**
	 * Initialize the available accounts with default values.
	 * 
	 */
	public void initDefaults();
	
	/**
	 * Get the {@link IAccount} implementation representing an unknown account.
	 * 
	 * @return
	 */
	public IAccount getUnknown();
	
	/**
	 * Get the current map of accounts.
	 * 
	 * @return
	 */
	public HashMap<Integer, IAccount> getAccounts();
	
	/**
	 * Remove the account.
	 * 
	 * @param account
	 */
	public void removeAccount(IAccount account);
	
	/**
	 * Add the account to the available accounts.
	 * 
	 * @param newAccount
	 */
	void addAccount(IAccount newAccount);
	
	/**
	 * Reset the available accounts to the specified accounts list.
	 * 
	 * @param accounts
	 */
	void setAccounts(List<IAccount> accounts);
}
