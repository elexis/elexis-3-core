package ch.elexis.core.data.extension;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.RegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.interfaces.AbstractReferenceDataImporter;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;

/**
 * This class offers a centralized contact point for reference data imports. It forwards the update
 * to the appropriate importer. This update forwarding effects all importers included as
 * ExtensionPoint {@code ch.elexis.core.data.referenceDataImporter} and extending the
 * {@link AbstractReferenceDataImporter} class
 * 
 */
public class ReferenceDataImporterExtensionPoint {
	private static Logger log = LoggerFactory.getLogger(ReferenceDataImporterExtensionPoint.class
		.getName());
	
	private static final String CLASS_PROPERTY = "class";
	private static final String ATTRIBUTE_REFDATA_ID = "referenceDataId";
	private static HashMap<String, AbstractReferenceDataImporter> importers;
	
	/**
	 * 
	 * @param refDataId
	 *            the id of the requested reference data type
	 * @return the {@link AbstractReferenceDataImporter} if found, else null
	 */
	public static @Nullable
	AbstractReferenceDataImporter getReferenceDataImporterByReferenceDataId(@NonNull
	String refDataId){
		if (importers == null) {
			initialize();
		}
		return importers.get(refDataId);
	}
	
	/**
	 * loads all ExtensionPoints of type {@code ExtensionPointConstantsData.REFERENCE_DATA_IMPORTER}
	 * and adds them to a list of available importers
	 */
	private static void initialize(){
		try {
			importers = new HashMap<String, AbstractReferenceDataImporter>();
			
			// load reference-data-extensionpoint
			IExtensionPoint refDataExtensionPoint =
				RegistryFactory.getRegistry().getExtensionPoint(
					ExtensionPointConstantsData.REFERENCE_DATA_IMPORTER);
			IConfigurationElement[] extensionPoints =
				refDataExtensionPoint.getConfigurationElements();
			
			// add all found extensionPoints to the importer list
			for (IConfigurationElement ePoint : extensionPoints) {
				Object o = ePoint.createExecutableExtension(CLASS_PROPERTY);
				String refDataId = ePoint.getAttribute(ATTRIBUTE_REFDATA_ID);
				
				if (o instanceof AbstractReferenceDataImporter) {
					AbstractReferenceDataImporter importer = (AbstractReferenceDataImporter) o;
					importers.put(refDataId, importer);
					
					log.debug("Added ReferenceDataImporter for... " + refDataId);
				}
			}
		} catch (CoreException e) {
			log.error("Exception occured trying to load ReferenceDataImporter ExtensionPoints", e);
		}
	}
}