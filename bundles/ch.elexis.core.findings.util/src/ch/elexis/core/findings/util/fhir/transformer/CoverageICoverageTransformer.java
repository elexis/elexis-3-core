package ch.elexis.core.findings.util.fhir.transformer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Coverage.CoverageStatus;
import org.hl7.fhir.r4.model.Period;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.findings.util.fhir.transformer.helper.ICoverageHelper;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.services.IModelService;

@Component
public class CoverageICoverageTransformer implements IFhirTransformer<Coverage, ICoverage> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	private ICoverageHelper coverageHelper;

	@Activate
	public void activate() {
		coverageHelper = new ICoverageHelper();
	}

	@Override
	public Optional<Coverage> getFhirObject(ICoverage localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Coverage coverage = new Coverage();

		coverage.setId(new IdDt("Coverage", localObject.getId()));
		coverage.addIdentifier(getElexisObjectIdentifier(localObject));

		// Bezeichnung
		coverageHelper.setText(coverage, coverageHelper.getFallText(localObject));

		CodeableConcept type = new CodeableConcept();

		// Abrechnungsmethode
		coverageHelper.getType(localObject).ifPresent(coding -> {
			type.addCoding(coding);
		});

		// Versicherungsgrund
		coverageHelper.getReason(localObject).ifPresent(coding -> {
			type.addCoding(coding);
		});

		// Unfalldatum
		coverageHelper.getAccidentDate(localObject).ifPresent(coding -> {
			type.addCoding(coding);
		});

		coverage.setType(type);

		// Versicherungsnummer (KVG)
		coverageHelper.getInsuranceNumber(localObject).ifPresent(identifier -> {
			coverage.addIdentifier(identifier);
		});

		// Startdatum, Enddatum
		coverage.setPeriod(coverageHelper.getPeriod(localObject));

		// Rechnunsempfaenger
		coverage.setPolicyHolder(coverageHelper.getPolicyHolderReference(localObject));

		// Kostenträger
		coverage.setPayor(Collections.singletonList(coverageHelper.getPayor(localObject)));

		// Patient
		coverage.setBeneficiary(coverageHelper.getBeneficiaryReference(localObject));

		// FallNummer (IVG), UnfallNummer (UVG)
		coverage.setDependent(coverageHelper.getDependent(localObject));

		// active
		coverage.setStatus(localObject.isOpen() ? CoverageStatus.ACTIVE : CoverageStatus.CANCELLED);

		return Optional.of(coverage);
	}

	@Override
	public Optional<ICoverage> getLocalObject(Coverage fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<ICoverage> existing = modelService.load(fhirObject.getId(), ICoverage.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ICoverage> updateLocalObject(Coverage fhirObject, ICoverage localObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<ICoverage> createLocalObject(Coverage fhirObject) {
		if (fhirObject.hasBeneficiary()) {
			Optional<IPatient> patient = modelService
					.load(fhirObject.getBeneficiary().getReferenceElement().getIdPart(), IPatient.class);
			if (patient.isEmpty()) {
				throw new IFhirTransformerException("WARNING", "Invalid patient", 412);
			}
			Optional<String> type = coverageHelper.getType(fhirObject);
			if (patient.isPresent() && type.isPresent()) {
				ICoverage created = new ICoverageBuilder(modelService, patient.get(), "online created",
						FallConstants.TYPE_DISEASE, type.get()).buildAndSave();
				String dependent = fhirObject.getDependent();
				if (dependent != null) {
					coverageHelper.setDependent(created, dependent);
				}
				Period period = fhirObject.getPeriod();
				if (period != null && period.getStart() != null) {
					coverageHelper.setPeriod(created, fhirObject.getPeriod());
				} else {
					created.setDateFrom(LocalDate.now());
				}
				modelService.save(created);
				AbstractHelper.acquireAndReleaseLock(created);
				return Optional.of(created);
			} else {
				LoggerFactory.getLogger(CoverageICoverageTransformer.class)
						.warn("Could not create fall for patinet [" + patient + "] type [" + type + "]");
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Coverage.class.equals(fhirClazz) && ICoverage.class.equals(localClazz);
	}

}
