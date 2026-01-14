package ch.elexis.core.fhir.mapper.r4;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Coverage.CoverageStatus;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.ICoverageHelper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.BillingSystem;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IModelService;

/**
 * @see https://hl7.org/fhir/R4/coverage.html
 */
public class ICoverageCoverageAttributeMapper extends IdentifiableDomainResourceAttributeMapper<ICoverage, Coverage> {

	private IModelService coreModelService;
	private ICoverageService coverageService;

	private ICoverageHelper coverageHelper;

	public ICoverageCoverageAttributeMapper(IModelService coreModelService, ICoverageService coverageService) {
		super(Coverage.class);

		this.coreModelService = coreModelService;
		this.coverageService = coverageService;

		coverageHelper = new ICoverageHelper();
	}

	@Override
	public void mapNarrative(ICoverage source, Coverage target) {
		String fallText = coverageHelper.getFallText(source);
		Narrative narrative = new Narrative();
		target.setText(narrative);
		narrative.setStatus(NarrativeStatus.GENERATED);
		narrative.setDivAsString(fallText);

		if (source.isOpen()) {
			clss(narrative.getDiv(), "open");
		} else {
			clss(narrative.getDiv(), "closed");
		}

		if (coverageService.isValid(source)) {
			clss(narrative.getDiv(), "valid");
		} else {
			clss(narrative.getDiv(), "invalid");
		}
	}

	/**
	 * Copied and adapted from org.hl7.fhir.utilities-6.5.27.jar, XHtmlNode
	 * 
	 * @param node
	 * @param name
	 * @return
	 * @deprecated replace with native class on hapi-fhir update
	 */
	private XhtmlNode clss(XhtmlNode node, String name) {
		if (node.hasAttribute("class")) {
			node.setAttribute("class", node.getAttribute("class") + " " + name);
		} else {
			node.setAttribute("class", name);
		}
		return node;
	}

	@Override
	public void fullElexisToFhir(ICoverage source, Coverage target, SummaryEnum summaryEnum) {

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
	public void fullFhirToElexis(Coverage source, ICoverage target) {

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
