package ch.elexis.core.findings.util.fhir.accessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Type;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.BackboneComponent;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.ModelUtil;

public class ObservationAccessor extends AbstractFindingsAccessor {
	
	private EnumMapping categoryMapping =
		new EnumMapping(org.hl7.fhir.dstu3.model.codesystems.ObservationCategory.class, null,
			ch.elexis.core.findings.IObservation.ObservationCategory.class, null);
	
	public Optional<LocalDateTime> getEffectiveTime(DomainResource resource){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
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
	
	public void setEffectiveTime(DomainResource resource, LocalDateTime time){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		fhirObservation.setEffective(new DateTimeType(getDate(time)));
	}
	
	public ObservationCategory getCategory(DomainResource resource){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		if (!fhirObservation.getCategory().isEmpty()) {
			for (CodeableConcept categoryConcept : fhirObservation.getCategory()) {
				List<Coding> coding = categoryConcept.getCoding();
				for (Coding code : coding) {
					if (code.getSystem().equals("http://hl7.org/fhir/observation-category")) {
						ch.elexis.core.findings.IObservation.ObservationCategory mappedCategory =
							(ch.elexis.core.findings.IObservation.ObservationCategory) categoryMapping
								.getLocalEnumValueByCode(code.getCode().toUpperCase());
						if (mappedCategory != null) {
							return mappedCategory;
						}
					} else if (code.getSystem().equals(IdentifierSystem.ELEXIS_SOAP.getSystem())) {
						ch.elexis.core.findings.IObservation.ObservationCategory mappedCategory =
							(ch.elexis.core.findings.IObservation.ObservationCategory) categoryMapping
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
	
	public void setCategory(DomainResource resource, ObservationCategory category){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		CodeableConcept categoryCode = new CodeableConcept();
		if (category.name().startsWith("SOAP_")) {
			// elexis soap categories
			categoryCode.setCoding(
				Collections.singletonList(new Coding(IdentifierSystem.ELEXIS_SOAP.getSystem(),
					category.getCode(), category.getLocalized())));
		} else {
			org.hl7.fhir.dstu3.model.codesystems.ObservationCategory fhirCategoryCode =
				(org.hl7.fhir.dstu3.model.codesystems.ObservationCategory) categoryMapping
					.getFhirEnumValueByEnum(category);
			if (fhirCategoryCode != null) {
				// lookup matching fhir category
				categoryCode
					.setCoding(Collections.singletonList(new Coding(fhirCategoryCode.getSystem(),
						fhirCategoryCode.toCode(), fhirCategoryCode.getDisplay())));
			} else {
				throw new IllegalStateException("Unknown observation category " + category);
			}
		}
		if (!categoryCode.getCoding().isEmpty()) {
			fhirObservation.setCategory(Collections.singletonList(categoryCode));
		}
	}
	
	public List<ICoding> getCoding(DomainResource resource){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		CodeableConcept codeableConcept = fhirObservation.getCode();
		if (codeableConcept != null) {
			return ModelUtil.getCodingsFromConcept(codeableConcept);
		}
		return Collections.emptyList();
	}
	
	public void setCoding(DomainResource resource, List<ICoding> coding){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		CodeableConcept codeableConcept = fhirObservation.getCode();
		if (codeableConcept == null) {
			codeableConcept = new CodeableConcept();
		}
		ModelUtil.setCodingsToConcept(codeableConcept, coding);
		fhirObservation.setCode(codeableConcept);
	}
	
	public void setPatientId(DomainResource resource, String patientId){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		fhirObservation.setSubject(new Reference(new IdDt("Patient", patientId)));
	}
	
	public void addComponent(DomainResource resource, BackboneComponent iComponent){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		ObservationComponentComponent observationComponentComponent =
			new ObservationComponentComponent();
		observationComponentComponent.setId(UUID.randomUUID().toString());
		CodeableConcept codeableConcept = observationComponentComponent.getCode();
		if (codeableConcept == null) {
			codeableConcept = new CodeableConcept();
		}
		ModelUtil.setCodingsToConcept(codeableConcept, iComponent.getCoding());
		observationComponentComponent.setCode(codeableConcept);
		
		Quantity quantity = new Quantity();
		iComponent.getNumericValue()
			.ifPresent(item -> quantity.setValue(iComponent.getNumericValue().get()));
		iComponent.getNumericValueUnit()
			.ifPresent(item -> quantity.setUnit(iComponent.getNumericValueUnit().get()));
		observationComponentComponent.setValue(quantity);
		
		fhirObservation.addComponent(observationComponentComponent);
	}
	
	public List<BackboneComponent> getComponents(DomainResource resource){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		List<BackboneComponent> components = new ArrayList<>();
		
		for (ObservationComponentComponent o : fhirObservation.getComponent()) {
			BackboneComponent component = new BackboneComponent(o.getId());
			CodeableConcept codeableConcept = o.getCode();
			if (codeableConcept != null) {
				component.setCoding(ModelUtil.getCodingsFromConcept(codeableConcept));
				if (o.hasValueQuantity()) {
					Quantity quantity = (Quantity) o.getValue();
					component.setNumericValue(Optional.of(quantity.getValue()));
					component.setNumericValueUnit(Optional.of(quantity.getUnit()));
				}
			}
			components.add(component);
		}
		return components;
	}
	
	public void updateComponent(DomainResource resource, BackboneComponent component){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		
		for (ObservationComponentComponent o : fhirObservation.getComponent()) {
			if (component.getId().equals(o.getId())) {
				if (component.getNumericValue().isPresent() && o.hasValueQuantity()) {
					Quantity quantity = (Quantity) o.getValue();
					quantity.setValue(component.getNumericValue().get());
				}
			}
		}
	}
	
	public void setNumericValue(DomainResource resource, BigDecimal value, String unit){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		Quantity q = new Quantity();
		q.setUnit(unit);
		q.setValue(value);
		fhirObservation.setValue(q);
	}
	
	public Optional<BigDecimal> getNumericValue(DomainResource resource){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		if (fhirObservation.hasValueQuantity()) {
			Quantity quantity = (Quantity) fhirObservation.getValue();
			if (quantity.getValue() != null) {
				return Optional.of(quantity.getValue());
			}
		}
		return Optional.empty();
	}
	
	public Optional<String> getNumericValueUnit(DomainResource resource){
		org.hl7.fhir.dstu3.model.Observation fhirObservation =
			(org.hl7.fhir.dstu3.model.Observation) resource;
		if (fhirObservation.hasValueQuantity()) {
			Quantity quantity = (Quantity) fhirObservation.getValue();
			if (quantity.getUnit() != null) {
				return Optional.of(quantity.getUnit());
			}
		}
		return Optional.empty();
	}
}
