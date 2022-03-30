package ch.elexis.core.model.builder;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.IModelService;

public class IBilledBuilder extends AbstractBuilder<IBilled> {

	public IBilledBuilder(IModelService modelService, IBillable billable, IEncounter encounter, IContact biller) {
		super(modelService);

		object = modelService.create(IBilled.class);
		object.setEncounter(encounter);
		object.setBillable(billable);
		object.setBiller(biller);
		object.setText(billable.getText());
		object.setPrimaryScale(100);
		object.setSecondaryScale(100);
		object.setAmount(1);
	}
}
