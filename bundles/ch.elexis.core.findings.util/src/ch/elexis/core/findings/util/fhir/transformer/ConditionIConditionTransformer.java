package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FindingsContentHelper;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component
public class ConditionIConditionTransformer implements IFhirTransformer<Condition, ICondition> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFindingsService findingsService;
	
	private FindingsContentHelper contentHelper = new FindingsContentHelper();
	
	@Override
	public Optional<Condition> getFhirObject(ICondition localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Condition) resource.get());
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<ICondition> getLocalObject(Condition fhirObject){
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<ICondition> existing =
				findingsService.findById(fhirObject.getId(), ICondition.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<ICondition> updateLocalObject(Condition fhirObject, ICondition localObject){
		return Optional.empty();
	}
	
	@Override
	public Optional<ICondition> createLocalObject(Condition fhirObject){
		ICondition iCondition = findingsService.create(ICondition.class);
		contentHelper.setResource(fhirObject, iCondition);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<IPatient> patient = modelService.load(id, IPatient.class);
			patient.ifPresent(k -> iCondition.setPatientId(id));
		}
		findingsService.saveFinding(iCondition);
		return Optional.of(iCondition);
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Condition.class.equals(fhirClazz) && ICondition.class.equals(localClazz);
	}
	
}
