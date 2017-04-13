package ch.elexis.core.services;

import java.io.InputStream;
import java.util.List;

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
	 * Load the content of the document.
	 * 
	 * @param document
	 * @return
	 */
	public InputStream loadDocument(IDocument document);
	
	/**
	 * Save changes to the meta information of the document. Not the content.
	 * 
	 * @param document
	 */
	public void saveDocument(IDocument document);
	
	/**
	 * Save changes to the meta information and the content of the document.
	 * 
	 * @param document
	 * @param content
	 */
	public void saveDocument(IDocument document, InputStream content);
	
	/**
	 * Get a list of all categories known to the store.
	 * 
	 * @return
	 */
	public List<ICategory> getCategories();
	
	/**
	 * Add a {@link ICategory} with the provided name to the store. If a {@link ICategory} with that
	 * name is already known to the store, it will be returned.
	 * 
	 * @param name
	 * @return
	 */
	public ICategory addCategory(String name);
	
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
	public void removeTag(ITag tag);
}
