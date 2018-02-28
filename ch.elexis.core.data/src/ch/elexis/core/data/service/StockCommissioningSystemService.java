package ch.elexis.core.data.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.internal.StockCommissioningSystemDriverFactories;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.stock.ICommissioningSystemDriver;
import ch.elexis.core.services.IStockCommissioningSystemService;
import ch.elexis.data.StockEntry;

/**
 * A service for performing article outlays via a stock management system. This implementation
 * currently one sends the respective events to the Elexis server, which handles the connection to
 * the stock management system.
 */
public class StockCommissioningSystemService implements IStockCommissioningSystemService {
	
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
		StockEntry se = (StockEntry) stockEntry;
		if (se == null) {
			return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "stock entry is null");
		}
				
		ElexisEvent performOutlayEvent = new ElexisEvent();
		performOutlayEvent.setTopic(ElexisEventTopics.STOCK_COMMISSIONING_OUTLAY);
		performOutlayEvent.getProperties()
			.put(ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID, se.getId());
		performOutlayEvent.getProperties().put(
			ElexisEventTopics.STOCK_COMMISSIONING_PROPKEY_QUANTITY, Integer.toString(quantity));
		return CoreHub.getElexisServerEventService().postEvent(performOutlayEvent);
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
		return CoreHub.getElexisServerEventService().postEvent(synchronizeEvent);
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
