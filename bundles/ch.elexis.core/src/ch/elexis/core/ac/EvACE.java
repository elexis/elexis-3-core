package ch.elexis.core.ac;

/**
 * Convenience methods for instantiating {@link EvaluatableACE}
 */
public class EvACE {

	/**
	 * ObjectEvaluatableACE#ObjectEvaluatableACE(Class, Right)
	 */
	public static ObjectEvaluatableACE of(Class<?> clazz, Right right) {
		return new ObjectEvaluatableACE(clazz, right);
	}

	/**
	 * @see ObjectEvaluatableACE#ObjectEvaluatableACE(Class, Right, String)
	 */
	public static EvaluatableACE of(Class<?> clazz, Right right, String id) {
		return new ObjectEvaluatableACE(clazz, right, id);
	}

	/**
	 * Request permission for a system command, defaults to Right.EXECUTE
	 * 
	 * @param systemCommandId
	 * @return
	 * @see SystemCommandConstants
	 */
	public static EvaluatableACE of(String systemCommandId) {
		return new SystemCommandEvaluatableACE(systemCommandId, Right.EXECUTE);
	}

	public static EvaluatableACE of(ConfigurationScope local, Right update, String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
