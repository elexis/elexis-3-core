package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Coverage.CoverageStatus;
import org.hl7.fhir.r4.model.Period;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.ICoverageHelper;
import ch.elexis.core.model.BillingSystem;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.services.IModelService;

public class ICoverageCoverageAttributeMapper
		implements IdentifiableDomainResourceAttributeMapper<ICoverage, Coverage> {

	private IModelService coreModelService;

	private ICoverageHelper coverageHelper;

	public ICoverageCoverageAttributeMapper(IModelService coreModelService) {
		coverageHelper = new ICoverageHelper();
		this.coreModelService = coreModelService;
	}

	public void elexisToFhir(ICoverage source, Coverage target, SummaryEnum summaryEnum, Set<Include> includes) {
		target.setId(new IdDt(Coverage.class.getSimpleName(), source.getId()));
		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

		// Bezeichnung
		coverageHelper.setNarrative(target, coverageHelper.getFallText(source));

		CodeableConcept type = new CodeableConcept();

		// Abrechnungsmethode
		coverageHelper.getType(source).ifPresent(type::addCoding);

		// Versicherungsgrund
		coverageHelper.getReason(source).ifPresent(type::addCoding);

		// Unfalldatum
		coverageHelper.getAccidentDate(source).ifPresent(type::addCoding);

		target.setType(type);

		// Versicherungsnummer (KVG)
		coverageHelper.getInsuranceNumber(source).ifPresent(target::addIdentifier);

		// Startdatum, Enddatum
		target.setPeriod(coverageHelper.getPeriod(source));

		// Rechnunsempfaenger
		target.setPolicyHolder(coverageHelper.getPolicyHolderReference(source));

		// Kostenträger
		target.setPayor(Collections.singletonList(coverageHelper.getPayor(source)));

		// Patient
		target.setBeneficiary(coverageHelper.getBeneficiaryReference(source));

		// FallNummer (IVG), UnfallNummer (UVG)
		target.setDependent(coverageHelper.getDependent(source));

		// active
		target.setStatus(source.isOpen() ? CoverageStatus.ACTIVE : CoverageStatus.CANCELLED);

	}

	@Override
	public void fhirToElexis(Coverage source, ICoverage target) {

		// FallNummer (IVG), UnfallNummer (UVG)
		String dependent = source.getDependent();
		if (dependent != null) {
			coverageHelper.setDependent(target, source.getDependent());
		}

		// Bezeichnung
//		String divAsString = source.getText().getDivAsString();
//		if (divAsString != null && !coverageHelper.getFallText(target).equalsIgnoreCase(divAsString)) {
//			target.setDescription(divAsString);
//		}

		// Abrechnungsmethode
		Optional<String> type = coverageHelper.getType(source);
		if (type.isPresent()) {
			target.setBillingSystem(new BillingSystem(type.get(), null));
		}

		// Versicherungsgrund
		FhirUtil.getCodeFromCodingList(CodingSystem.ELEXIS_COVERAGE_REASON.getSystem(), source.getType().getCoding())
				.ifPresent(target::setReason);

		// Unfalldatum

		// Versicherungsnummer (KVG)
		coverageHelper.setInsuranceNumber(source, target);

		// Startdatum, Enddatum
		Period period = source.getPeriod();
		if (period != null && period.getStart() != null) {
			coverageHelper.setPeriod(target, source.getPeriod());
		} else {
			target.setDateFrom(LocalDate.now());
			target.setDateTo(null);
		}

		// Rechnungsempfaenger
		coverageHelper.getPolicyHolderByReference(coreModelService, source.getPolicyHolder())
				.ifPresent(guarantor -> target.setGuarantor(guarantor));

		// Kostenträger
		coverageHelper.getPayorByReference(coreModelService, source.getPayorFirstRep())
				.ifPresent(payor -> target.setCostBearer(payor));

		// Patient
		// not changeable, create new Coverage

		// FallNummer (IVG), UnfallNummer (UVG)

		// active
		// not settable => derived
	}

}
