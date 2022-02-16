package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r4.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r4.model.ValueSet.ValueSetComposeComponent;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.ICodeElement;

@Component
public class ValueSetListTransformer implements IFhirTransformer<ValueSet, List<ICodeElement>> {
	
	@Override
	public Optional<ValueSet> getFhirObject(List<ICodeElement> localObjects,
		SummaryEnum summaryEnum, Set<Include> includes){
		
		ValueSet valueSet = new ValueSet();
		valueSet.setId(new IdDt("ValueSet", "virtual"));
	
		
		valueSet.setStatus(PublicationStatus.ACTIVE);
		
		ValueSetComposeComponent vscc = new ValueSetComposeComponent();
		valueSet.setCompose(vscc);
		ConceptSetComponent conceptSetComponent = vscc.addInclude();
		localObjects.stream().map(c -> toConceptReferenceComponent(c))
			.forEach(crc -> conceptSetComponent.addConcept(crc));
		
		return Optional.of(valueSet);
	}
	
	@Override
	public Optional<List<ICodeElement>> getLocalObject(ValueSet fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<List<ICodeElement>> updateLocalObject(ValueSet fhirObject,
		List<ICodeElement> localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<List<ICodeElement>> createLocalObject(ValueSet fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return ValueSet.class.equals(fhirClazz) && List.class.equals(localClazz);
	}
	
	public ConceptReferenceComponent toConceptReferenceComponent(ICodeElement codeElement){
		CodeType codeType = new CodeType(codeElement.getCode());
		ConceptReferenceComponent crc = new ConceptReferenceComponent(codeType);
		crc.setDisplay(codeElement.getText());
		return crc;
	}
}
