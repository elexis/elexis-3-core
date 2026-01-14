package ch.elexis.core.fhir.mapper.r4;

import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.findings.DocumentReferenceAccessor;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IDocumentStore.Capability;

public class IDocumentAttributeMapper extends IdentifiableDomainResourceAttributeMapper<IDocument, DocumentReference> {

	private DocumentReferenceAccessor accessor;
	private List<IDocumentStore> documentStores;

	public IDocumentAttributeMapper(List<IDocumentStore> documentStores) {
		super(DocumentReference.class);

		accessor = new DocumentReferenceAccessor();
		this.documentStores = documentStores;
	}

	public DocumentReferenceAccessor getAccessor() {
		return accessor;
	}

	@Override
	public void fullElexisToFhir(IDocument elexis, DocumentReference fhir, SummaryEnum summaryEnum) {
		mapIdAndMeta(DocumentReference.class, fhir, elexis);

		fhir.addIdentifier().setSystem("http://elexis.info/referenced-document-id").setValue(elexis.getId());

		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(elexis, fhir);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

		// we replace all entries in the existing list
		fhir.getCategory().clear();

		fhir.addCategory(new CodeableConcept(
				new Coding(CodingSystem.ELEXIS_DOCUMENT_STOREID.getSystem(), elexis.getStoreId(), StringUtils.EMPTY)));

		fhir.setDate(elexis.getLastchanged());

		if (elexis.getCategory() != null) {
			ICategory category = elexis.getCategory();
			CodeableConcept newConcept = new CodeableConcept(new Coding(
					CodingSystem.ELEXIS_DOCUMENT_CATEGORY.getSystem(), category.getName(), category.getName()));
			fhir.addCategory(newConcept);
		}

		if (elexis instanceof IDocumentTemplate
				&& StringUtils.isNotBlank(((IDocumentTemplate) elexis).getTemplateTyp())) {
			fhir.addCategory(new CodeableConcept(new Coding(CodingSystem.ELEXIS_DOCUMENT_TEMPLATE_TYP.getSystem(),
					((IDocumentTemplate) elexis).getTemplateTyp(), StringUtils.EMPTY)));
		}

		if (StringUtils.isNotBlank(elexis.getKeywords())) {
			fhir.setDescription(elexis.getKeywords());
		}

		DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
		Attachment attachment = new Attachment();
		String title = elexis.getTitle();
		String extension = FilenameUtils.getExtension(title);
		String documentExtension = elexis.getExtension();
		if (StringUtils.isEmpty(extension)
				|| (StringUtils.isNotBlank(documentExtension) && !documentExtension.equals(extension))) {
			title = title + "." + documentExtension;
		}
		attachment.setTitle(title);
		attachment.setUrl(getBinaryUrl(fhir));
		attachment.setCreation(elexis.getCreated());
		content.setAttachment(attachment);
		fhir.addContent(content);
	}

	private String getBinaryUrl(DocumentReference ret) {
		return ret.getId() + "/$binary-access-read";
	}

	@Override
	public void fullFhirToElexis(DocumentReference fhir, IDocument elexis) {
		Optional<IDocumentStore> documentStore = getDocumentStoreWithId(elexis.getStoreId());
		if (fhir.hasDate()) {
			elexis.setLastchanged(fhir.getDate());
		}
		if (fhir.hasContent() && fhir.getContent().get(0).hasAttachment()
				&& fhir.getContent().get(0).getAttachment().hasTitle()) {
			elexis.setTitle(fhir.getContent().get(0).getAttachment().getTitle());
		}
		if (fhir.hasContent() && fhir.getContent().get(0).hasAttachment()
				&& fhir.getContent().get(0).getAttachment().hasCreation()) {
			elexis.setCreated(fhir.getContent().get(0).getAttachment().getCreation());
		}
		documentStore.ifPresent(ds -> {
			Optional<String> category = accessor.getCategory(fhir);
			if (category.isPresent() && ds.isAllowed(Capability.CATEGORY)) {
				elexis.setCategory(ds.createCategory(category.get()));
			}
			Optional<String> keywords = accessor.getKeywords(fhir);
			if (ds.isAllowed(Capability.KEYWORDS)) {
				elexis.setKeywords(keywords.orElse(null));
			}
		});
	}

	private Optional<IDocumentStore> getDocumentStoreWithId(String id) {
		return documentStores.stream().filter(ds -> ds.getId().equals(id)).findFirst();
	}
}
