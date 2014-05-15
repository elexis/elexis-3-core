package ch.elexis.core.data.extension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.AbstractReferenceDataImporter;

/**
 * This class offers a centralized contact point for reference data imports. It forwards the update
 * to the appropriate importer. This update forwarding effects all importers included as
 * ExtensionPoint {@code ch.elexis.core.data.referenceDataImporter} and extending the
 * {@link AbstractReferenceDataImporter} class
 * 
 * @author Lucia
 *
 */
public class ReferenceDataImporterExtensionPoint {
	private static Logger log = LoggerFactory.getLogger(ReferenceDataImporterExtensionPoint.class
		.getName());
	
	private static final String CLASS_PROPERTY = "class";
	private static List<AbstractReferenceDataImporter> importers;
	private static ReferenceDataImporterExtensionPoint instance = null;
	
	/**
	 * loads all ExtensionPoints of type {@code ExtensionPointConstantsData.REFERENCE_DATA_IMPORTER}
	 * and adds them to a list of available importers
	 */
	private ReferenceDataImporterExtensionPoint(){
		try {
			importers = new ArrayList<AbstractReferenceDataImporter>();
			
			// load reference-data-extensionpoint
			IExtensionPoint refDataExtensionPoint =
				RegistryFactory.getRegistry().getExtensionPoint(
					ExtensionPointConstantsData.REFERENCE_DATA_IMPORTER);
			IConfigurationElement[] extensionPoints =
				refDataExtensionPoint.getConfigurationElements();
			
			// add all found extensionPoints to the importer list
			for (IConfigurationElement ePoint : extensionPoints) {
				Object o;
				
				o = ePoint.createExecutableExtension(CLASS_PROPERTY);
				
				if (o instanceof AbstractReferenceDataImporter) {
					AbstractReferenceDataImporter importer = (AbstractReferenceDataImporter) o;
					importers.add(importer);
					
					log.debug("Added ReferenceDataImporter for... "
						+ importer.getReferenceDataIdResponsibleFor());
				}
			}
		} catch (CoreException e) {
			log.error("Exception occured trying to load ReferenceDataImporter ExtensionPoints", e);
		}
	}
	
	/**
	 * performs an update using the importer given by the id
	 * 
	 * @param importerId
	 *            the id of the importer to use
	 * @param inputStream
	 *            a {@link InputStream} containing the data to be imported
	 * @return {@link IStatus#OK} for a successful import, {@link IStatus#ERROR} for any other case,
	 *         where in the case of an error the data set version will not be increased.
	 */
	public static IStatus update(String importerId, IProgressMonitor ipm, InputStream inputStream){
		for (AbstractReferenceDataImporter importer : getImporters()) {
			if (importerId.equals(importer.getReferenceDataIdResponsibleFor())) {
				if (ipm == null)
					ipm = new NullProgressMonitor();
				return importer.performImport(ipm, inputStream);
			}
		}
		return new Status(IStatus.ERROR, CoreHub.PLUGIN_ID, "No Importer found for id: "
			+ importerId);
	}
	
	/**
	 * get all ReferenceDataImporters found via the referenceDataImporter ExtensionPoint
	 * 
	 * @return a list of AbstractReferenceDataImporters
	 */
	public static List<AbstractReferenceDataImporter> getImporters(){
		if (instance == null) {
			instance = new ReferenceDataImporterExtensionPoint();
		}
		return importers;
	}
}
