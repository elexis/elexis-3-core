package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Patient;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IPatientPatientAttributeMapper;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;

@Component(property = IFhirTransformer.TRANSFORMERID + "=Patient.IPatient")
public class PatientIPatientTransformer implements IFhirTransformer<Patient, IPatient> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IXidService xidService;

	private IPatientPatientAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		attributeMapper = new IPatientPatientAttributeMapper(modelService, xidService);
	}

	@Override
	public Optional<Patient> getFhirObject(IPatient localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Patient patient = new Patient();
		attributeMapper.elexisToFhir(localObject, patient, summaryEnum, includes);
		return Optional.of(patient);
	}

	@Override
	public Optional<IPatient> getLocalObject(Patient fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return modelService.load(localId.get(), IPatient.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<? extends Identifiable> getLocalObjectForReference(String fhirReference) {
		if (fhirReference.startsWith(Patient.class.getSimpleName())) {
			Optional<String> localId = FhirUtil.getLocalId(fhirReference);
			if (localId.isPresent()) {
				return modelService.load(localId.get(), IPatient.class);
			}
		}
		return IFhirTransformer.super.getLocalObjectForReference(fhirReference);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Patient.class.equals(fhirClazz) && IPatient.class.equals(localClazz);
	}

	@Override
	public Optional<IPatient> updateLocalObject(Patient fhirObject, IPatient localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		modelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IPatient> createLocalObject(Patient fhirObject) {
		IPatient create = modelService.create(IPatient.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		modelService.save(create);
		return Optional.of(create);
	}
}
