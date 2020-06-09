package ch.elexis.core.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;
import ch.elexis.core.services.internal.Bundle;

/**
 * A service for performing article outlays via a stock management system. This implementation
 * currently one sends the respective events to the Elexis server, which handles the connection to
 * the stock management system.
 */
@Component
public class StockCommissioningSystemService implements IStockCommissioningSystemService {
	
	@Reference
	private IElexisServerService elexisServerService;
	
	@Override
	public List<UUID> listAllAvailableDrivers(){
		return StockCommissioningSystemDriverFactories.getAllDriverUuids();
	}
	
	@Override
	public String getInfoStringForDriver(UUID driverUuid, boolean extended){
		return StockCommissioningSystemDriverFactories.getInfoStringForDriver(driverUuid, extended);
	}
	
	@Override
	public IStatus initializeStockCommissioningSystem(IStock stock){
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity,
		Map<String, Object> data){
		if (stockEntry == null) {
			return new Status(Status.ERROR, Bundle.ID, "stock entry is null");
		}
		
		ElexisEvent performOutlayEvent = new ElexisEvent();
		performOutlayEvent.setTopic(ElexisEventTopics.STOCK_COMMISSIONING_OUTLAY);
		performOutlayEvent.getProperties()
			.put(ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID, stockEntry.getId());
		performOutlayEvent.getProperties().put(
			ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_QUANTITY, Integer.toString(quantity));
		return elexisServerService.postEvent(performOutlayEvent);
	}
	
	@Override
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock){
		return null;
	}
	
	@Override
	public IStatus synchronizeInventory(IStock stock, List<String> articleIds,
		Map<String, Object> data){
		ElexisEvent synchronizeEvent = new ElexisEvent();
		synchronizeEvent.setTopic(ElexisEventTopics.STOCK_COMMISSIONING_SYNC_STOCK);
		synchronizeEvent.getProperties().put(ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_STOCK_ID,
			stock.getId());
		// TODO enable transfer of list
		return elexisServerService.postEvent(synchronizeEvent);
	}
	
	@Override
	public IStatus initializeInstancesUsingDriver(UUID driver){
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus shutdownInstancesUsingDriver(UUID driver){
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus shutdownStockCommissioningSytem(IStock stock){
		return Status.OK_STATUS;
	}
	
}
