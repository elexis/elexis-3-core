package ch.elexis.core.findings.util.fhir.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IModelService;

@Component
public class DocumentReferenceIDocumentTransformer implements IFhirTransformer<DocumentReference, IDocument> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private List<IDocumentStore> documentStores;

	@Override
	public Optional<DocumentReference> getFhirObject(IDocument localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		DocumentReference ret = new DocumentReference();
		FhirUtil.setVersionedIdPartLastUpdatedMeta(DocumentReference.class, ret, localObject);

		ret.setDate(localObject.getLastchanged());

		ret.addCategory(new CodeableConcept(new Coding(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(),
				localObject.getStoreId(), StringUtils.EMPTY)));

		if (localObject.getCategory() != null) {
			ICategory category = localObject.getCategory();
			CodeableConcept newConcept = new CodeableConcept(new Coding(
					CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), category.getName(), category.getName()));
			ret.addCategory(newConcept);
		}

		if (localObject instanceof IDocumentTemplate
				&& StringUtils.isNotBlank(((IDocumentTemplate) localObject).getTemplateTyp())) {
			ret.addCategory(new CodeableConcept(new Coding(CodingSystem.ELEXIS_DOCUMENT_TEMPLATE_TYP.getSystem(),
					((IDocumentTemplate) localObject).getTemplateTyp(), StringUtils.EMPTY)));
		}

		if (localObject.getPatient() != null) {
			ret.setSubject(new org.hl7.fhir.r4.model.Reference(new IdDt("Patient", localObject.getPatient().getId())));
		}

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		String title = localObject.getTitle();
		String extension = FilenameUtils.getExtension(title);
		String documentExtension = localObject.getExtension();
		if (StringUtils.isEmpty(extension)
				|| (StringUtils.isNotBlank(documentExtension) && !documentExtension.equals(extension))) {
			title = title + "." + documentExtension;
		}
		attachment.setTitle(title);
		attachment.setUrl(getBinaryUrl(ret));
		attachment.setCreation(localObject.getCreated());
		content.setAttachment(attachment);
		ret.addContent(content);

		return Optional.of(ret);
	}

	private String getBinaryUrl(DocumentReference ret) {
		return ret.getId() + "/$binary-access-read";
	}

	@Override
	public Optional<IDocument> getLocalObject(DocumentReference fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				for (IDocumentStore iDocumentStore : documentStores) {
					IDocument ret = iDocumentStore.loadDocument(localId.get()).orElse(null);
					if (ret != null) {
						return Optional.of(ret);
					}
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IDocument> updateLocalObject(DocumentReference fhirObject, IDocument localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IDocument> createLocalObject(DocumentReference fhirObject) {
		if (fhirObject.getContent() != null && !fhirObject.getContent().isEmpty()) {

			DocumentReferenceContentComponent content = fhirObject.getContent().get(0);
			Attachment attachment = content.getAttachment();

			Optional<String> patientId = FhirUtil.getId(fhirObject.getSubject());
			IDocumentStore documentStore = getDocumentStoreId(fhirObject);
			IDocument document = createDocument(patientId.orElse(null), attachment, "TODOCATEGORY",
					documentStore);
			try {
				if (fhirObject.hasDate()) {
					document.setLastchanged(fhirObject.getDate());
				}
				if (attachment != null && attachment.hasCreation()) {
					document.setCreated(attachment.getCreation());
				}
				documentStore.saveDocument(document);
			} catch (ElexisException e) {
				LoggerFactory.getLogger(getClass()).error("Error updating document", e);
			}
			return Optional.of(document);

		}
		return Optional.empty();
	}

	private IDocumentStore getDocumentStoreId(DocumentReference fhirObject) {
		if (fhirObject.hasCategory()) {
			Optional<String> storeIdCode = FhirUtil
					.getCodeFromConceptList(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(), fhirObject.getCategory());
			if (storeIdCode.isPresent()) {
				return getDocumentStoreWithId(storeIdCode.get()).orElse(getDefaultDocumentStore());
			}
		}
		return getDefaultDocumentStore();
	}

	private IDocumentStore getDefaultDocumentStore() {
		if (documentStores.size() > 0) {
			return documentStores.stream().filter(ds -> ds.getId().toLowerCase().contains("omnivore")).findFirst()
					.orElse(documentStores.get(0));
		}
		throw new IllegalStateException("No document stores available");
	}

	private Optional<IDocumentStore> getDocumentStoreWithId(String id) {
		return documentStores.stream().filter(ds -> ds.getId().equals(id)).findFirst();
	}

	private IDocument createDocument(String patientId, Attachment attachment, String category,
			IDocumentStore documentStore) {
		IDocument ret = documentStore.createDocument(patientId, attachment.getTitle(), category);
		if (StringUtils.isNotBlank(attachment.getContentType())) {
			ret.setMimeType(attachment.getContentType());
		}
		try {
			documentStore.saveDocument(ret);
			if (attachment != null) {
				if (attachment.getData() != null) {
					try (InputStream in = new ByteArrayInputStream(attachment.getData())) {
						documentStore.saveDocument(ret, in);
					} catch (IOException | ElexisException e) {
						LoggerFactory.getLogger(getClass())
								.error("Error reading content from attachment data [" + attachment.getUrl() + "]", e);
					}
				} else if (StringUtils.isNotBlank(attachment.getUrl()) && attachment.getUrl().contains("://")) {
					try {
						URL url = new URL(attachment.getUrl());
						try (InputStream in = url.openStream()) {
							documentStore.saveDocument(ret, in);
						} catch (IOException | ElexisException e) {
							LoggerFactory.getLogger(getClass())
									.error("Error reading content from url [" + attachment.getUrl() + "]", e);
						}
					} catch (MalformedURLException e) {
						LoggerFactory.getLogger(getClass()).error("Attachment url invalid", e);
					}
				}
			}
		} catch (ElexisException e) {
			LoggerFactory.getLogger(getClass()).error("Error creating document", e);
		}
		return ret;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return DocumentReference.class.equals(fhirClazz) && IDocument.class.equals(localClazz);
	}
}
