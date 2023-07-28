package ch.elexis.core.ac;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.services.IAccessControlService;

public class ObjectEvaluatableACE extends EvaluatableACE {

	private final String object;
	private final String storeToString;

	/**
	 * 
	 * @param object
	 * @param requestedRight
	 * @param storeToString  the specific object the right is queried for. If no
	 *                       storeToString is given, the request counts for global
	 *                       access, i.e. "*"
	 */
	public ObjectEvaluatableACE(String object, Right requestedRight, String storeToString) {
		super();
		this.object = object;
		this.storeToString = storeToString;
		requestedRightMap[requestedRight.ordinal()] = 1;

		requested |= 1 << requestedRight.ordinal();
	}

	public ObjectEvaluatableACE(String object, Right requestedRight) {
		this(object, requestedRight, null);
	}

	public ObjectEvaluatableACE(Class<?> clazz, Right requestedRight) {
		this(getElexisInterfaceName(clazz), requestedRight, null);
	}

	private static String getElexisInterfaceName(Class<?> clazz) {
		if (!clazz.isInterface()) {
			List<String> canidates = new ArrayList<>();
			for (Class<?> interfaze : clazz.getInterfaces()) {
				canidates.add(interfaze.getName());
			}
			if (!canidates.isEmpty()) {
				canidates.sort((l, r) -> {
					String lSimplename = l.substring(l.lastIndexOf('.'));
					boolean lFirstLetters = Character.isUpperCase(lSimplename.charAt(0))
							&& Character.isUpperCase(lSimplename.charAt(1));
					String rSimplename = r.substring(r.lastIndexOf('.'));
					boolean rFirstLetters = Character.isUpperCase(lSimplename.charAt(0))
							&& Character.isUpperCase(lSimplename.charAt(1));
					if (lFirstLetters && !rFirstLetters) {
						return 1;
					} else if (rFirstLetters && !lFirstLetters) {
						return -1;
					} else {
						return lSimplename.compareTo(rSimplename);
					}
				});
				return canidates.get(0);
			}
		}
		return clazz.getName();
	}

	/**
	 * @see #ObjectEvaluatableACE(String, Right, String)
	 */
	public ObjectEvaluatableACE(Class<?> clazz, Right requestedRight, String storeToString) {
		this(clazz.getName(), requestedRight, storeToString);
	}

	public String getObject() {
		return object;
	}

	public boolean evaluate(IAccessControlService iac) {
		return iac.evaluate(this);
	}

	public byte[] getRequestedRightMap() {
		return requestedRightMap;
	}

	public short getRequested() {
		return requested;
	}

	public String getStoreToString() {
		return storeToString;
	}

}
