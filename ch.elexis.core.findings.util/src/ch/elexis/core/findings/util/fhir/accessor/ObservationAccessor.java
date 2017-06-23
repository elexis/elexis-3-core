package ch.elexis.core.findings.util.fhir.accessor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Type;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.ModelUtil;

public class ObservationAccessor extends AbstractFindingsAccessor {

	private EnumMapping categoryMapping = new EnumMapping(
			org.hl7.fhir.dstu3.model.codesystems.ObservationCategory.class, null,
			ch.elexis.core.findings.IObservation.ObservationCategory.class, null);

	public Optional<LocalDateTime> getEffectiveTime(DomainResource resource) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		Type effective = fhirObservation.getEffective();
		if (effective instanceof DateTimeType) {
			return Optional.of(getLocalDateTime(((DateTimeType) effective).getValue()));
		} else if (effective instanceof Period) {
			Date start = ((Period) effective).getStart();
			if (start != null) {
				return Optional.of(getLocalDateTime(start));
			}
			Date end = ((Period) effective).getEnd();
			if (end != null) {
				return Optional.of(getLocalDateTime(end));
			}
		}
		return Optional.empty();
	}

	public void setEffectiveTime(DomainResource resource, LocalDateTime time) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		fhirObservation.setEffective(new DateTimeType(getDate(time)));
	}

	public ObservationCategory getCategory(DomainResource resource) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		if (!fhirObservation.getCategory().isEmpty()) {
			for (CodeableConcept categoryConcept : fhirObservation.getCategory()) {
				List<Coding> coding = categoryConcept.getCoding();
				for (Coding code : coding) {
					if (code.getSystem().equals("http://hl7.org/fhir/observation-category")) {
						ch.elexis.core.findings.IObservation.ObservationCategory mappedCategory = (ch.elexis.core.findings.IObservation.ObservationCategory) categoryMapping
								.getLocalEnumValueByCode(code.getCode().toUpperCase());
						if (mappedCategory != null) {
							return mappedCategory;
						}
					} else if (code.getSystem().equals(IdentifierSystem.ELEXIS_SOAP.getSystem())) {
						ch.elexis.core.findings.IObservation.ObservationCategory mappedCategory = (ch.elexis.core.findings.IObservation.ObservationCategory) categoryMapping
								.getLocalEnumValueByCode("SOAP_" + code.getCode().toUpperCase());
						if (mappedCategory != null) {
							return mappedCategory;
						}
					}
				}
			}
		}
		return ObservationCategory.UNKNOWN;
	}

	public void setCategory(DomainResource resource, ObservationCategory category) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		CodeableConcept categoryCode = new CodeableConcept();
		org.hl7.fhir.dstu3.model.codesystems.ObservationCategory fhirCategoryCode = (org.hl7.fhir.dstu3.model.codesystems.ObservationCategory) categoryMapping
				.getFhirEnumValueByEnum(category);
		if (fhirCategoryCode != null) {
			// lookup matching fhir category
			categoryCode.setCoding(Collections.singletonList(new Coding(fhirCategoryCode.getSystem(),
					fhirCategoryCode.toCode(), fhirCategoryCode.getDisplay())));
			fhirObservation.setCategory(Collections.singletonList(categoryCode));
		} else if (category.name().startsWith("SOAP_")) {
			// elexis soap categories
			categoryCode.setCoding(Collections.singletonList(
					new Coding(IdentifierSystem.ELEXIS_SOAP.getSystem(), category.getCode(), category.getLocalized())));
		} else {
			throw new IllegalStateException("Unknown observation category " + category);
		}
	}

	public List<ICoding> getCoding(DomainResource resource) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		CodeableConcept codeableConcept = fhirObservation.getCode();
		if (codeableConcept != null) {
			return ModelUtil.getCodingsFromConcept(codeableConcept);
		}
		return Collections.emptyList();
	}

	public void setCoding(DomainResource resource, List<ICoding> coding) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		CodeableConcept codeableConcept = fhirObservation.getCode();
		if (codeableConcept == null) {
			codeableConcept = new CodeableConcept();
		}
		ModelUtil.setCodingsToConcept(codeableConcept, coding);
		fhirObservation.setCode(codeableConcept);
	}

	public void setPatientId(DomainResource resource, String patientId) {
		org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) resource;
		fhirObservation.setSubject(new Reference(new IdDt("Patient", patientId)));
	}
}
