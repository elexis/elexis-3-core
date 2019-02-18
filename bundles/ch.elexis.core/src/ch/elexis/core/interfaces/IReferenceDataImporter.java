package ch.elexis.core.interfaces;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;

/**
 * Service interface for reference data importers. Implementations should provide the service
 * property {@link IReferenceDataImporter#REFERENCEDATAID}, so clients can choose the desired
 * service.
 * 
 * @author thomas
 *
 */
public interface IReferenceDataImporter {
	
	public String REFERENCEDATAID = "referenceDataId";
	
	/**
	 * 
	 * @param ipm
	 *            optional {@link IProgressMonitor} class, may be <code>null</code> as used in UI
	 *            only
	 * @param input
	 *            the input to be imported
	 * @param newVersion
	 *            the new version number to set, may be <code>null</code>
	 * @return {@link IStatus#OK} for a successful import, {@link IStatus#ERROR} for any other case,
	 *         where in the case of an error the data set version will not be increased.
	 */
	public abstract IStatus performImport(@Nullable IProgressMonitor ipm,
		@NonNull InputStream input, @Nullable Integer newVersion);
}
