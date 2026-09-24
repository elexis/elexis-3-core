package ch.elexis.core.model.billable;

import java.util.List;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IBilledBuilder;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Result;

public abstract class AbstractOptifier<T extends IBillable> implements IBillableOptifier<T> {

	protected IModelService coreModelService;
	protected IContextService contextService;

	/**
	 * Create an {@link AbstractOptifier} instance, and provide an
	 * {@link IModelService} for accessing the ch.elexis.core model.
	 *
	 * @param coreModelService
	 */
	public AbstractOptifier(IModelService coreModelService, IContextService contextService) {
		this.coreModelService = coreModelService;
		this.contextService = contextService;
	}

	@Override
	public Result<IBilled> add(T billable, IEncounter encounter, double amount, boolean save) {
		boolean added = false;
		IBilled billed = null;
		// lookup existing billed, add if found
		List<IBilled> existingBilled = encounter.getBilled();
		for (IBilled iBilled : existingBilled) {
			IBillable existing = iBilled.getBillable();
			if (existing != null && existing.equals(billable)) {
				setAmount(iBilled, iBilled.getAmount() + amount);
				if (save) {
					coreModelService.save(iBilled);
				}
				billed = iBilled;
				added = true;
				break;
			}
		}
		if (!added) {
			IContact activeUserContact = contextService.getActiveUserContact().get();
			billed = new IBilledBuilder(coreModelService, billable, encounter, activeUserContact).build();
			setAmount(billed, amount);
			setPrice(billable, billed);
			if (save) {
				coreModelService.save(billed);
			}
		}
		return new Result<>(billed);
	}

	@Override
	public Result<IBilled> remove(IBilled billed, IEncounter encounter) {
		encounter.removeBilled(billed);
		return new Result<>(billed);
	}

	/**
	 * Set the amount of the {@link IBillable}, override if additional changes
	 * should be done on change of amount.
	 *
	 * @param billed
	 * @param amount
	 */
	protected void setAmount(IBilled billed, double amount) {
		billed.setAmount(amount);
	}

	/**
	 * Set the actual price of <b>one</b> billable in the {@link IBilled} instance.
	 * The encounter reference of the {@link IBilled} should be set.
	 *
	 * @param billable
	 * @param billed
	 */
	protected abstract void setPrice(T billable, IBilled billed);

	@Override
	public void putContext(String key, Object value) {
	}

	@Override
	public void clearContext() {
	}
}
