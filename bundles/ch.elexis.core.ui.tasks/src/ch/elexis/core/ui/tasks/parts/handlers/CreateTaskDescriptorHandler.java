
package ch.elexis.core.ui.tasks.parts.handlers;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskService;

public class CreateTaskDescriptorHandler {

	@Execute
	public void execute(ITaskService taskService) {
		try {
			taskService.createTaskDescriptor(new IIdentifiedRunnable() {

				@Override
				public String getLocalizedDescription() {
					return StringUtils.EMPTY;
				}

				@Override
				public String getId() {
					return StringUtils.EMPTY;
				}

				@Override
				public Map<String, Serializable> getDefaultRunContext() {
					return Collections.emptyMap();
				}

				@Override
				public Map<String, Serializable> run(Map<String, Serializable> runContext,
						IProgressMonitor progressMonitor, Logger logger) throws TaskException {
					return Collections.emptyMap();
				}
			});
		} catch (TaskException e) {
			LoggerFactory.getLogger(getClass()).warn("Could not create iTaskDescriptor", e); //$NON-NLS-1$
		}

	}

}