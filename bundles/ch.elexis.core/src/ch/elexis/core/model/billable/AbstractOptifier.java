package ch.elexis.core.model.billable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.LoggerFactory;

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
	public Result<IBillable> add(IBillable billable, IEncounter encounter, double amount){
		boolean added = false;
		// lookup existing billed, add if found
		List<IBilled> existingBilled = encounter.getBilled();
		for (IBilled iBilled : existingBilled) {
			IBillable existing = iBilled.getBillable();
			if (existing.equals(billable)) {
				iBilled.setAmount(iBilled.getAmount() + amount);
				modelService.save(iBilled);
				added = true;
				break;
			}
		}
		if (!added) {
			IBilled billed = new IBilledBuilder(modelService, billable, encounter).buildAndSave();
			addBilledToEncounter(encounter, billed);
			billed.setAmount(amount);
			setPrice(billed);
			modelService.save(billed);
		}
		return new Result<IBillable>(billable);
	}
	
	protected void addBilledToEncounter(IEncounter encounter, IBilled billed){
		try {
			Method method = encounter.getClass().getDeclaredMethod("addBilled", IBilled.class);
			method.invoke(encounter, billed);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LoggerFactory.getLogger(getClass())
				.error("Could not call addBilled method of [" + encounter + "]", e);
		}
	}
	
	protected abstract void setPrice(IBilled billed);
}
