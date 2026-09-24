package ch.elexis.core.fhir.mapper.r4;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.codes.MedicamentCoding;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.services.IModelService;

public class IVaccinationImmunizationAttributeMapper
		extends IdentifiableDomainResourceAttributeMapper<IVaccination, Immunization> {

	private IModelService modelService;

	public IVaccinationImmunizationAttributeMapper(IModelService modelService) {
		super(Immunization.class);

		this.modelService = modelService;
	}

	@Override
	public void fullElexisToFhir(IVaccination source, Immunization target, SummaryEnum summaryEnum) {

		target.setStatus(ImmunizationStatus.COMPLETED);

		target.setPatient(FhirUtil.getReference(source.getPatient()));

		if (source.getPerformer() != null && source.getPerformer().isMandator()) {
			IMandator mandator = modelService.load(source.getPerformer().getId(), IMandator.class).get();
			target.addPerformer().setActor(FhirUtil.getReference(mandator));
		}

		StringBuilder textBuilder = new StringBuilder();

		CodeableConcept vaccine = new CodeableConcept();
		String gtin = source.getArticleGtin();
		String atc = source.getArticleAtc();
		String articelLabel = source.getArticleName();
		if (gtin != null) {
			Coding coding = vaccine.addCoding();
			coding.setSystem(MedicamentCoding.GTIN.getOid());
			coding.setCode(gtin);
			coding.setDisplay(articelLabel);
		}
		if (atc != null) {
			Coding coding = vaccine.addCoding();
			coding.setSystem(MedicamentCoding.ATC.getOid());
			coding.setCode(atc);
		}
		vaccine.setText(articelLabel);
		textBuilder.append(articelLabel);
		vaccine.setText(textBuilder.toString());
		target.setVaccineCode(vaccine);

		target.setLotNumber(source.getLotNumber());

		if (StringUtils.isNotBlank(source.getIngredientsAtc())) {
			CodeableConcept reasonCode = target.addReasonCode();
			String[] parts = source.getIngredientsAtc().split(",");
			for (String ingredientAtc : parts) {
				Coding coding = reasonCode.addCoding();
				coding.setSystem(MedicamentCoding.ATC.getOid());
				coding.setCode(ingredientAtc);
			}
		}

		LocalDate dateOfAdministration = source.getDateOfAdministration();
		if (dateOfAdministration != null) {
			Date date = Date.from(dateOfAdministration.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			target.setOccurrence(new DateTimeType(date));
		}
	}

	@Override
	public void fullFhirToElexis(Immunization source, IVaccination target) {
		Optional<IMandator> mandator = Optional.empty();
		if (source.hasPerformer() && source.getPerformerFirstRep().hasActor()) {
			mandator = modelService.load(FhirUtil.getId(source.getPerformerFirstRep().getActor()).orElse(null),
					IMandator.class);
		}

		if (mandator.isPresent()) {
			target.setPerformer(mandator.get());
		}

		if (source.hasOccurrenceDateTimeType()) {
			LocalDate occurance = LocalDateTime
					.ofInstant(source.getOccurrenceDateTimeType().getValue().toInstant(), ZoneId.systemDefault())
					.toLocalDate();
			target.setDateOfAdministration(occurance);
		}

		if (source.hasLotNumber()) {
			target.setLotNumber(source.getLotNumber());
		}

		if (source.hasReasonCode()) {
			List<String> atcCodes = FhirUtil.getCodesFromConceptList(MedicamentCoding.ATC.getOid(),
					source.getReasonCode());
			if (!atcCodes.isEmpty()) {
				target.setIngredientsAtc(atcCodes.stream().collect(Collectors.joining(",")));
			}
		}
	}
}
