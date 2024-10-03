package ch.elexis.core.services;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.ITag;

/**
 * Service interface for document access.
 *
 * @author thomas
 *
 */
public interface IDocumentStore {

	public enum Capability {
		CATEGORY, KEYWORDS
	}

	/**
	 * Get the id of the implementation.
	 *
	 * @return
	 */
	public String getId();

	/**
	 * Get the name of the implementation, that can be displayed to the user
	 *
	 * @return
	 */
	public String getName();

	/**
	 * Get documents of a patient. Additional filter parameters are optional.
	 *
	 * @param patientId
	 * @param authorId
	 * @param category
	 * @param tag
	 * @return
	 */
	public List<IDocument> getDocuments(@NonNull String patientId, @Nullable String authorId,
			@Nullable ICategory category, @Nullable List<ITag> tag);

	/**
	 * Get the {@link IDocumentTemplate}s.
	 * 
	 * @return
	 */
	public List<IDocumentTemplate> getDocumentTemplates(boolean includeSystem);

	/**
	 * Load a {@link IDocument} with the id from the store.
	 *
	 * @param id
	 * @return
	 */
	public Optional<IDocument> loadDocument(String id);

	/**
	 * Load the content of the document.
	 *
	 * @param document
	 * @return
	 */
	public Optional<InputStream> loadContent(IDocument document);

	/**
	 * Creates a empty {@link IDocument} for the given patientId
	 *
	 * @param patientId
	 * @param title
	 * @param categoryName
	 *
	 * @return
	 */
	public IDocument createDocument(@NonNull String patientId, String title, String categoryName);

	/**
	 * Save changes to the meta information of the document. Not the content.
	 *
	 * @param document
	 * @return saved document
	 * @throws ElexisException
	 */
	public IDocument saveDocument(IDocument document) throws ElexisException;

	/**
	 * Save changes to the meta information and the content of the document.
	 *
	 * @param document
	 * @param content
	 * @return
	 * @throws ElexisException
	 */
	public IDocument saveDocument(IDocument document, InputStream content) throws ElexisException;

	/**
	 * Remove the {@link IDocument} from the store.
	 *
	 * @param document
	 */
	public void removeDocument(IDocument document);

	/**
	 * Get a list of all categories known to the store.
	 *
	 * @return
	 */
	public List<ICategory> getCategories();

	/**
	 * Returns the default category for a store.
	 *
	 * @return
	 */
	public ICategory getCategoryDefault();

	/**
	 * Returns an existing category by its name
	 * 
	 * @param name
	 * @return
	 * @since 3.12
	 */
	default Optional<ICategory> getCategoryByName(String name) {
		return getCategories().stream().filter(c -> name.equalsIgnoreCase(c.getName())).findFirst();
	}

	/**
	 * Creates or returns an existing {@link ICategory} with the provided name from
	 * the store.
	 *
	 * @param name
	 * @return
	 */
	public ICategory createCategory(String name);

	/**
	 * Remove the {@link ICategory} from the store. Only empty {@link ICategory} can
	 * be removed. If there are {@link IDocument} referencing the {@link ICategory}
	 * an {@link IllegalStateException} is thrown.
	 *
	 * @param iDocument
	 * @param newCategory
	 */
	public void removeCategory(IDocument iDocument, String newCategory) throws IllegalStateException;

	/**
	 * Rename the {@link ICategory} from the store.
	 *
	 * @param category
	 * @param newCategory
	 */
	public void renameCategory(ICategory category, String newCategory);

	/**
	 * Remove the {@link ICategory} from the store. If there are any
	 * {@link IDocument} referencing the provided category, move them to newCategory
	 * 
	 * @param category
	 * @param newCategory to move to, or default category if <code>null</code>
	 * @return
	 * @since 3.12
	 */
	void removeCategory(ICategory category, @Nullable ICategory newCategory);

	public default boolean isAllowed(Capability restricted) {
		return true;
	}

	/**
	 *
	 * @param iDocument
	 * @return
	 */
	public Optional<Object> getPersistenceObject(IDocument iDocument);
}
