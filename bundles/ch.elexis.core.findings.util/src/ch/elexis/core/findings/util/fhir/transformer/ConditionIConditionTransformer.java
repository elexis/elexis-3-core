package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.findings.ConditionAccessor;
import ch.elexis.core.fhir.mapper.r4.helper.FindingsContentHelper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component
public class ConditionIConditionTransformer implements IFhirTransformer<Condition, ICondition> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IFindingsService findingsService;

	private ConditionAccessor accessor = new ConditionAccessor();

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	@Override
	public Optional<Condition> getFhirObject(ICondition localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			Condition fhirObject = (Condition) resource.get();
			FhirUtil.setVersionedIdPartLastUpdatedMeta(Condition.class, fhirObject, localObject);
			return Optional.of(fhirObject);
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> getLocalObject(Condition fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<ICondition> existing = findingsService
					.findById(FhirUtil.getLocalId(fhirObject.getId()).orElse(StringUtils.EMPTY), ICondition.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICondition> updateLocalObject(Condition fhirObject, ICondition localObject) {
		Optional<String> fhirText = ModelUtil.getNarrativeAsString(fhirObject.getText());
		if (fhirText.isPresent()) {
			localObject.setText(fhirText.get());
		} else {
			localObject.setText(StringUtils.EMPTY);
		}
		localObject.setStatus(accessor.getStatus(fhirObject));
		localObject.setStart(accessor.getStart(fhirObject).orElse(StringUtils.EMPTY));
		localObject.setEnd(accessor.getEnd(fhirObject).orElse(StringUtils.EMPTY));

		findingsService.saveFinding(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<ICondition> createLocalObject(Condition fhirObject) {
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
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Condition.class.equals(fhirClazz) && ICondition.class.equals(localClazz);
	}

}
