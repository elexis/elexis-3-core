package ch.elexis.core.model.billable;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableVerifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.rgw.tools.Result;

/**
 * Default {@link IBillableVerifier} implementation always returns OK result.
 *
 * @author thomas
 *
 */
public class DefaultVerifier implements IBillableVerifier {

	@Override
	public Result<IBillable> verifyAdd(IBillable billable, IEncounter encounter, double amount) {
		return Result.OK();
	}

	@Override
	public Result<IBilled> verify(IEncounter encounter) {
		return Result.OK();
	}
}
