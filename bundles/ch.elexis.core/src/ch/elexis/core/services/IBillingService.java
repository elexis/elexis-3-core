package ch.elexis.core.services;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.rgw.tools.Result;

public interface IBillingService {
	
	public Result<IEncounter> isEditable(IEncounter encounter);
	
	public Result<IBilled> bill(IBillable billable, IEncounter encounter, double amount);
}
