package ch.elexis.core.findings.util.fhir.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.CodeSystemUtil;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;

@Component
public class CodeSystemStringTransformer implements IFhirTransformer<CodeSystem, String> {

	@Reference
	private ICodingService codingService;

	private HashMap<String, CodeSystem> idMap = new HashMap<>();

	@Override
	public Optional<CodeSystem> getFhirObject(String localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		CodeSystem ret = null;
		Optional<String> idString = CodeSystemUtil.getIdForString(localObject);
		Optional<String> systemString = CodeSystemUtil.getSystemForId(idString.get());
		if (idString.isPresent()) {
			ret = idMap.get(idString.get());
			if (ret == null) {
				if (codingService != null && systemString.isPresent()) {
					List<ICoding> codes = codingService.getAvailableCodes(systemString.get());
					if (codes != null && !codes.isEmpty()) {
						CodeSystem system = new CodeSystem();
						system.setId(idString.get());
						system.setUrl(systemString.get());
						List<ConceptDefinitionComponent> concepts = codes.stream()
								.map(iCoding -> codingToConcept(iCoding)).collect(Collectors.toList());
						system.setConcept(concepts);
						idMap.put(idString.get(), system);
						ret = system;
					}
				}
			}
		}
		return Optional.ofNullable(ret);
	}

	private ConceptDefinitionComponent codingToConcept(ICoding iCoding) {
		return new ConceptDefinitionComponent().setCode(iCoding.getCode()).setDisplay(iCoding.getDisplay());
	}

	@Override
	public Optional<String> getLocalObject(CodeSystem fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> updateLocalObject(CodeSystem fhirObject, String localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> createLocalObject(CodeSystem fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return CodeSystem.class.equals(fhirClazz) && String.class.equals(localClazz);
	}

}
