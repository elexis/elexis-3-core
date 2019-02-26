package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.interfaces.IReferenceDataImporter;

/**
 * Service interface for accessing {@link IReferenceDataImporter} implementations by the
 * {@link IReferenceDataImporter#REFERENCEDATAID} property.
 * 
 * @author thomas
 *
 */
public interface IReferenceDataImporterService {

	/**
	 * Get the {@link IReferenceDataImporter} service registered with a matching
	 * {@link IReferenceDataImporter#REFERENCEDATAID} property.
	 * 
	 * @param referenceDataId
	 * @return
	 */
	public Optional<IReferenceDataImporter> getImporter(String referenceDataId);
}
