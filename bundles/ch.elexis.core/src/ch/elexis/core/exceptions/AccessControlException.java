package ch.elexis.core.exceptions;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;

public class AccessControlException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private Class<?> clazz;
	private Right right;

	public AccessControlException(Class<?> clazz, Right right) {
		this.clazz = clazz;
		this.right = right;
	}

	@Override
	public String getMessage() {
		// use ObjectEvaluatableACE to get the inteface name that was evaluated
		ObjectEvaluatableACE ace = EvACE.of(clazz, right);
		return "User has no right [" + right + "] for class [" + ace.getObject() + "]";
	}
}
