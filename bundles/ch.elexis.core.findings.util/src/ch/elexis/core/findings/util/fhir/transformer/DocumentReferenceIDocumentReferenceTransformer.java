package ch.elexis.core.findings.util.fhir.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FindingsContentHelper;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IModelService;

@Component
public class DocumentReferenceIDocumentReferenceTransformer
		implements IFhirTransformer<DocumentReference, IDocumentReference> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService codeModelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	private IModelService findingsModelService;

	@Reference(target = "(storeid=ch.elexis.data.store.omnivore)")
	private IDocumentStore omnivoreStore;

	@Reference
	private IFindingsService findingsService;

	private FindingsContentHelper contentHelper;

	@Activate
	public void activate() {
		contentHelper = new FindingsContentHelper();
	}

	@Override
	public Optional<DocumentReference> getFhirObject(IDocumentReference localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			DocumentReference ret = (DocumentReference) resource.get();
			if (ret.getContent().isEmpty()) {
				// should add content or url
				IDocument document = localObject.getDocument();
				if (document != null) {
					try {
						DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
						Attachment attachment = new Attachment();
						attachment.setTitle(document.getTitle());
						attachment.setData(Base64.getEncoder().encode(IOUtils.toByteArray(document.getContent())));
						content.setAttachment(attachment);
						ret.addContent(content);
					} catch (IOException e) {
						LoggerFactory.getLogger(getClass()).error("Error reading content of document", e);
					}
				} else {
					LoggerFactory.getLogger(getClass()).error("No document with content found for reference " + ret);
				}
			}
			return Optional.of(ret);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IDocumentReference> getLocalObject(DocumentReference fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IDocumentReference> existing = findingsService.findById(fhirObject.getId(),
					IDocumentReference.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IDocumentReference> updateLocalObject(DocumentReference fhirObject,
			IDocumentReference localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IDocumentReference> createLocalObject(DocumentReference fhirObject) {
		if (fhirObject.getContent() != null && !fhirObject.getContent().isEmpty()) {
			DocumentReferenceContentComponent content = fhirObject.getContent().get(0);
			Attachment attachment = content.getAttachment();
			if(attachment.getData() != null || attachment.getUrl() != null) {
				if (fhirObject.getSubject() != null && fhirObject.getSubject().getId() != null) {
					Optional<IPatient> patientKontakt = codeModelService.load(fhirObject.getSubject().getId(),
							IPatient.class);
					if (patientKontakt.isPresent()) {
						IDocumentReference iDocumentReference = findingsService.create(IDocumentReference.class);
						contentHelper.setResource(fhirObject, iDocumentReference);
						iDocumentReference.setPatientId(patientKontakt.get().getId());
						IDocument document = createDocument(patientKontakt.get(), attachment, null);
						if (document != null) {
							fhirObject.getContent().clear();
							iDocumentReference.setDocument(document);
							findingsService.saveFinding(iDocumentReference);
						}
						return Optional.of(iDocumentReference);
					} else {
						LoggerFactory.getLogger(getClass())
								.error("Patient [" + fhirObject.getSubject().getId() + "] not found");
					}
				} else {
					LoggerFactory.getLogger(getClass()).error("No patient for document");
				}
			} else {
				LoggerFactory.getLogger(getClass()).error("No attachment for document");
			}
		}
		return Optional.empty();
	}

	private IDocument createDocument(IPatient patient, Attachment attachment, String category) {
		boolean success = false;
		IDocument ret = omnivoreStore.createDocument(patient.getId(), attachment.getTitle(), category);
		if (attachment.getData() != null) {
			try (InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(attachment.getData()))) {
				ret.setContent(in);
				success = true;
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass())
						.error("Error reading content from attachment data [" + attachment.getUrl() + "]", e);
			}
		} else if (attachment.getUrl() != null) {
			try {
				URL url = new URL(attachment.getUrl());
				try (InputStream in = url.openStream()) {
					ret.setContent(in);
					success = true;
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass())
							.error("Error reading content from url [" + attachment.getUrl() + "]", e);
				}
			} catch (MalformedURLException e) {
				LoggerFactory.getLogger(getClass()).error("Attachment url invalid", e);
			}
		}
		return success ? ret : null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return DocumentReference.class.equals(fhirClazz) && IDocumentReference.class.equals(localClazz);
	}

}
