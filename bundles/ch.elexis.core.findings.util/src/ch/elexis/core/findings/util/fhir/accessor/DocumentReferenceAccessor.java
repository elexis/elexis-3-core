package ch.elexis.core.findings.util.fhir.accessor;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.DomainResource;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.util.ModelUtil;

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
		List<CodeableConcept> codeableConcept = fhirResource.getCategory();
		if (codeableConcept != null && !codeableConcept.isEmpty()) {
			return ModelUtil.getCodingsFromConcept(codeableConcept.get(0));
		}
		return Collections.emptyList();
	}

	public void setDocumentClass(DomainResource resource, ICoding coding) {
		org.hl7.fhir.r4.model.DocumentReference fhirResource = (org.hl7.fhir.r4.model.DocumentReference) resource;
		CodeableConcept codeableConcept = new CodeableConcept();
		ModelUtil.setCodingToConcept(codeableConcept, coding);
		fhirResource.setCategory(Collections.singletonList(codeableConcept));
	}
}
