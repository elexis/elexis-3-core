package ch.elexis.core.fhir.mapper.r4.findings;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.fhir.mapper.r4.util.ModelUtil;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;

public class ConditionAccessor extends AbstractFindingsAccessor {

	private EnumMapping categoryMapping = new EnumMapping(org.hl7.fhir.r4.model.codesystems.ConditionCategory.class,
			org.hl7.fhir.r4.model.codesystems.ConditionCategory.PROBLEMLISTITEM,
			ch.elexis.core.findings.ICondition.ConditionCategory.class, ConditionCategory.PROBLEMLISTITEM);

	public Optional<LocalDate> getDateRecorded(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		Date date = fhirCondition.getRecordedDate();
		if (date != null) {
			return Optional.of(getLocalDate(date));
		}
		return Optional.empty();
	}

	public void setDateRecorded(DomainResource resource, LocalDate date) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		fhirCondition.setRecordedDate(getDate(date));
	}

	public ConditionCategory getCategory(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		if (!fhirCondition.getCategory().isEmpty()) {
			List<Coding> coding = fhirCondition.getCategory().get(0).getCoding();
			if (!coding.isEmpty()) {
				for (Coding categoryCoding : coding) {
					if ("http://hl7.org/fhir/condition-category".equals(categoryCoding.getSystem())
							|| "http://terminology.hl7.org/CodeSystem/condition-category"
									.equals(categoryCoding.getSystem())
							|| "http://hl7.org/fhir/ValueSet/condition-category".equals(categoryCoding.getSystem())) {
						return (ConditionCategory) categoryMapping
								.getLocalEnumValueByCode(categoryCoding.getCode().toUpperCase());
					}
				}
			}
		}
		return ConditionCategory.UNKNOWN;
	}

	public void setCategory(DomainResource resource, ConditionCategory category) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		CodeableConcept categoryCode = new CodeableConcept();
		org.hl7.fhir.r4.model.codesystems.ConditionCategory fhirCategoryCode = (org.hl7.fhir.r4.model.codesystems.ConditionCategory) categoryMapping
				.getFhirEnumValueByEnum(category);
		if (fhirCategoryCode != null) {
			categoryCode.setCoding(Collections.singletonList(new Coding(fhirCategoryCode.getSystem(),
					fhirCategoryCode.toCode(), fhirCategoryCode.getDisplay())));
			fhirCondition.setCategory(Collections.singletonList(categoryCode));
		}
	}

	public ConditionStatus getStatus(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		CodeableConcept fhirStatus = fhirCondition.getClinicalStatus();
		return toConditionStatus(fhirStatus);
	}

	private ConditionStatus toConditionStatus(CodeableConcept fhirStatus) {
		if (fhirStatus != null) {
			String code = fhirStatus.getCodingFirstRep().getCode();
			if (code != null) {
				try {
					return ConditionStatus.valueOf(code.toUpperCase());
				} catch (IllegalArgumentException iae) {
					LoggerFactory.getLogger(getClass()).warn(code, iae);
				}
			}
		}
		return ConditionStatus.UNKNOWN;
	}

	public void setStatus(DomainResource resource, ConditionStatus status) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		CodeableConcept fhirCategoryCode = toCodeableConcept(status);
		if (fhirCategoryCode != null) {
			fhirCondition.setClinicalStatus(fhirCategoryCode);
		}
	}

	private CodeableConcept toCodeableConcept(ConditionStatus status) {
		Coding coding = new Coding("http://terminology.hl7.org/CodeSystem/condition-clinical",
				status.name().toLowerCase(), null);
		return new CodeableConcept(coding);
	}

	public void setStart(DomainResource resource, String start) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		fhirCondition.setOnset(new StringType(start));
	}

	public Optional<String> getStart(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		try {
			if (fhirCondition.hasOnsetDateTimeType()) {
				DateTimeType dateTime = fhirCondition.getOnsetDateTimeType();
				if (dateTime != null) {
					Date date = dateTime.getValue();
					SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
					return Optional.of(format.format(date));
				}
			} else if (fhirCondition.hasOnsetStringType()) {
				return Optional.of(fhirCondition.getOnsetStringType().getValue());
			}
		} catch (FHIRException e) {
			LoggerFactory.getLogger(ConditionAccessor.class).error("Could not access start time.", e);
		}
		return Optional.empty();
	}

	public void setEnd(DomainResource resource, String end) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		fhirCondition.setAbatement(new StringType(end));
	}

	public Optional<String> getEnd(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		try {
			if (fhirCondition.hasAbatementDateTimeType()) {
				DateTimeType dateTime = fhirCondition.getAbatementDateTimeType();
				if (dateTime != null) {
					Date date = dateTime.getValue();
					SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
					return Optional.of(format.format(date));
				}
			} else if (fhirCondition.hasAbatementStringType()) {
				return Optional.of(fhirCondition.getAbatementStringType().getValue());
			}
		} catch (FHIRException e) {
			LoggerFactory.getLogger(ConditionAccessor.class).error("Could not access end.", e);
		}
		return Optional.empty();
	}

	public void addNote(DomainResource resource, String text) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		Annotation annotation = new Annotation();
		annotation.setText(text);
		fhirCondition.addNote(annotation);
	}

	public void removeNote(DomainResource resource, String text) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		List<Annotation> notes = new ArrayList<>(fhirCondition.getNote());
		notes = notes.stream().filter(annotation -> !text.equals(annotation.getText())).collect(Collectors.toList());
		fhirCondition.setNote(notes);
	}

	public List<String> getNotes(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		List<Annotation> notes = fhirCondition.getNote();
		return notes.stream().map(annotation -> annotation.getText()).collect(Collectors.toList());
	}

	public List<ICoding> getCoding(DomainResource resource) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		CodeableConcept codeableConcept = fhirCondition.getCode();
		if (codeableConcept != null) {
			return ModelUtil.getCodingsFromConcept(codeableConcept);
		}
		return Collections.emptyList();
	}

	public void setCoding(DomainResource resource, List<ICoding> coding) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		CodeableConcept codeableConcept = fhirCondition.getCode();
		if (codeableConcept == null) {
			codeableConcept = new CodeableConcept();
		}
		ModelUtil.setCodingsToConcept(codeableConcept, coding);
		fhirCondition.setCode(codeableConcept);
	}

	public void setPatientId(DomainResource resource, String patientId) {
		org.hl7.fhir.r4.model.Condition fhirCondition = (org.hl7.fhir.r4.model.Condition) resource;
		fhirCondition.setSubject(new Reference(new IdDt("Patient", patientId)));
	}
}
