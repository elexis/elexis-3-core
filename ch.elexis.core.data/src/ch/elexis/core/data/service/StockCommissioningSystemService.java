package ch.elexis.core.data.service;

import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.internal.StockCommissioningSystemDriverFactories;
import ch.elexis.core.stock.ICommissioningSystemDriver;
import ch.elexis.core.stock.IStock;
import ch.elexis.core.stock.IStockCommissioningSystemService;
import ch.elexis.core.stock.IStockEntry;
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
	public IStatus performArticleOutlay(IStockEntry stockEntry, int quantity, Object data){
		StockEntry se = (StockEntry) stockEntry;
		if (se == null) {
			return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "stock entry is null");
		}
		ElexisEvent performOutlayEvent = new ElexisEvent();
		performOutlayEvent.setTopic(ElexisEventTopics.TOPIC_STOCK_COMMISSIONING_OUTLAY);
		performOutlayEvent.getProperties()
			.put(ElexisEventTopics.TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID, se.getId());
		performOutlayEvent.getProperties().put(
			ElexisEventTopics.TOPIC_STOCK_COMMISSIONING_PROPKEY_QUANTITY,
			Integer.toString(quantity));
		return CoreHub.getElexisServerEventService().postEvent(performOutlayEvent);
	}
	
	@Override
	public IStatus shutdownStockCommissioningSytem(IStock stock){
		return Status.OK_STATUS;
	}
	
	@Override
	public ICommissioningSystemDriver getDriverInstanceForStock(IStock stock){
		return null;
	}
	
	@Override
	public void shutdownInstances(){}

	@Override
	public IStatus synchronizeInventory(IStock stock, String articleId, Object data){
		return Status.OK_STATUS;
	}
	
}
