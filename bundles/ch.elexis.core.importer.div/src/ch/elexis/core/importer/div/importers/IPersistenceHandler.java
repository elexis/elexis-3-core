package ch.elexis.core.importer.div.importers;

import java.util.List;

import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.IPatient;

public interface IPersistenceHandler {
	
	/**
	 * Get {@link ILabOrder} instances matching the orderId.
	 * 
	 * @param orderId
	 * @return
	 */
	List<ILabOrder> getLabOrdersByOrderId(String orderId);
	
	/**
	 * Load an {@link IPatient} instance with matching id.
	 * 
	 * @param id
	 * @return
	 */
	IPatient loadPatient(String id);
}
