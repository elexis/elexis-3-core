package ch.elexis.core.model.billable;

import java.util.List;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IBilledBuilder;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Result;

public abstract class AbstractOptifier<T extends IBillable> implements IBillableOptifier<T> {
	
	private IModelService coreModelService;
	
	/**
	 * Create an {@link AbstractOptifier} instance, and provide an {@link IModelService} for
	 * accessing the ch.elexis.core model.
	 * 
	 * @param coreModelService
	 */
	public AbstractOptifier(IModelService coreModelService){
		this.coreModelService = coreModelService;
	}
	
	@Override
	public Result<IBilled> add(T billable, IEncounter encounter, double amount){
		boolean added = false;
		IBilled billed = null;
		// lookup existing billed, add if found
		List<IBilled> existingBilled = encounter.getBilled();
		for (IBilled iBilled : existingBilled) {
			IBillable existing = iBilled.getBillable();
			if (existing != null && existing.equals(billable)) {
				iBilled.setAmount(iBilled.getAmount() + amount);
				coreModelService.save(iBilled);
				billed = iBilled;
				added = true;
				break;
			}
		}
		if (!added) {
			billed = new IBilledBuilder(coreModelService, billable, encounter).build();
			setPrice(billable, billed);
			billed.setAmount(amount);
			coreModelService.save(billed);
		}
		return new Result<IBilled>(billed);
	}
	
	/**
	 * Set the actual price of <b>one</b> billable in the {@link IBilled} instance. The encounter
	 * reference of the {@link IBilled} should be set.
	 * 
	 * @param billable
	 * @param billed
	 */
	protected abstract void setPrice(T billable, IBilled billed);
	
	@Override
	public void putContext(String key, Object value){
	}
	
	@Override
	public void clearContext(){
	}
}
