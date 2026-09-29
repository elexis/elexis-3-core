package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IEncounter;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public interface IBillingService {

	/**
	 * Get a {@link IBillingSystemFactor} for the system at the provided date, empty
	 * if no such factor is defined.
	 *
	 * @param system
	 * @param date
	 * @return
	 */
	public Optional<IBillingSystemFactor> getBillingSystemFactor(String system, LocalDate date);

	/**
	 *
	 * @param from
	 * @param to
	 * @param factor
	 * @param system
	 */
	public void setBillingSystemFactor(LocalDate from, LocalDate to, double factor, String system);

	/**
	 * Test if the {@link IEncounter} is editable in the context of billing.
	 *
	 * @param encounter
	 * @return
	 */
	public Result<IEncounter> isEditable(IEncounter encounter);

	/**
	 * Try to bill the amount of {@link IBillable} using the {@link IEncounter}.
	 * Test the result to see if billing was successful or there was a problem.
	 *
	 * @param billable
	 * @param encounter
	 * @param amount
	 * @return a {@link Result} that returns a {@link SEVERITY#WARNING} if only a
	 *         partial amount could be billed
	 */
	public Result<IBilled> bill(IBillable billable, IEncounter encounter, double amount);

	/**
	 * Try to bill the amount of {@link IBillable} using the {@link IEncounter},
	 * see {@link #bill(IBillable, IEncounter, double)}. If the billable is an
	 * article, it is only disposed from stock if stockDisposal is
	 * <code>true</code>. Use <code>false</code> if the stock is managed by the
	 * caller, e.g. for patient medication orders.
	 *
	 * @param billable
	 * @param encounter
	 * @param amount
	 * @param stockDisposal
	 * @return a {@link Result} that returns a {@link SEVERITY#WARNING} if only a
	 *         partial amount could be billed
	 */
	public Result<IBilled> bill(IBillable billable, IEncounter encounter, double amount, boolean stockDisposal);

	/**
	 * Remove a billed service from the encounter. This will only work if the
	 * encounter is editable. This method additionally takes care of side-effects
	 * like e.g. returning an article to stock if removed
	 *
	 * @param billed
	 * @param encounter
	 * @return
	 */
	public Result<?> removeBilled(IBilled billed, IEncounter encounter);

	/**
	 * Change the amount for this service or article, considering the rules given by
	 * the resp. optifiers.
	 *
	 * @param billed
	 * @param newCount
	 * @return
	 */
	public IStatus changeAmountValidated(IBilled billed, double newAmount);

	/**
	 * Change the amount for this service or article. If it is an {@link IArticle},
	 * the store will be updated accordingly
	 *
	 * @param newAmount new count this service is to be billed.
	 */
	void changeAmount(IBilled billed, double newAmount);
}
