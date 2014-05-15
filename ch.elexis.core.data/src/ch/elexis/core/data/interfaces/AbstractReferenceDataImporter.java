package ch.elexis.core.data.interfaces;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.jdt.Nullable;

public abstract class AbstractReferenceDataImporter {
	
	/**
	 * @return the id of the reference data set this importer is responsible for
	 */
	public abstract String getReferenceDataIdResponsibleFor();
	
	/**
	 * 
	 * @param ipm
	 *            optional {@link IProgressMonitor} class, may be <code>null</code> as used in UI
	 *            only
	 * @param input
	 *            the input to be imported
	 * @return {@link IStatus#OK} for a successful import, {@link IStatus#ERROR} for any other case,
	 *         where in the case of an error the data set version will not be increased.
	 */
	public abstract IStatus performImport(@Nullable IProgressMonitor ipm, InputStream input);
	
}
