package ch.elexis.core.ui.p2;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;


/**
 * Source code from
 * <ul>
 * <li><a href="http://wiki.eclipse.org/Equinox/p2/Adding_Self-Update_to_an_RCP_Application">Equinox Wiki pages, Adding self update</a></li>
 * </ul>
 * 
 * @author mahieddine.ichir@free.fr
 */
public class P2Util {

	/**
	 * Check for application updates.
	 * @param agent
	 * @param monitor
	 * @return
	 * @throws OperationCanceledException
	 */
	public static IStatus checkForUpdates(IProvisioningAgent agent, IProgressMonitor monitor) throws OperationCanceledException {
		System.out.println(">> checkForUpdates");
		ProvisioningSession session = new ProvisioningSession(agent);
		UpdateOperation operation = new UpdateOperation(session);
		SubMonitor sub = SubMonitor.convert(monitor, "Checking for application updates...", 200);
		return operation.resolveModal(sub.newChild(100));
	}
	
	/**
	 * Download and install application updates.
	 * @param agent
	 * @param monitor
	 * @return
	 * @throws OperationCanceledException
	 */
	public static IStatus installUpdates(IProvisioningAgent agent, IProgressMonitor monitor) throws OperationCanceledException {
		ProvisioningSession session = new ProvisioningSession(agent);
		UpdateOperation operation = new UpdateOperation(session);
		SubMonitor sub = SubMonitor.convert(monitor, "Installing updates ...", 200);
		operation.resolveModal(sub.newChild(100));
		ProvisioningJob job = operation.getProvisioningJob(monitor);
		return job.runModal(sub.newChild(100));
	}

	/**
	 * Add a repository to declared updates repositories.
	 * @param repo
	 * @return
	 */
	public static boolean addRepository(IProvisioningAgent agent, String repo) {
		System.out.println(">> adding repository "+repo);
		IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
		IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (metadataManager == null) {
			System.out.println("metadataManager is null!!!");
			return false;
		}
		if (artifactManager == null) {
			System.out.println("artifactManager is null!!!");
			return false;
		}
		try {
			URI uri = new URI(repo);
			metadataManager.addRepository(uri);
			artifactManager.addRepository(uri);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
