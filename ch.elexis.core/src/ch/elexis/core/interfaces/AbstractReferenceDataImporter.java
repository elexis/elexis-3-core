package ch.elexis.core.interfaces;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;

public abstract class AbstractReferenceDataImporter {
	
	/**
	 * @return the class this reference data importer is responsible for
	 */
	public abstract @NonNull
	Class<?> getReferenceDataTypeResponsibleFor();
	
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
	public abstract IStatus performImport(@Nullable
	IProgressMonitor ipm, @NonNull
	InputStream input, @Nullable
	Integer newVersion);
	
	/**
	 * @return the current local version of the dataset, this is specific to the dataset. If the
	 *         update service propagates and int value higher than the one returned here,
	 *         {@link #performImport(IProgressMonitor, InputStream)} will be called. If the version
	 *         is not determined, return -1
	 */
	public abstract int getCurrentVersion();
	
}
