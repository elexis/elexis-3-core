package ch.elexis.core.findings.fhir.model;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.fhir.mapper.r4.findings.DocumentReferenceAccessor;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.fhir.model.service.DocumentStoreHolder;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IXid;

public class DocumentReference extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.DocumentReference>
		implements IDocumentReference {

	private DocumentReferenceAccessor accessor = new DocumentReferenceAccessor();

	public DocumentReference(ch.elexis.core.jpa.entities.DocumentReference entity) {
		super(entity);
	}

	@Override
	public String getPatientId() {
		return getEntity().getPatientId();
	}

	@Override
	public void setPatientId(String patientId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.r4.model.DocumentReference fhirDocumentReference = (org.hl7.fhir.r4.model.DocumentReference) resource
					.get();
			fhirDocumentReference.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}

		getEntity().setPatientId(patientId);
	}

	@Override
	public RawContentFormat getRawContentFormat() {
		return RawContentFormat.FHIR_JSON;
	}

	@Override
	public String getRawContent() {
		return getEntity().getContent();
	}

	@Override
	public void setRawContent(String content) {
		getEntity().setContent(content);
	}

	@Override
	public IDocument getDocument() {
		return DocumentStoreHolder.getDocument(getEntity().getDocumentStoreId(), getEntity().getDocumentId())
				.orElse(null);
	}

	@Override
	public void setDocument(IDocument document) {
		getEntity().setDocumentStoreId(document.getStoreId());
		getEntity().setDocumentId(document.getId());
		if (document.getPatient() != null) {
			setPatientId(document.getPatient().getId());
		}
		if (document.getAuthor() != null) {
			setAuthorId(document.getAuthor().getId());
		}
	}

	@Override
	public ICoding getDocumentClass() {
		return loadResource().map(i -> accessor.getDocumentClass((DomainResource) i)).get().stream().findFirst()
				.orElse(null);
	}

	@Override
	public void setDocumentClass(ICoding coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setDocumentClass((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

//	@Override
//	public String getCategory() {
//		return loadResource().map(i -> accessor.getCategory((DomainResource) i).orElse(null)).orElse(null);
//	}
//
//	@Override
//	public void setCategory(String value) {
//		Optional<IBaseResource> resource = loadResource();
//		if (resource.isPresent()) {
//			accessor.setCategory((DomainResource) resource.get(), value);
//			saveResource(resource.get());
//		}
//	}

	@Override
	public ICoding getPracticeSetting() {
		return loadResource().map(i -> accessor.getPracticeSetting((DomainResource) i)).get().stream().findFirst()
				.orElse(null);
	}

	@Override
	public void setPracticeSetting(ICoding coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setPracticeSetting((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

	@Override
	public ICoding getFacilityType() {
		return loadResource().map(i -> accessor.getFacilityType((DomainResource) i)).get().stream().findFirst()
				.orElse(null);

	}

	@Override
	public void setFacilityType(ICoding coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setFacilityType((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}

	}

//	@Override
//	public Optional<LocalDateTime> getDate() {
//		Optional<IBaseResource> resource = loadResource();
//		if (resource.isPresent()) {
//			org.hl7.fhir.r4.model.DocumentReference fhirDocument = (org.hl7.fhir.r4.model.DocumentReference) resource
//					.get();
//			try {
//				if (fhirDocument.hasDate()) {
//					return Optional.of(getLocalDateTime(fhirDocument.getDate()));
//				}
//			} catch (FHIRException e) {
//				LoggerFactory.getLogger(getClass()).error("Could not access date.", e);
//			}
//		}
//		return Optional.empty();
//	}
//
//	@Override
//	public void setDate(LocalDateTime time) {
//		Optional<IBaseResource> resource = loadResource();
//		if (resource.isPresent()) {
//			org.hl7.fhir.r4.model.DocumentReference fhirProcedureRequest = (org.hl7.fhir.r4.model.DocumentReference) resource
//					.get();
//			fhirProcedureRequest.setDate(getDate(time));
//
//			saveResource(resource.get());
//		}
//	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthorId() {
		return getEntity().getAuthorId();
	}

	@Override
	public void setAuthorId(String authorId) {
		getEntity().setAuthorId(authorId);
	}

//	@Override
//	public String getKeywords() {
//		return getEntity().getKeywords();
//	}
//
//	@Override
//	public void setKeywords(String keywords) {
//		getEntity().setKeywords(keywords);
//	}
}
