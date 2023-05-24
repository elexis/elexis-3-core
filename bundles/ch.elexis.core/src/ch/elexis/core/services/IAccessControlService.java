package ch.elexis.core.services;

import ch.elexis.core.ac.EvaluatableACE;

public interface IAccessControlService {

	boolean evaluate(EvaluatableACE evaluatableAce);

}
