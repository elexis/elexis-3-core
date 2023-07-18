package ch.elexis.core.services;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.exceptions.AccessControlException;

public interface IAccessControlService {

	boolean evaluate(EvaluatableACE evaluatableAce) throws AccessControlException;

}
