package ch.elexis.core.ac;

import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.utils.OsgiServiceUtil;

// getEvaluatable -> to evaluate if the current user has the required rights
// 	evaluated via IAccessControlService
public abstract class EvaluatableACE {

	private static IAccessControlService iacs;

	protected byte[] requestedRightMap;

	public EvaluatableACE() {
		requestedRightMap = new byte[Right.values().length];
	}

	private IAccessControlService getAccessControlService() {
		if (iacs == null) {
			iacs = OsgiServiceUtil.getService(IAccessControlService.class).get();
		}
		return iacs;
	}

	public boolean eval() {
		return getAccessControlService().evaluate(this);
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
		return this;
	}

}
