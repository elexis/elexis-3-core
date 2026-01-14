package ch.elexis.core.fhir.mapper.r4.findings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.LoggerFactory;

import ch.elexis.core.fhir.mapper.r4.util.ModelUtil;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;

public class DocumentReferenceAccessor extends AbstractFindingsAccessor {

	public List<ICoding> getPracticeSetting(DomainResource resource) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		DocumentReferenceContextComponent fhirContext = fhirResource.getContext();
		if (fhirContext != null) {
			CodeableConcept codeableConcept = fhirContext.getPracticeSetting();
			if (codeableConcept != null) {
				return ModelUtil.getCodingsFromConcept(codeableConcept);
			}
		}
		return Collections.emptyList();
	}

	public void setPracticeSetting(DomainResource resource, ICoding coding) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		DocumentReferenceContextComponent fhirContext = fhirResource.getContext();
		if (fhirContext != null) {
			CodeableConcept codeableConcept = new CodeableConcept();
			ModelUtil.setCodingToConcept(codeableConcept, coding);
			fhirContext.setPracticeSetting(codeableConcept);
		}
	}

	public List<ICoding> getFacilityType(DomainResource resource) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		DocumentReferenceContextComponent fhirContext = fhirResource.getContext();
		if (fhirContext != null) {
			CodeableConcept codeableConcept = fhirContext.getFacilityType();
			if (codeableConcept != null) {
				return ModelUtil.getCodingsFromConcept(codeableConcept);
			}
		}
		return Collections.emptyList();
	}

	public void setFacilityType(DomainResource resource, ICoding coding) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		DocumentReferenceContextComponent fhirContext = fhirResource.getContext();
		if (fhirContext != null) {
			CodeableConcept codeableConcept = new CodeableConcept();
			ModelUtil.setCodingToConcept(codeableConcept, coding);
			fhirContext.setFacilityType(codeableConcept);
		}
	}

	public List<ICoding> getDocumentClass(DomainResource resource) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		Optional<CodeableConcept> classConcept = ModelUtil.getCodeableConceptBySystem(fhirResource.getCategory(),
				"2.16.840.1.113883.6.96");
		if (classConcept.isPresent()) {
			return ModelUtil.getCodingsFromConcept(classConcept.get());
		}
		return Collections.emptyList();
	}

	public void setDocumentClass(DomainResource resource, ICoding iCoding) {
		if (!"2.16.840.1.113883.6.96".equals(iCoding.getSystem())) {
			LoggerFactory.getLogger(getClass())
					.error("Invalid code system [" + iCoding.getSystem() + "] for document class code.");
			return;
		}
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		Optional<CodeableConcept> classConcept = ModelUtil.getCodeableConceptBySystem(fhirResource.getCategory(),
				"2.16.840.1.113883.6.96");
		if (classConcept.isPresent()) {
			ModelUtil.setCodingToConcept(classConcept.get(), iCoding);
		} else {
			CodeableConcept newConcept = new CodeableConcept(
					new Coding(iCoding.getSystem(), iCoding.getCode(), iCoding.getDisplay()));
			fhirResource.addCategory(newConcept);
		}
	}

	public Optional<String> getCategory(DomainResource resource) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		Optional<CodeableConcept> categoryConcept = ModelUtil.getCodeableConceptBySystem(fhirResource.getCategory(),
				CodingSystem.ELEXIS_DOCUMENT_CATEGORY);
		if (categoryConcept.isPresent()) {
			Optional<ICoding> categoryCoding = ModelUtil.getCodeBySystem(
					ModelUtil.getCodingsFromConcept(categoryConcept.get()), CodingSystem.ELEXIS_DOCUMENT_CATEGORY);
			return Optional.of(categoryCoding.get().getDisplay());
		}
		return Optional.empty();
	}

	public void setCategory(DomainResource resource, String value) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		Optional<CodeableConcept> categoryConcept = ModelUtil.getCodeableConceptBySystem(fhirResource.getCategory(),
				CodingSystem.ELEXIS_DOCUMENT_CATEGORY);
		if (categoryConcept.isPresent()) {
			categoryConcept.get().getCodingFirstRep().setCode(value);
			categoryConcept.get().getCodingFirstRep().setDisplay(value);
		} else {
			CodeableConcept newConcept = new CodeableConcept(
					new Coding(CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), value, value));
			fhirResource.addCategory(newConcept);
		}
	}

	public Optional<String> getKeywords(DocumentReference fhir) {
		return Optional.ofNullable(fhir.getDescription());
	}
}
