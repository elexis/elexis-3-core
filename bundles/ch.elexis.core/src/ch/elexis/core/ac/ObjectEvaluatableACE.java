package ch.elexis.core.ac;

import ch.elexis.core.services.IAccessControlService;

public class ObjectEvaluatableACE extends EvaluatableACE {

	private final String object;
	private final String objectId;

	/**
	 * 
	 * @param object
	 * @param requestedRight
	 * @param objectId       the specific object the right is queried for. If no
	 *                       objectId is given, the request counts for global
	 *                       access, i.e. "*"
	 */
	public ObjectEvaluatableACE(String object, Right requestedRight, String objectId) {
		super();
		this.object = object;
		this.objectId = objectId;
		requestedRightMap[requestedRight.ordinal()] = 1;
	}

	public ObjectEvaluatableACE(String object, Right requestedRight) {
		this(object, requestedRight, null);
	}

	public ObjectEvaluatableACE(Class<?> clazz, Right requestedRight) {
		this(clazz.getName(), requestedRight, null);
	}

	/**
	 * @see #ObjectEvaluatableACE(String, Right, String)
	 */
	public ObjectEvaluatableACE(Class<?> clazz, Right requestedRight, String objectId) {
		this(clazz.getName(), requestedRight, objectId);
	}

	public String getObject() {
		return object;
	}

	public String getObjectId() {
		return objectId;
	}

	public boolean evaluate(IAccessControlService iac) {
		return iac.evaluate(this);
	}

	public byte[] getRequestedRightMap() {
		return requestedRightMap;
	}

}
