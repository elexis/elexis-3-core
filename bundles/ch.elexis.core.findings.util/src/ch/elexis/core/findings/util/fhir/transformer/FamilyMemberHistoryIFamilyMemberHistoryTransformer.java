package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.FindingsContentHelper;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

@Component(immediate = true)
public class FamilyMemberHistoryIFamilyMemberHistoryTransformer
		implements IFhirTransformer<FamilyMemberHistory, IFamilyMemberHistory> {

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
	public Optional<FamilyMemberHistory> getFhirObject(IFamilyMemberHistory localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((FamilyMemberHistory) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IFamilyMemberHistory> getLocalObject(FamilyMemberHistory fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IFamilyMemberHistory> existing = findingsService.findById(fhirObject.getId(),
					IFamilyMemberHistory.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IFamilyMemberHistory> updateLocalObject(FamilyMemberHistory fhirObject,
			IFamilyMemberHistory localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IFamilyMemberHistory> createLocalObject(FamilyMemberHistory fhirObject) {
		IFamilyMemberHistory IFamilyMemberHistory = findingsService.create(IFamilyMemberHistory.class);
		contentHelper.setResource(fhirObject, IFamilyMemberHistory);
		if (fhirObject.getPatient() != null && fhirObject.getPatient().hasReference()) {
			String id = fhirObject.getPatient().getReferenceElement().getIdPart();
			Optional<IPatient> patient = modelService.load(id, IPatient.class);
			patient.ifPresent(k -> IFamilyMemberHistory.setPatientId(id));
		}
		findingsService.saveFinding(IFamilyMemberHistory);
		return Optional.of(IFamilyMemberHistory);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return FamilyMemberHistory.class.equals(fhirClazz) && IFamilyMemberHistory.class.equals(localClazz);
	}

}
