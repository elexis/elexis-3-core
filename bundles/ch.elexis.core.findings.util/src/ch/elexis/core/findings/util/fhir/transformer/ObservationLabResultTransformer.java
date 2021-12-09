package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.codesystems.ObservationCategory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.ILabResultHelper;
import ch.elexis.core.model.ILabResult;

@Component
public class ObservationLabResultTransformer implements IFhirTransformer<Observation, ILabResult> {
	
	private ILabResultHelper labResultHelper;
	
	@Activate
	public void activate(){
		labResultHelper = new ILabResultHelper();
	}
	
	@Override
	public Optional<Observation> getFhirObject(ILabResult localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		Observation observation = new Observation();
		
		observation.setId(new IdDt("Observation", localObject.getId()));
		observation.addIdentifier(getElexisObjectIdentifier(localObject));
		
		CodeableConcept observationCode = new CodeableConcept();
		observationCode.setCoding(Collections.singletonList(new Coding(
			ObservationCategory.LABORATORY.getSystem(), ObservationCategory.LABORATORY.toCode(),
			ObservationCategory.LABORATORY.getDisplay())));
		observation.addCategory(observationCode);
		
		observation.setSubject(labResultHelper.getReference("Patient", localObject.getPatient()));
		
		observation.setEffective(labResultHelper.getEffectiveDateTime(localObject));
		
		observation.setValue(labResultHelper.getResult(localObject));
		
		observation.setReferenceRange(labResultHelper.getReferenceComponents(localObject));
		
		observation.setCode(labResultHelper.getCodeableConcept(localObject));
		
		observation.setNote(labResultHelper.getNote(localObject));
		
		return Optional.of(observation);
	}
	
	@Override
	public Optional<ILabResult> getLocalObject(Observation fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<ILabResult> updateLocalObject(Observation fhirObject, ILabResult localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<ILabResult> createLocalObject(Observation fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Observation.class.equals(fhirClazz) && ILabResult.class.equals(localClazz);
	}
	
}
