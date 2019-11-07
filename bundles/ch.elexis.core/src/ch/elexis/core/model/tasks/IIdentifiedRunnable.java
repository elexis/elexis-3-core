package ch.elexis.core.model.tasks;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.Identifiable;

public interface IIdentifiedRunnable {
	
	/**
	 * Standardized key values for run parameters {@link Map} passed to the
	 * {@link IIdentifiedRunnable}
	 */
	public final class RunContextParameter {
		private RunContextParameter(){}
		
		/**
		 * Run-Context parameter key: If the context should in any way transport a resource that is
		 * representable as url. This can both mean a file resolvable url, or a http url. Usage form
		 * is up to the implementation.
		 */
		public static final String STRING_URL = "url";
		
		/**
		 * Run-Context parameter key: The reference id of a task descriptor, used to e.g. execute
		 * other tasks out of a running task
		 */
		public static final String TASK_DESCRIPTOR_REFID = "taskDescriptorReferenceId";
		
		/**
		 * Run-Context parameter key: The id of an {@link Identifiable}
		 */
		public static final String IDENTIFIABLE_ID = "identifiableId";
		
		/**
		 * Run-Context parameter value: Denotes that a value for the given key is missing (no
		 * default applicable)and required for execution
		 */
		public static final String VALUE_MISSING_REQUIRED = "missingRequired";
	}
	
	/**
	 * Standardized key values for return {@link Map} after {@link IIdentifiedRunnable} execution
	 */
	public final class ReturnParameter {
		private ReturnParameter(){}
		
		/**
		 * The class that is being delivered within {@link #RESULT_DATA}, if <code>null</code>
		 * assume {@link String}
		 */
		public static final String RESULT_CLASS = "resultClass";
		
		/**
		 * The actual result data, can be casted to {@link #RESULT_CLASS}
		 */
		public static final String RESULT_DATA = "resultData";
		
		/**
		 * The existence of this key does advise the task system to not persist the tasks results.
		 * Use this e.g. to not "log" an empty run. (e.g. a directory watcher that found nothing).
		 * When setting this to the run context, every successful execution of the task will NOT be
		 * logged. A single task may still use this as return value.
		 */
		public static final String MARKER_DO_NOT_PERSIST = "markerDoNotPersist";
		
		/**
		 * If the task throws an exception (task fails), the message of the exception is returned
		 * using this key.
		 */
		public static final String FAILED_TASK_EXCEPTION_MESSAGE = "exceptionMessage";
	}
	
	/**
	 * Used to refer to this runnable. This value has to be unique across the system.
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * Provide a localized human readable description of what this runnables task respectively
	 * purpose is.
	 * 
	 * @return
	 */
	String getLocalizedDescription();
	
	/**
	 * Return a map containing all keys, that this {@link IIdentifiedRunnable} will consider during
	 * execution. Where possible, default values will be provided with a key. Values that equal
	 * {@link #RCP_MISSING_REQUIRED} must be replaced with a real value for proper execution.
	 * 
	 * @return
	 */
	Map<String, String> getDefaultRunContext();
	
	/**
	 * Execute the task. If no exception is thrown during execution, it is assumed that the task
	 * executed successfully.
	 * 
	 * @param runcontext
	 *            or <code>null</code>
	 * @param progressMonitor
	 *            or <code>null</code>
	 * @param logger
	 *            or <code>null</code>
	 * @return a map or <code>null</code>
	 * @throw {@link TaskException} if there was an error executing this task
	 */
	Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException;
	
	/**
	 * 
	 * @return
	 */
	default Map<String, String> getTaskDescriptorDefaults(){
		return Collections.emptyMap();
	}
	
}
