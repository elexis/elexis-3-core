package ch.elexis.core.importer.div.importers;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

/**
 * since 3.8 moved from ch.elexis.core.ui.importer.div.importers.PersistenceHandler
 */
public class DefaultPersistenceHandler implements IPersistenceHandler {
	
	@Override
	public List<ILabOrder> getLabOrdersByOrderId(String orderId){
		IQuery<ILabOrder> query = CoreModelServiceHolder.get().getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__ORDER_ID, COMPARATOR.EQUALS, orderId);
		query.and("ID", COMPARATOR.NOT_EQUALS, StringConstants.VERSION_LITERAL);
		return query.execute();
	}

	public IPatient loadPatient(String id){
		Optional<IPatient> loaded = CoreModelServiceHolder.get().load(id, IPatient.class);
		return loaded.orElse(null);
	}
	
}
