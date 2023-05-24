package ch.elexis.core.ac;

public class SystemCommandEvaluatableACE extends EvaluatableACE {

	private final String systemCommandId;

	public SystemCommandEvaluatableACE(String systemCommandId, Right execute) {
		this.systemCommandId = systemCommandId;
		requestedRightMap[Right.EXECUTE.ordinal()] = 1;
	}

	public String getSystemCommandId() {
		return systemCommandId;
	}
}
