package ch.elexis.core.fhir.rdus;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.status.ObjectStatus;

/**
 * Medelexis Reference Data Update Service <br>
 * Synchronize local entities and remote FHIR resources with
 * https://fhir.medelexis.ch. For a synced object an XID denoting the local
 * known version is stored.
 */
public interface IResourceSynchronizer {

	/**
	 * import a remote entity:<br>
	 * a) we don't have it yet -> import <br>
	 * b) we have it, but remote is newer -> import <br>
	 * c) we have it, remote is same -> ignore <br>
	 * d) we have it, remote is deleted -> user decides
	 * 
	 * @param entityUrl e.g. <code>Person/medelexis-ict-contact</code>
	 * @return if {@link IStatus#OK} returns an {@link ObjectStatus} containing the
	 *         local resource
	 */
	public IStatus pull(String entityUrl);

	/**
	 * REQUIRES Medelexis authentication and role export entity to remote <br>
	 * https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations.html#contention-aware-updating
	 * <br>
	 * a) server doesn't know it -> export <br>
	 * b) server knows it, and has newer -> info to perform importEntity <br>
	 * c) server knows it, and has older -> export (creates new version on remote)
	 */
	public IStatus push(Identifiable localObject, String bearerToken);

	/**
	 * 
	 * @param fhirResourceId
	 * @param bearerToken
	 * @return
	 */
	public IStatus delete(String fhirResourceId, String bearerToken);
}
