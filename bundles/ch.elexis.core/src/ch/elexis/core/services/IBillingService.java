package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.Optional;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IEncounter;
import ch.rgw.tools.Result;

public interface IBillingService {
	
	/**
	 * Get a {@link IBillingSystemFactor} for the system at the provided date, empty if no such factor is defined.
	 * 
	 * @param system
	 * @param date
	 * @return
	 */
	public Optional<IBillingSystemFactor> getBillingSystemFactor(String system, LocalDate date);
	
	/**
	 * Test if the {@link IEncounter} is editable in the context of billing.
	 * 
	 * @param encounter
	 * @return
	 */
	public Result<IEncounter> isEditable(IEncounter encounter);
	
	/**
	 * Try to bill the amount of {@link IBillable} using the {@link IEncounter}. Test the result to
	 * see if billing was successful or there was a problem.
	 * 
	 * @param billable
	 * @param encounter
	 * @param amount
	 * @return a Result, that may contain a message even if its ok
	 */
	public Result<IBilled> bill(IBillable billable, IEncounter encounter, double amount);
}
