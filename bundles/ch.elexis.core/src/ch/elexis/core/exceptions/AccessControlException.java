package ch.elexis.core.exceptions;

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
		return "User has no right [" + right + "] for class [" + clazz.getName() + "]";
	}
}
