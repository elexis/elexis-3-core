package ch.elexis.core.model.billable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IBilledBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
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
	
	/**
	 * Get a valid {@link IBillingSystemFactor} object that is matching the system and valid on the
	 * provided date.
	 * 
	 * @param system
	 * @param date
	 * @return
	 */
	public Optional<IBillingSystemFactor> getBillingSystemFactor(String system, LocalDate date){
		IQuery<IBillingSystemFactor> query = coreModelService.getQuery(IBillingSystemFactor.class);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__SYSTEM, COMPARATOR.EQUALS, system);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_FROM,
			COMPARATOR.LESS_OR_EQUAL, date);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_TO,
			COMPARATOR.GREATER_OR_EQUAL, date);
		return query.executeSingleResult();
	}
	
	@Override
	public void putContext(String key, Object value){
	}
	
	@Override
	public void clearContext(){
	}
}
