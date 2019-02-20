package ch.elexis.core.model.builder;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.services.IModelService;

public class IInvoiceBilledBuilder extends AbstractBuilder<IInvoiceBilled> {
	
	public IInvoiceBilledBuilder(IModelService modelService, IBilled billed){
		super(modelService);
		
		object = modelService.create(IInvoiceBilled.class);
		object.setAmount(billed.getAmount());
		object.setBillable(billed.getBillable());
		object.setEncounter(billed.getEncounter());
		// copy ext info
		modelService.setEntityProperty("detail",
			modelService.getEntityProperty("detail", billed), object);
		object.setFactor(billed.getFactor());
		object.setNetPrice(billed.getNetPrice());
		object.setPoints(billed.getPoints());
		if (billed.isChangedPrice()) {
			object.setPrice(billed.getPrice());
		}
		object.setPrimaryScale(billed.getPrimaryScale());
		object.setSecondaryScale(billed.getSecondaryScale());
		object.setText(billed.getText());
	}
}
