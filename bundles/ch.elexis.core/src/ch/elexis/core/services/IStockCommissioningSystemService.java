package ch.elexis.core.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;

public interface IStockCommissioningSystemService {
	
	
	public List<UUID> listAllAvailableDrivers();
	
	public String getInfoStringForDriver(UUID driverUuid, boolean extended);
	
	public IStatus initializeStockCommissioningSystem(IStock stock);
	
	public IStatus shutdownStockCommissioningSytem(IStock stock);
	
	public IStatus initializeInstancesUsingDriver(UUID driver);
	
	public IStatus shutdownInstancesUsingDriver(UUID driver);
	
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock);
	
	/**
	 * Outlays the article by effectively ordering the respective the device to issue it. 
	 * 
	 * @param stockEntry
	 * @param quantity
	 * @param data pass optional values
	 * @return
	 */
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity, Map<String, Object> data);
	
	/**
	 * Synchronize the given {@link IStock} with the state of the commissioning system.
	 * 
	 * @param stock
	 *            the stock to perform the synchronization on
	 * @param articleId
	 *            if <code>null</code> synchronize all articles, else only those with ids contained
	 *            in the list
	 * @param data pass optional values
	 * @return
	 */
	public IStatus synchronizeInventory(IStock stock, List<String> articleIds, Map<String, Object> data);
}
