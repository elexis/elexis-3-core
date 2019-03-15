package ch.elexis.core.model.tasks;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

public interface IIdentifiedRunnable {
	
	/**
	 * Used to refer to this runnable. This value has to be unique across the system.
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * 
	 * @param context
	 *            or <code>null</code>
	 * @param progressMonitor
	 *            or <code>null</code>
	 * @param logger
	 *            or <code>null</code>
	 * @return
	 */
	Map<String, Serializable> run(Map<String, Serializable> context,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException;
	
}
