package ch.elexis.core.status;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Extends {@link Status} allowing to carry a simple object with the status
 */
public class ObjectStatus extends Status {
	
	/**
	 * Constant used to indicate an unknown plugin id.
	 */
	private static final String unknownId = "unknown"; //$NON-NLS-1$
	
	private final Object object;
	
	public ObjectStatus(int severity, String pluginId, int code, String message,
		Throwable exception, Object object){
		super(severity, pluginId, code, message, exception);
		this.object = object;
	}
	
	public ObjectStatus(int severity, String pluginId, String message, Throwable exception,
		Object object){
		super(severity, pluginId, message, exception);
		this.object = object;
	}
	
	public ObjectStatus(int severity, String pluginId, String message, Object object){
		super(severity, pluginId, message);
		this.object = object;
	}
	
	public ObjectStatus(IStatus status, Object object) {
		super(status.getSeverity(), status.getPlugin(), status.getMessage());
		this.object = object;
	}
	
	public Object getObject(){
		return object;
	}
	
	/**
	 * Default status without pluginId and default message
	 * 
	 * @param object
	 * @return
	 */
	public static ObjectStatus OK_STATUS(Object object){
		return new ObjectStatus(OK, unknownId, OK, "ok", null, object);
	}
	
	/**
	 * Default status without pluginId and default message
	 * 
	 * @param object
	 * @return
	 */
	public static ObjectStatus CANCEL_STATUS(Object object){
		return new ObjectStatus(CANCEL, unknownId, CANCEL, "cancel", null, object);
	}
	
	/**
	 * Default status without pluginId and default message
	 * 
	 * @param object
	 * @return
	 */
	public static ObjectStatus INFO_STATUS(Object object){
		return new ObjectStatus(INFO, unknownId, INFO, "info", null, object);
	}
	
	/**
	 * Default status without pluginId and default message
	 * 
	 * @param object
	 * @return
	 */
	public static ObjectStatus ERROR_STATUS(Object object){
		return new ObjectStatus(ERROR, unknownId, ERROR, "error", null, object);
	}
	
	public static ObjectStatus ERROR_STATUS(Object object, Throwable exception){
		return new ObjectStatus(ERROR, unknownId, ERROR, "error", exception, object);
	}
}
