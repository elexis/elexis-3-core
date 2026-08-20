package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.services.IModelService;

/**
 * Builder for creating new {@link IOrder} objects. Allows object creation and
 * configuration without immediate persistence.
 */
public class IOrderBuilder extends AbstractBuilder<IOrder> {

	/**
	 * Initializes the builder and sets up the initial IOrder state.
	 *
	 * @param modelService the model service used for object creation
	 * @param name         the initial name of the order
	 */
	public IOrderBuilder(IModelService modelService, String name) {
		super(modelService);

		object = modelService.create(IOrder.class);
		object.setTimestamp(LocalDateTime.now());
		object.setName(name);
	}
}