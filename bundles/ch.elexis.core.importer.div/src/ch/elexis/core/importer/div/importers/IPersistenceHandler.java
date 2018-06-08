package ch.elexis.core.importer.div.importers;

import java.util.List;

import ch.elexis.core.data.interfaces.ILabOrder;
import ch.elexis.core.data.interfaces.IPatient;

public interface IPersistenceHandler {
	
	List<ILabOrder> getLabOrdersByOrderId(String resultObj);
	
	IPatient loadPatient(String id);
}
