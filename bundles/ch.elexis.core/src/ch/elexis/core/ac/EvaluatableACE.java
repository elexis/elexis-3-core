package ch.elexis.core.ac;

import ch.elexis.core.services.IAccessControlService;

// getEvaluatable -> to evaluate if the current user has the required rights
// 	evaluated via IAccessControlService
public abstract class EvaluatableACE {

	protected byte[] requestedRightMap;

	protected short requested;

	public EvaluatableACE() {
		requestedRightMap = new byte[Right.values().length];
	}

	public boolean eval(IAccessControlService accessControlService) {
		return accessControlService.evaluate(this);
	}

	/**
	 * Evaluate without database access (might not resolve constraint limited
	 * access)
	 * 
	 * @return if <code>null</code> undecided, else final decision made
	 */
	public Boolean fastEval() {

		// do resolve undecided if AOBO or SELF or other entry in DB ACL
		// else direct resolve

		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * And a requested right to the existing list
	 * 
	 * @param requestedRight
	 * @return
	 */
	public EvaluatableACE and(Right requestedRight) {
		requestedRightMap[requestedRight.ordinal()] = 1;
		requested |= 1 << requestedRight.ordinal();
		return this;
	}
}
