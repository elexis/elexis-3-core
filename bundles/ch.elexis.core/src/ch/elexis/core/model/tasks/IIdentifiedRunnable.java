package ch.elexis.core.model.tasks;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.Identifiable;

public interface IIdentifiedRunnable {

	/**
	 * Standardized key values for run parameters {@link Map} passed to the
	 * {@link IIdentifiedRunnable}. Be sure to handle serialized boolean values
	 * using {@link SerializableBoolean}
	 */
	public final class RunContextParameter {
		private RunContextParameter() {
		}

		/**
		 * Run-Context parameter key: If the context should in any way transport a
		 * resource that is representable as url. This can both mean a file resolvable
		 * url, or a http url. Usage form is up to the implementation.
		 */
		public static final String STRING_URL = "url";

		/**
		 * Run-Context parameter key: The reference id of a task descriptor, used to
		 * e.g. execute other tasks out of a running task
		 */
		public static final String TASK_DESCRIPTOR_REFID = "taskDescriptorReferenceId";

		/**
		 * Run-Context parameter key: The id of an {@link Identifiable}
		 */
		public static final String IDENTIFIABLE_ID = "identifiableId";

		/**
		 * Run-Context parameter value: Denotes that a value for the given key is
		 * missing (no default applicable)and required for execution. If for any entry
		 * of the runContext this value is set, execution by the TaskService will be
		 * rejected.
		 */
		public static final String VALUE_MISSING_REQUIRED = "missingRequired";
	}

	/**
	 * Standardized key values for return {@link Map} after
	 * {@link IIdentifiedRunnable} execution
	 */
	public final class ReturnParameter {
		private ReturnParameter() {
		}

		/**
		 * The full class name that is being delivered within {@link #RESULT_DATA} or
		 * {@link #RESULT_DATA_LIST}, if <code>null</code> assume {@link String}
		 */
		public static final String RESULT_CLASS = "resultClass";

		/**
		 * The actual result data for a single object. Object can be casted to
		 * {@link #RESULT_CLASS}. If multiple result objects are required, use
		 * {@link #RESULT_DATA_LIST}. Do not use both.
		 */
		public static final String RESULT_DATA = "resultData";

		/**
		 * The actual result data for multiple objects. Each object can be casted to
		 * {@link #RESULT_CLASS}. If a single result object is required use
		 * {@link #RESULT_DATA}. Do not use both.
		 */
		public static final String RESULT_DATA_LIST = "resultDataList";

		/**
		 * If the context should in any way transport a resource that is representable
		 * as url. This can both mean a file resolvable url, or a http url. Usage form
		 * is up to the implementation.
		 */
		public static final String STRING_URL = "url";

		/**
		 * The existence of this key does advise the task system to not persist the
		 * tasks results. Use this e.g. to not "log" an empty run. (e.g. a directory
		 * watcher that found nothing). When setting this to the run context, every
		 * successful execution of the task will NOT be logged. A single task may still
		 * use this as return value.
		 * 
		 * @since 3.13 keeps a single latest entry with id = taskDescriptorId
		 */
		public static final String MARKER_DO_NOT_PERSIST = "markerDoNotPersist";

		/**
		 * The existence of this key does advise the task system that there was a
		 * warning during execution. That is, the task completed successfully, but not
		 * "as intended for successful outcome". The task object will be marked
		 * TaskState.COMPLETED_WARN.
		 */
		public static final String MARKER_WARN = "markerWarn";

		/**
		 * If the task throws an exception (task fails), the message of the exception is
		 * returned using this key. This is only set if TaskState == FAILED
		 */
		public static final String FAILED_TASK_EXCEPTION_MESSAGE = "exceptionMessage";
	}

	/**
	 * Used to refer to this runnable. This value has to be unique across the
	 * system.
	 *
	 * @return
	 */
	String getId();

	/**
	 * Provide a localized human readable description of what this runnables task
	 * respectively purpose is.
	 *
	 * @return
	 */
	String getLocalizedDescription();

	/**
	 * Return a map containing all keys, that this {@link IIdentifiedRunnable} will
	 * consider during execution. Where possible, default values will be provided
	 * with a key. Values that equal
	 * {@link RunContextParameter#VALUE_MISSING_REQUIRED} must be replaced with a
	 * real value for proper execution.
	 *
	 * @return
	 */
	Map<String, Serializable> getDefaultRunContext();

	/**
	 * Execute the task. If no exception is thrown during execution, it is assumed
	 * that the task executed successfully.
	 *
	 * @param runcontext      or <code>null</code>
	 * @param progressMonitor or <code>null</code>
	 * @param logger          or <code>null</code>
	 * @return a map or <code>null</code>, use {@link ResultStatusAdapter} for
	 *         extended analysis (e.g. if task completed successfully)
	 * @throw {@link TaskException} if there was a technical error executing this
	 *        task
	 */
	Map<String, Serializable> run(Map<String, Serializable> runContext, IProgressMonitor progressMonitor, Logger logger)
			throws TaskException;

	/**
	 * @return whether only one instance of this runnable must be executed at a
	 *         given time
	 */
	default boolean isSingleton() {
		return false;
	}

}
