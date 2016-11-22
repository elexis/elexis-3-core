package ch.elexis.core.stock;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.status.ObjectStatus;

public interface ICommissioningSystemDriver {

	public IStatus initializeInstance(String configuration);

	public IStatus getStatus();

	public IStatus shutdownInstance();

	public IStatus performStockRemoval(String articleId, int quantity, Object data);

	/**
	 * Retrieve the inventory of the stock commissioning system.
	 * 
	 * @param articleId
	 *            if only for a specific article, or <code>null</code> for all
	 * @param data
	 * @return on success, an {@link ObjectStatus} containing a
	 */
	public IStatus retrieveInventory(String articleId, Object data);
}
