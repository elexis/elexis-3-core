package ch.elexis.core.stock;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;

public interface IStockCommissioningSystemService {

	public List<UUID> listAllAvailableDrivers();

	public String getInfoStringForDriver(UUID driverUuid, boolean extended);

	public IStatus initializeStockCommissioningSystem(IStock stock);

	/**
	 * Outlays the article by effectively ordering the respective the device to
	 * issue it.
	 * 
	 * @param stockEntry
	 * @param quantity
	 * @param data
	 * @return
	 */
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity, Object data);

	/**
	 * Synchronize the given {@link IStock} with the state of the commissioning
	 * system.
	 * 
	 * @param stock
	 *            the stock to perform the synchronization on
	 * @param articleId
	 *            if <code>null</code> synchronize all articles, else only this
	 *            one
	 * @param data
	 * @return
	 */
	public IStatus synchronizeInventory(IStock stock, String articleId, Object data);

	public IStatus shutdownStockCommissioningSytem(IStock stock);

	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock);

	public void shutdownInstances();
}
