package ch.elexis.core.model.tasks;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

public interface IIdentifiedRunnable {
	
	public final class RunContextParameter {
		private RunContextParameter(){}
		
		/**
		 * Run-Context parameter key: If the context should in any way transport a resource that is
		 * representable as url. This can both mean a file resolvable url, or a http url. Usage form
		 * is up to the implementation.
		 */
		public static final String STRING_URL = "url";
		
		/**
		 * The reference id of a task descriptor, used to e.g. execute other tasks out of a running
		 * task
		 */
		public static final String TASK_DESCRIPTOR_REFID = "taskDescriptorReferenceId";
		
		/**
		 * Run-Context parameter value: Denotes that a value for the given key is missing (no
		 * default applicable)and required for execution
		 */
		public static final String VALUE_MISSING_REQUIRED = "missingRequired";
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
	Map<String, Serializable> getDefaultRunContext();
	
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
	 * @return
	 * @throw {@link TaskException} if there was an error executing this task
	 */
	Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException;
	
}
