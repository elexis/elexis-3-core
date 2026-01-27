package ch.elexis.core.findings.util.fhir.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.Meta;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.fhir.mapper.r4.IDocumentAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.helper.FindingsContentHelper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IModelService;

@Component
public class DocumentReferenceIDocumentReferenceTransformer
		implements IFhirTransformer<DocumentReference, IDocumentReference> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private List<IDocumentStore> documentStores;

	@Reference
	private IFindingsService findingsService;

	private FindingsContentHelper contentHelper;

	private IDocumentAttributeMapper attributeMapper;

	@Activate
	public void activate() {
		contentHelper = new FindingsContentHelper();
		attributeMapper = new IDocumentAttributeMapper(documentStores);
	}

	@Override
	public Optional<DocumentReference> getFhirObject(IDocumentReference localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			DocumentReference ret = (DocumentReference) resource.get();
			if (ret.getContent().isEmpty()) {
				IDocument document = localObject.getDocument();
				if (document != null) {
					attributeMapper.elexisToFhir(document, ret, summaryEnum);

					ret.setId(new IdDt(DocumentReference.class.getSimpleName(), localObject.getId()));
					Meta meta = new Meta();
					meta.setLastUpdated(new Date(localObject.getLastupdate()));
					ret.setMeta(meta);

					if (ret.hasContent() && ret.getContent().get(0).hasAttachment()) {
						ret.getContent().get(0).getAttachment().setUrl(getBinaryUrl(ret));
					}
				} else {
					LoggerFactory.getLogger(getClass()).error("No document with content found for reference " + ret);
				}
			}
			return Optional.of(ret);
		}
		return Optional.empty();
	}

	private String getBinaryUrl(DocumentReference ret) {
		return ret.getId() + "/$binary-access-read";
	}

	@Override
	public Optional<IDocumentReference> getLocalObject(DocumentReference fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return findingsService.findById(localId.get(), IDocumentReference.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IDocumentReference> updateLocalObject(DocumentReference fhirObject,
			IDocumentReference localObject) {
		IDocument document = localObject.getDocument();
		attributeMapper.fhirToElexis(fhirObject, document);
		saveDocument(document);
		return Optional.of(localObject);
	}

	private void saveDocument(IDocument document) {
		Optional<IDocumentStore> documentStore = getDocumentStoreWithId(document.getStoreId());
		if (documentStore.isPresent()) {
			try {
				documentStore.get().saveDocument(document);
			} catch (ElexisException e) {
				LoggerFactory.getLogger(getClass()).error("Error saving document", e);
				throw new IFhirTransformerException("WARNING", "Error saving document", 500);
			}
		} else {
			throw new IFhirTransformerException("WARNING", "No document store for document", 412);
		}
	}

	@Override
	public Optional<IDocumentReference> createLocalObject(DocumentReference fhirObject) {
		if (fhirObject.getContent() != null && !fhirObject.getContent().isEmpty()) {
			DocumentReferenceContentComponent content = fhirObject.getContent().get(0);
			Attachment attachment = content.getAttachment();
			Optional<String> patientId = FhirUtil.getId(fhirObject.getSubject());
			if (patientId.isPresent()) {
				Optional<IPatient> patient = coreModelService.load(patientId.get(), IPatient.class);
				if (!patient.isPresent()) {
					throw new IFhirTransformerException("WARNING", "Invalid patient", 412);
				}
			}

			IDocumentStore documentStore = getDocumentStoreId(fhirObject);
			IDocument document = createDocument(patientId.orElse(null), attachment, attributeMapper.getAccessor()
					.getCategory(fhirObject).orElse(documentStore.getCategoryDefault().getName()), documentStore);
			IDocumentReference iDocumentReference = findingsService.create(IDocumentReference.class);
			iDocumentReference.setDocument(document);
			attributeMapper.fhirToElexis(fhirObject, document);
			saveDocument(document);

			fhirObject.getContent().clear();
			contentHelper.setResource(fhirObject, iDocumentReference);

			findingsService.saveFinding(iDocumentReference);
			return Optional.of(iDocumentReference);
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
		if (!documentStores.isEmpty()) {
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
		return DocumentReference.class.equals(fhirClazz) && IDocumentReference.class.equals(localClazz);
	}

}
