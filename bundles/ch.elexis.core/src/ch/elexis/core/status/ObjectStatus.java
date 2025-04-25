package ch.elexis.core.status;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Extends {@link Status} allowing to carry a simple object with the status
 * 
 * @since 3.12 supported typed ObjectStatus
 */
public class ObjectStatus<T> extends Status {

	/**
	 * Constant used to indicate an unknown plugin id.
	 */
	private static final String unknownId = "unknown"; //$NON-NLS-1$

	private final T object;

	public ObjectStatus(int severity, String pluginId, int code, String message, Throwable exception, T object) {
		super(severity, pluginId, code, message, exception);
		this.object = object;
	}

	public ObjectStatus(int severity, String pluginId, String message, Throwable exception, T object) {
		super(severity, pluginId, message, exception);
		this.object = object;
	}

	public ObjectStatus(int severity, String pluginId, String message, T object) {
		super(severity, pluginId, message);
		this.object = object;
	}

	public ObjectStatus(IStatus status, T object) {
		super(status.getSeverity(), status.getPlugin(), status.getMessage());
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	public T get() {
		return object;
	}

	/**
	 * Default status without pluginId and default message
	 *
	 * @param object
	 * @return
	 */
	public static <T> ObjectStatus<T> OK_STATUS(T object) {
		return new ObjectStatus<>(OK, unknownId, OK, "ok", null, object);
	}

	public static final <T> ObjectStatus<T> OK_STATUS(String message, T object) {
		return new ObjectStatus<>(OK, unknownId, OK, message, null, object);
	}

	/**
	 * Default status without pluginId and default message
	 *
	 * @param object
	 * @return
	 */
	public static <T> ObjectStatus<T> WARNING_STATUS(T object) {
		return new ObjectStatus<>(WARNING, unknownId, WARNING, "warn", null, object);
	}

	/**
	 * Default status without pluginId and default message
	 *
	 * @param object
	 * @return
	 */
	public static <T> ObjectStatus<T> CANCEL_STATUS(T object) {
		return new ObjectStatus<>(CANCEL, unknownId, CANCEL, "cancel", null, object);
	}

	/**
	 * Default status without pluginId and default message
	 *
	 * @param object
	 * @return
	 */
	public static <T> ObjectStatus<T> INFO_STATUS(T object) {
		return new ObjectStatus<>(INFO, unknownId, INFO, "info", null, object);
	}

	/**
	 * Default status without pluginId and default message
	 *
	 * @param object
	 * @return
	 */
	public static <T> ObjectStatus<T> ERROR_STATUS(T object) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, "error", null, object);
	}

	public static <T> ObjectStatus<T> ERROR_STATUS(String message, Throwable exception) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, message, exception, null);
	}

	public static <T> ObjectStatus<T> ERROR_STATUS(T object, Throwable exception) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, "error", exception, object);
	}

	public static <T> ObjectStatus<T> OK(T object) {
		return new ObjectStatus<>(OK, unknownId, OK, "ok", null, object);
	}

	public static final <T> ObjectStatus<T> OK(String message, T object) {
		return new ObjectStatus<>(OK, unknownId, OK, message, null, object);
	}

	public static <T> ObjectStatus<T> CANCEL(T object) {
		return new ObjectStatus<>(CANCEL, unknownId, CANCEL, "cancel", null, object);
	}

	public static <T> ObjectStatus<T> INFO(T object) {
		return new ObjectStatus<>(INFO, unknownId, INFO, "info", null, object);
	}

	public static <T> ObjectStatus<T> INFO(String message) {
		return new ObjectStatus<>(INFO, unknownId, ERROR, message, null, null);
	}

	public static <T> ObjectStatus<T> WARNING(T object) {
		return new ObjectStatus<>(WARNING, unknownId, WARNING, "warn", null, object);
	}

	public static <T> ObjectStatus<T> ERROR(T object) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, "error", null, object);
	}

	public static <T> ObjectStatus<T> ERROR_STATUS(String message) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, message, null, null);
	}

	public static <T> ObjectStatus<T> ERROR(String message) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, message, null, null);
	}

	public static <T> ObjectStatus<T> ERROR(String message, Throwable exception) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, message, exception, null);
	}

	public static <T> ObjectStatus<T> ERROR(T object, Throwable exception) {
		return new ObjectStatus<>(ERROR, unknownId, ERROR, "error", exception, object);
	}
}
