package ch.elexis.core.tasks.test.runnable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;

public class TestBlockingRunnable implements IIdentifiedRunnable {

	public static final String ID = "testBlockingRunnable";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getLocalizedDescription() {
		return "test simply block for some time";
	}

	@Override
	public Map<String, Serializable> getDefaultRunContext() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext, IProgressMonitor progressMonitor,
			Logger logger) throws TaskException {

		try {
			logger.info("Sleeping for 10 seconds");
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			logger.info("Being interrupted");
			e.printStackTrace();
		}

		return runContext;
	}

}
