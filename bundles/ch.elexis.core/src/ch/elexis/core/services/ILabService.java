package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
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
	
	/**
	 *
	 * @param contact
	 * @param item
	 * @return
	 */
	public Optional<ILabMapping> getLabMappingByContactAndItem(IContact contact, ILabItem item);
	
}
