package ch.elexis.core.stock;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;

public interface IStockCommissioningSystemService {
	
	public List<UUID> listAllAvailableDrivers();
	
	public String getInfoStringForDriver(UUID driverUuid, boolean extended);
	
	public IStatus initializeStockCommissioningSystem(IStock stock);
	
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity,
		Object data);
	
	public IStatus shutdownStockCommissioningSytem(IStock stock);
	
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock);
	
	public void shutdownInstances();
}
