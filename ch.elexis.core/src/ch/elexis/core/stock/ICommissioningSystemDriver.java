package ch.elexis.core.stock;

import org.eclipse.core.runtime.IStatus;

public interface ICommissioningSystemDriver {
	
	public IStatus initializeInstance(String configuration);
	
	public IStatus getStatus();
	
	public IStatus shutdownInstance();
	
	public IStatus performStockRemoval(String articleId, int quantity, Object data);
	
	/**
	 * Synchronize the system stock data with the device stock data.
	 * 
	 * @param stock
	 *            the system stock this device is allocated to
	 * @param articleId
	 *            if <code>null</code> synchronize stock information for all articles, else only the
	 *            given
	 * @param data
	 * @return
	 */
	public IStatus synchronizeInventory(IStock stock, String articleId, Object data);
}
