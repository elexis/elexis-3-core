package ch.elexis.core.fhir.mapper.r4.findings;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.fhir.mapper.r4.util.ModelUtil;
import ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory;
import ch.elexis.core.findings.ICoding;

public class AllergyIntoleranceAccessor extends AbstractFindingsAccessor {

	private EnumMapping categoryMapping = new EnumMapping(
			org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory.class,
			org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory.NULL,
			ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory.class,
			AllergyIntoleranceCategory.UNKNOWN);

	public void setPatientId(DomainResource resource, String patientId) {
		AllergyIntolerance fhAllergyIntolerance = (AllergyIntolerance) resource;
		fhAllergyIntolerance.setPatient(new Reference(new IdDt("Patient", patientId)));
	}

	public Optional<LocalDate> getDateRecorded(DomainResource resource) {
		org.hl7.fhir.r4.model.AllergyIntolerance fhirCondition = (org.hl7.fhir.r4.model.AllergyIntolerance) resource;
		Date date = fhirCondition.getRecordedDate();
		if (date != null) {
			return Optional.of(getLocalDate(date));
		}
		return Optional.empty();
	}

	public AllergyIntoleranceCategory getCategory(DomainResource domainResource) {
		org.hl7.fhir.r4.model.AllergyIntolerance fhirCondition = (org.hl7.fhir.r4.model.AllergyIntolerance) domainResource;
		if (!fhirCondition.getCategory().isEmpty()) {
			List<Enumeration<org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory>> categories = fhirCondition
					.getCategory();
			if (!categories.isEmpty()) {
				for (Enumeration<org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory> categoryEnum : categories) {
					try {
						Enum<?> localValue = categoryMapping.getLocalEnumValueByEnum(categoryEnum.getValue());
						if (localValue != null) {
							return (AllergyIntoleranceCategory) localValue;
						}
					} catch (IllegalArgumentException e) {
						LoggerFactory.getLogger(AllergyIntoleranceAccessor.class).warn(e.getMessage());
					}
				}
			}
		}
		return AllergyIntoleranceCategory.UNKNOWN;
	}

	public void setCategory(DomainResource resource, AllergyIntoleranceCategory category) {
		org.hl7.fhir.r4.model.AllergyIntolerance fhirCondition = (org.hl7.fhir.r4.model.AllergyIntolerance) resource;
		if (category == AllergyIntoleranceCategory.UNKNOWN) {
			if (fhirCondition.hasCategory()) {
				fhirCondition.setCategory(null);
			}
			return;
		}
		org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory fhirCategoryCode = (org.hl7.fhir.r4.model.AllergyIntolerance.AllergyIntoleranceCategory) categoryMapping
				.getFhirEnumValueByEnum(category);
		if (fhirCategoryCode != null) {
			if (fhirCondition.hasCategory()) {
				fhirCondition.setCategory(null);
			}
			fhirCondition.addCategory(fhirCategoryCode);
		}
	}

	public List<ICoding> getCoding(DomainResource resource) {
		org.hl7.fhir.r4.model.AllergyIntolerance fhirCondition = (org.hl7.fhir.r4.model.AllergyIntolerance) resource;
		CodeableConcept codeableConcept = fhirCondition.getCode();
		if (codeableConcept != null) {
			return ModelUtil.getCodingsFromConcept(codeableConcept);
		}
		return Collections.emptyList();
	}

	public void setCoding(DomainResource resource, List<ICoding> coding) {
		org.hl7.fhir.r4.model.AllergyIntolerance fhirCondition = (org.hl7.fhir.r4.model.AllergyIntolerance) resource;
		CodeableConcept codeableConcept = fhirCondition.getCode();
		if (codeableConcept == null) {
			codeableConcept = new CodeableConcept();
		}
		ModelUtil.setCodingsToConcept(codeableConcept, coding);
		fhirCondition.setCode(codeableConcept);
	}
}
