package ch.elexis.core.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ITag;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.internal.dto.CategoryDocumentDTO;

@Component(property = "storeid=ch.elexis.data.store.brief")
public class BriefDocumentStore implements IDocumentStore {

	private static final String STORE_ID = "ch.elexis.data.store.brief";

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Override
	public String getId() {
		return STORE_ID;
	}

	@Override
	public String getName() {
		return "Briefe";
	}

	@Override
	public List<IDocument> getDocuments(String patientId, String authorId, ICategory category, List<ITag> tag) {

		Optional<IPatient> patient = coreModelService.load(patientId, IPatient.class);
		if (patient.isPresent()) {
			IQuery<IDocumentLetter> query = coreModelService.getQuery(IDocumentLetter.class);
			query.and(ModelPackage.Literals.IDOCUMENT__PATIENT, COMPARATOR.EQUALS, patient.get());

			if (authorId != null) {
				Optional<IContact> author = coreModelService.load(authorId, IContact.class);
				author.ifPresent(a -> {
					query.and(ModelPackage.Literals.IDOCUMENT__AUTHOR, COMPARATOR.EQUALS, a);
				});
			}
			if (category != null && category.getName() != null) {
				query.and(ModelPackage.Literals.IDOCUMENT__CATEGORY, COMPARATOR.EQUALS, category.getName());
			}

			@SuppressWarnings("unchecked")
			List<IDocument> results = (List<IDocument>) ((List<?>) query.execute());
			results.parallelStream().forEach(d -> d.setStoreId(STORE_ID));
			return results;
		}
		return Collections.emptyList();
	}

	@Override
	public List<ICategory> getCategories() {
		List<ICategory> categories = new ArrayList<>();
		categories.add(new CategoryDocumentDTO(BriefConstants.TEMPLATE));
		categories.add(new CategoryDocumentDTO(BriefConstants.AUZ));
		categories.add(new CategoryDocumentDTO(BriefConstants.RP));
		categories.add(new CategoryDocumentDTO(BriefConstants.UNKNOWN));
		categories.add(new CategoryDocumentDTO(BriefConstants.LABOR));
		categories.add(new CategoryDocumentDTO(BriefConstants.BESTELLUNG));
		categories.add(new CategoryDocumentDTO(BriefConstants.RECHNUNG));
		return categories;
	}

	@Override
	public void removeDocument(IDocument document) {
		Optional<IDocumentLetter> existing = coreModelService.load(document.getId(), IDocumentLetter.class);
		existing.ifPresent(d -> {
			coreModelService.delete(d);
		});
	}

	@Override
	public IDocument saveDocument(IDocument document) throws ElexisException {
		return save(document, null);
	}

	@Override
	public IDocument saveDocument(IDocument document, InputStream content) throws ElexisException {
		return save(document, content);
	}

	private IDocument save(IDocument document, InputStream content) throws ElexisException {
		try {
			if (content != null) {
				document.setContent(content);
			}
			coreModelService.save(document);
			return document;
		} catch (PersistenceException e) {
			throw new ElexisException("cannot save", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<IDocument> loadDocument(String id) {
		return (Optional<IDocument>) (Optional<?>) coreModelService.load(id, IDocumentLetter.class);
	}

	@Override
	public Optional<InputStream> loadContent(IDocument document) {
		return Optional.ofNullable(document.getContent());
	}

	@Override
	public boolean isAllowed(Capability restricted) {
		if (Capability.CATEGORY.equals(restricted) || Capability.KEYWORDS.equals(restricted)) {
			return false;
		}
		return IDocumentStore.super.isAllowed(restricted);
	}

	@Override
	public IDocument createDocument(String patientId, String title, String categoryName) {
		IDocumentLetter letter = coreModelService.create(IDocumentLetter.class);
		letter.setStoreId(STORE_ID);
		letter.setTitle(title);
		letter.setPatient(coreModelService.load(patientId, IPatient.class).orElse(null));
		ICategory iCategory = categoryName != null ? new CategoryDocumentDTO(categoryName) : getCategoryDefault();
		letter.setCategory(iCategory);
		return letter;
	}

	@Override
	public ICategory getCategoryDefault() {
		return new CategoryDocumentDTO(BriefConstants.UNKNOWN);
	}

	@Override
	public Optional<Object> getPersistenceObject(IDocument iDocument) {
		return Optional.ofNullable(coreModelService.load(iDocument.getId(), IDocumentLetter.class).orElse(null));
	}

	@Override
	public ICategory createCategory(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeCategory(IDocument iDocument, String newCategory) throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void renameCategory(ICategory category, String newCategory) {
		throw new UnsupportedOperationException();
	}

}
