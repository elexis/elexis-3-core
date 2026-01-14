package ch.elexis.core.fhir.mapper.r4;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.FhirConstants;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.time.TimeUtil;

public class ISickCertificateConditionAttributeMapper
		extends IdentifiableDomainResourceAttributeMapper<ISickCertificate, Condition> {

	private IModelService modelService;

	public ISickCertificateConditionAttributeMapper(IModelService modelService) {
		super(Condition.class);

		this.modelService = modelService;
	}

	@Override
	public void fullElexisToFhir(ISickCertificate source, Condition target, SummaryEnum summaryEnum) {

		target.getCode().addCoding(new Coding(FhirConstants.DE_EAU_SYSTEM, FhirConstants.DE_EAU_SYSTEM_CODE,
				FhirConstants.DE_EAU_SYSTEM_CODE));
		target.setSubject(new Reference("Patient/" + source.getPatient().getId()));

		LocalDate date = source.getDate();
		target.setRecordedDate(TimeUtil.toDate(date));

		LocalDate start = source.getStart();
		target.setOnset(new DateTimeType(TimeUtil.toDate(start)));

		if (source.getEnd() != null) {
			LocalDate end = source.getEnd();
			target.setAbatement(new DateTimeType(TimeUtil.toDate(end)));
		}

		if (StringUtils.isNotBlank(source.getNote())) {
			Annotation annotation = new Annotation();
			annotation.setText(source.getNote());
			target.getNote().add(annotation);
		}

		Coding reasonCoding = new Coding(CodingSystem.ELEXIS_AUF_REASON.getSystem(), source.getReason(),
				source.getReason());
		target.getCode().addCoding(reasonCoding);

		Coding aufDegreeCoding = new Coding(CodingSystem.ELEXIS_AUF_DEGREE.getSystem(),
				Integer.toString(source.getPercent()), Integer.toString(source.getPercent()) + "%");
		target.getStageFirstRep().setType(new CodeableConcept(aufDegreeCoding));

	}

	@Override
	public void fullFhirToElexis(Condition source, ISickCertificate target) {
		Optional<IPatient> patient = modelService.load(source.getSubject().getReferenceElement().getIdPart(),
				IPatient.class);
		if (patient.isEmpty()) {
			throw new AttributeMapperException("WARNING", "Invalid patient", 412);
		}
		target.setPatient(patient.get());

		if (source.hasRecordedDate()) {
			target.setDate(TimeUtil.toLocalDate(source.getRecordedDate()));
		}
		if (source.hasOnsetDateTimeType()) {
			target.setStart(TimeUtil.toLocalDate(source.getOnsetDateTimeType().getValue()));
		}
		if (source.hasAbatementDateTimeType()) {
			target.setEnd(TimeUtil.toLocalDate(source.getAbatementDateTimeType().getValue()));
		}

		if (source.hasCode()) {
			List<Coding> codings = source.getCode().getCoding();
			List<Coding> reasonCodings = codings.stream()
					.filter(c -> !FhirConstants.DE_EAU_SYSTEM.equals(c.getSystem())).collect(Collectors.toList());
			if (!reasonCodings.isEmpty()) {
				target.setReason(reasonCodings.get(0).getDisplay());
			}
		}

		if (source.hasStage() && source.getStageFirstRep().hasType()) {
			CodeableConcept stageType = source.getStageFirstRep().getType();
			for (Coding coding : stageType.getCoding()) {
				if (coding.hasSystem()
						&& (coding.getSystem().endsWith("degree") || coding.getSystem().endsWith("percent"))) {
					String code = coding.getCode().replaceAll("%", "").trim();
					if (StringUtils.isNotBlank(code) && StringUtils.isNumeric(code)) {
						target.setPercent(Integer.valueOf(code));
						break;
					}
				}
			}
		}

		if (source.hasNote()) {
			target.setNote(source.getNote().stream().map(n -> n.getText()).collect(Collectors.joining("\n\n")));
		}
	}

}
