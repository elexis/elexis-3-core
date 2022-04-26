package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FindingsContentHelper;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component
public class AllergyIntoleranceIAllergyIntoleranceTransformer
		implements IFhirTransformer<AllergyIntolerance, IAllergyIntolerance> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IFindingsService findingsService;

	private FindingsContentHelper contentHelper;

	@Activate
	public void activate() {
		contentHelper = new FindingsContentHelper();
	}

	@Override
	public Optional<AllergyIntolerance> getFhirObject(IAllergyIntolerance localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((AllergyIntolerance) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAllergyIntolerance> getLocalObject(AllergyIntolerance fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IAllergyIntolerance> existing = findingsService.findById(fhirObject.getId(),
					IAllergyIntolerance.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAllergyIntolerance> updateLocalObject(AllergyIntolerance fhirObject,
			IAllergyIntolerance localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IAllergyIntolerance> createLocalObject(AllergyIntolerance fhirObject) {
		IAllergyIntolerance IallergyIntolerance = findingsService.create(IAllergyIntolerance.class);
		contentHelper.setResource(fhirObject, IallergyIntolerance);
		if (fhirObject.getPatient() != null && fhirObject.getPatient().hasReference()) {
			String id = fhirObject.getPatient().getReferenceElement().getIdPart();
			Optional<IPatient> patient = modelService.load(id, IPatient.class);
			patient.ifPresent(k -> IallergyIntolerance.setPatientId(id));
		}
		findingsService.saveFinding(IallergyIntolerance);
		return Optional.of(IallergyIntolerance);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return AllergyIntolerance.class.equals(fhirClazz) && IAllergyIntolerance.class.equals(localClazz);
	}

}
