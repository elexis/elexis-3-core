package ch.elexis.core.findings.util.fhir.accessor;

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

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.util.ModelUtil;

public class AllergyIntoleranceAccessor extends AbstractFindingsAccessor {

	private EnumMapping categoryMapping = new EnumMapping(
			org.hl7.fhir.r4.model.codesystems.AllergyIntoleranceCategory.class,
			org.hl7.fhir.r4.model.codesystems.AllergyIntoleranceCategory.NULL,
			ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory.class,
			ConditionCategory.UNKNOWN);

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
					Enum<?> localValue = categoryMapping.getLocalEnumValueByEnum(categoryEnum.getValue());
					if (localValue != null) {
						return (AllergyIntoleranceCategory) localValue;
					}
				}
			}
		}
		return AllergyIntoleranceCategory.UNKNOWN;
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
