package ch.elexis.core.model.billable;

import java.util.List;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IBilledBuilder;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Result;

public abstract class AbstractOptifier implements IBillableOptifier {
	
	private IModelService modelService;
	
	public AbstractOptifier(IModelService modelService){
		this.modelService = modelService;
	}
	
	@Override
	public Result<IBilled> add(IBillable billable, IEncounter encounter, double amount){
		boolean added = false;
		IBilled billed = null;
		// lookup existing billed, add if found
		List<IBilled> existingBilled = encounter.getBilled();
		for (IBilled iBilled : existingBilled) {
			IBillable existing = iBilled.getBillable();
			if (existing != null && existing.equals(billable)) {
				iBilled.setAmount(iBilled.getAmount() + amount);
				modelService.save(iBilled);
				billed = iBilled;
				added = true;
				break;
			}
		}
		if (!added) {
			billed = new IBilledBuilder(modelService, billable, encounter).build();
			setPrice(billable, billed);
			billed.setAmount(amount);
			modelService.save(billed);
		}
		return new Result<IBilled>(billed);
	}
	
	protected abstract void setPrice(IBillable billable, IBilled billed);
}
