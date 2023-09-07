package ch.elexis.core.ac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
				Optional<String> canidate = canidates.stream().filter(s -> {
					String lSimplename = s.substring(s.lastIndexOf('.') + 1);
					return Character.isUpperCase(lSimplename.charAt(0)) && Character.isUpperCase(lSimplename.charAt(1));
				}).findFirst();
				if (canidate.isPresent()) {
					return canidate.get();
				}
			}
		}
		return clazz.getName();
	}

	/**
	 * @see #ObjectEvaluatableACE(String, Right, String)
	 */
	public ObjectEvaluatableACE(Class<?> clazz, Right requestedRight, String storeToString) {
		this(getElexisInterfaceName(clazz), requestedRight, storeToString);
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

	@Override
	public String toString() {
		return object + "#" + Arrays.toString(requestedRightMap);
	}

}
