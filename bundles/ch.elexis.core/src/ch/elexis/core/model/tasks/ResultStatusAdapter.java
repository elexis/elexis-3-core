package ch.elexis.core.model.tasks;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ResultStatusAdapter {
	
	/**
	 * Adapt the result of an {@link IIdentifiedRunnable} to an {@link IStatus}
	 * 
	 * @param run
	 * @return
	 */
	public static IStatus adapt(Map<String, Serializable> result){
		String resultData = (String) result.get(IIdentifiedRunnable.ReturnParameter.RESULT_DATA);
		boolean isWarning = result.containsKey(IIdentifiedRunnable.ReturnParameter.MARKER_WARN);
		return new Status(isWarning ? Status.WARNING : Status.OK, "unknown", resultData);
	}
	
}
