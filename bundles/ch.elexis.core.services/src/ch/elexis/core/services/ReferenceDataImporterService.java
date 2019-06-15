package ch.elexis.core.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.IReferenceDataImporter;

@Component
public class ReferenceDataImporterService implements IReferenceDataImporterService {
	
	private HashMap<String, IReferenceDataImporter> importersMap = new HashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setReferenceDataImporter(IReferenceDataImporter referenceDataImporter,
		Map<String, Object> properties){
		IReferenceDataImporter previous =
			importersMap.put((String) properties.get(IReferenceDataImporter.REFERENCEDATAID),
				referenceDataImporter);
		if (previous != null) {
			LoggerFactory.getLogger(getClass())
				.warn("Possible IReferenceDataImporter collision previous [" + previous
					+ "] new [" + referenceDataImporter + "]");
		}
	}
	
	public void unsetReferenceDataImporter(IReferenceDataImporter referenceDataImporter,
		Map<String, Object> properties){
		importersMap.remove((String) properties.get(IReferenceDataImporter.REFERENCEDATAID));
		LoggerFactory.getLogger(getClass())
			.info("Removed IReferenceDataImporter [" + referenceDataImporter + "]");
	}
	
	@Override
	public Optional<IReferenceDataImporter> getImporter(String referenceDataId){
		return Optional.ofNullable(importersMap.get(referenceDataId));
	}
}
