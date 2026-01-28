package ch.elexis.core.model.tasks;

import org.apache.commons.lang3.StringUtils;

import ch.rgw.tools.Result;

public class TaskException extends Exception {
	/**
	 * The task service was rejecting the execution of this task
	 */
	public static final int EXECUTION_REJECTED = 1;
	/**
	 * The runnable's id is invalid
	 */
	public static final int RWC_INVALID_ID = 2;
	/**
	 * The task service could not instantiate the runnable
	 */
	public static final int RWC_NO_INSTANCE_FOUND = 3;
	public static final int PERSISTENCE_ERROR = 4;
	public static final int PARAMETERS_MISSING = 5;
	/**
	 * An error occured during execution of this task, please see the message for
	 * further details
	 */
	public static final int EXECUTION_ERROR = 6;
	public static final int TRIGGER_REGISTER_ERROR = 7;
	public static final int TRIGGER_NOT_AVAILABLE = 8;

	private static final long serialVersionUID = -6228358636762420555L;

	private final int exceptionCode;

	public TaskException(int exceptionCode, String message, Throwable re) {
		super(message, re);
		this.exceptionCode = exceptionCode;
	}

	public TaskException(int exceptionCode, String message) {
		super(message);
		this.exceptionCode = exceptionCode;
	}

	public TaskException(int exceptionCode, Throwable re) {
		this(exceptionCode, (re != null) ? re.getMessage() : null, re);
	}

	public TaskException(int exceptionCode) {
		super();
		this.exceptionCode = exceptionCode;
	}

	public TaskException(int exceptionCode, @SuppressWarnings("rawtypes") Result result) {
		this(exceptionCode, (result != null) ? result.toString() : StringUtils.EMPTY);
	}

	public int getExceptionCode() {
		return exceptionCode;
	}

	public static final TaskException EXECUTION_ERROR(String message) {
		return new TaskException(EXECUTION_ERROR, message);
	}

	public static final TaskException EXECUTION_ERROR(String message, Throwable throwable) {
		return new TaskException(EXECUTION_ERROR, message, throwable);
	}

	@Override
	public String toString() {
		String s = getClass().getName();
		String message = getLocalizedMessage();
		return (message != null) ? (s + ": code " + exceptionCode + " - " + message) : s;
	}
}
