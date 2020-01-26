package ch.elexis.core.coding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.coding.internal.JsonValueSet;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.IValueSetContribution;

@Component
public class ChEprValueSetsContribution implements IValueSetContribution {
	
	private String[] names = {
		"EprAuthorRole", "EprDocumentAvailabilityStatus", "EprDocumentClassCode",
		"EprDocumentConfidentialityCode", "EprDocumentFormatCode", "EprDocumentLanguage",
		"EprDocumentMimeType", "EprDocumentPracticeSettingCode", "EprDocumentTypeCode", "EprGender",
		"EprHealthcareFacilityTypeCode",
	};
	
	private List<String> ids;
	
	private Map<String, JsonValueSet> idValueSetMap;
	private Map<String, JsonValueSet> nameValueSetMap;
	
	@Activate
	public void activate(){
		ids = new ArrayList<>();
		idValueSetMap = new HashMap<>();
		nameValueSetMap = new HashMap<>();
		for (String name : names) {
			Optional<JsonValueSet> valueSet = JsonValueSet.load(name);
			if(valueSet.isPresent()) {
				ids.add(valueSet.get().getId());
				idValueSetMap.put(valueSet.get().getId(), valueSet.get());
				nameValueSetMap.put(name, valueSet.get());
			} else {
				LoggerFactory.getLogger(getClass())
					.warn("Could not load valueset with name " + name);
			}
		}
	}
	
	@Override
	public List<String> getValueSetIds(){
		return ids;
	}
	
	@Override
	public List<String> getValueSetNames(){
		return Arrays.asList(names);
	}
	
	@Override
	public List<ICoding> getValueSet(String id){
		if (idValueSetMap.get(id) != null) {
			return idValueSetMap.get(id).getCoding();
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<ICoding> getValueSetByName(String name){
		if (nameValueSetMap.get(name) != null) {
			return nameValueSetMap.get(name).getCoding();
		}
		return Collections.emptyList();
	}
}
