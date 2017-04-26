package ch.elexis.core.services;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.ITag;

/**
 * Service interface for document access.
 * 
 * @author thomas
 *
 */
public interface IDocumentStore {
	
	public enum Capability {
			CATEGORY, TAG
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
	 * 
	 * @return
	 */
	public IDocument createDocument(@NonNull String patientId, String title);
	
	/**
	 * Save changes to the meta information of the document. Not the content.
	 * 
	 * @param document
	 * @return saved document
	 * @throws ElexisException
	 */
	public IDocument saveDocument(IDocument document) throws ElexisException;
	
	/**
	 * Save changes to the meta information and the content of the document. If an
	 * {@link InputStream} is given it will be closed.
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
	 * Creates a {@link ICategory} with the provided name to the store.
	 * 
	 * @param name
	 * @return
	 */
	public ICategory createCategory(String name);
	
	/**
	 * Remove the {@link ICategory} from the store. Only empty {@link ICategory} can be removed. If
	 * there are {@link IDocument} referencing the {@link ICategory} an
	 * {@link IllegalStateException} is thrown.
	 * 
	 * @param category
	 */
	public void removeCategory(ICategory category) throws IllegalStateException;
	
	/**
	 * Get a list of all {@link ITag} known to the store.
	 * 
	 * @return
	 */
	public List<ITag> getTags();
	
	/**
	 * Add a {@link ITag} with the provided name to the store. If a {@link ITag} with that name is
	 * already known to the store, it will be returned.
	 * 
	 * @param name
	 * @return
	 */
	public ITag addTag(String name);
	
	/**
	 * Remove the {@link ITag} from the store. Only empty {@link ITag} can be removed. If there are
	 * {@link IDocument} referencing the {@link ITag} an {@link IllegalStateException} is thrown.
	 * 
	 * @param tag
	 */
	public void removeTag(ITag tag) throws IllegalStateException;
	
	public default boolean isAllowed(Capability restricted)
	{
		return true;
	}
	
}
