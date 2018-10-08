package ch.elexis.core.services;

import ch.elexis.core.model.ILabResult;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.Result;

public interface ILabService {
	
	/**
	 * Evaluate a {@link ILabResult} of {@link LabItemTyp#FORMULA}
	 * 
	 * @param labResult
	 * @return contains the result, and a flag if it could be correctly calculated
	 */
	public Result<String> evaluate(ILabResult labResult);
	
}
