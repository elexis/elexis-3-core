package ch.elexis.core.ui.importer.div.importers;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.importer.div.services.ModelServiceHolder;

public class PersistenceHandler implements IPersistenceHandler {
	
	@Override
	public List<ILabOrder> getLabOrdersByOrderId(String orderId){
		IQuery<ILabOrder> query = ModelServiceHolder.get().getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__ORDER_ID, COMPARATOR.EQUALS, orderId);
		return query.execute();
	}

	public IPatient loadPatient(String id){
		Optional<IPatient> loaded = ModelServiceHolder.get().load(id, IPatient.class);
		return loaded.orElse(null);
	}
}
