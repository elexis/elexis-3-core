package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Coverage;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ch.elexis.core.fhir.mapper.r4.ICoverageCoverageAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.helper.ICoverageHelper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;

@Component
public class CoverageICoverageTransformer implements IFhirTransformer<Coverage, ICoverage> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private ICoverageService coverageService;

	private ICoverageCoverageAttributeMapper attributeMapper;

	@Activate
	public void activate() {
		attributeMapper = new ICoverageCoverageAttributeMapper(coreModelService, coverageService);
	}

	@Override
	public Optional<Coverage> getFhirObject(ICoverage localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Coverage coverage = new Coverage();
		attributeMapper.elexisToFhir(localObject, coverage, summaryEnum);
		return Optional.of(coverage);
	}

	@Override
	public Optional<ICoverage> getLocalObject(Coverage fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return coreModelService.load(localId.get(), ICoverage.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICoverage> updateLocalObject(Coverage fhirObject, ICoverage localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		coreModelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<ICoverage> createLocalObject(Coverage fhirObject) {
		if (!fhirObject.hasBeneficiary()) {
			throw new PreconditionFailedException("Beneficiary missing");
		}

		Optional<IPatient> patient = coreModelService
				.load(fhirObject.getBeneficiary().getReferenceElement().getIdPart(), IPatient.class);
		if (patient.isEmpty()) {
			throw new PreconditionFailedException("Invalid patient");
		}

		Optional<String> type = new ICoverageHelper().getType(fhirObject);
		if (type.isEmpty()) {
			throw new PreconditionFailedException("BillingSystem missing");
		}

		ICoverage create = new ICoverageBuilder(coreModelService, patient.get(), "online created",
				FallConstants.TYPE_DISEASE, type.get()).buildAndSave();
		attributeMapper.fhirToElexis(fhirObject, create);
		coreModelService.save(create);
		return Optional.of(create);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Coverage.class.equals(fhirClazz) && ICoverage.class.equals(localClazz);
	}

}
