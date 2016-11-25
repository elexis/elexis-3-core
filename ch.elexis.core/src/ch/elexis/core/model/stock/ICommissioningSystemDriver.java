package ch.elexis.core.model.stock;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IStock;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.core.status.ObjectStatus;

public interface ICommissioningSystemDriver {

	/**
	 * Initialize a driver commissioning system driver instance for this stock.
	 * The driver instantiated is responsible for maintaining the connection to
	 * the commissioning system and inform the
	 * {@link IStockCommissioningSystemService} about relevant changes.
	 * 
	 * @param configuration
	 * @param stock
	 * @return
	 */
	public IStatus initializeInstance(String configuration, IStock stock);

	public IStatus getStatus();

	public IStatus shutdownInstance();

	public IStatus performStockRemoval(String articleId, int quantity, Object data);

	/**
	 * Retrieve the inventory of the stock commissioning system.
	 * 
	 * @param articleId
	 *            if only for a specific number of articles, or <code>null</code> for all
	 * @param data
	 * @return on success, an {@link ObjectStatus} containing a
	 */
	public IStatus retrieveInventory(List<String> articleIds, Object data);
}
