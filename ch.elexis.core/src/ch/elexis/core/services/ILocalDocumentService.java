package ch.elexis.core.services;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Service interface to manage documents on the local file system. Documents (Omnivore, Brief) are
 * exported to the local file system.
 * 
 * @author thomas
 *
 */
public interface ILocalDocumentService {
	
	public interface ISaveHandler {
		/**
		 * Save the documetSource using the service.
		 * 
		 * @param documentSource
		 * @param service
		 * @return
		 */
		public boolean save(Object documentSource, ILocalDocumentService service);
	}
	
	public interface ILoadHandler {
		
		public InputStream load(Object documentSource);
	}
	
	/**
	 * Register a save handler implementation to be used saving an instance of clazz.
	 * 
	 * @param clazz
	 * @param saveHandler
	 */
	public void registerSaveHandler(Class<?> clazz, ISaveHandler saveHandler);
	
	public void registerLoadHandler(Class<?> clazz, ILoadHandler iLoadHandler);
	
	/**
	 * Save the document from the managed list. Throws an {@link IllegalStateException} if no
	 * {@link ISaveHandler} found for the documentSource.
	 * 
	 * @param documentSource
	 * @return
	 * @throws IllegalStateException
	 */
	public boolean save(Object documentSource) throws IllegalStateException;
	
	/**
	 * Add the document source to the managed local documents, and return the local file. The
	 * conflict handler is used if the file already exists. Throws an {@link IllegalStateException}
	 * if no {@link ILoadHandler} found for the documentSource.
	 * 
	 * @param documentSource
	 * @param conflictHandler
	 * @return the local file, or empty if aborted
	 * @throws IllegalStateException
	 */
	public Optional<File> add(Object documentSource, IConflictHandler conflictHandler)
		throws IllegalStateException;
	
	/**
	 * Remove the document from the managed list. Document will be removed even if local file is not
	 * accessible.
	 * 
	 * @param documentSource
	 */
	public void remove(Object documentSource);
	
	/**
	 * Remove the managed local document from the managed list. The conflict handler is used if the
	 * managed local file can not be deleted.
	 * 
	 * @param documentSource
	 */
	public void remove(Object documentSource, IConflictHandler conflictHandler);
	
	/**
	 * Test if the document source is managed by this service.
	 * 
	 * @param documentSource
	 * @return
	 */
	public boolean contains(Object documentSource);
	
	/**
	 * Get the current content of the local managed document.
	 * 
	 * @param documentSource
	 * @return
	 */
	public Optional<InputStream> getContent(Object documentSource);
	
	/**
	 * Get a list of all currently managed document source objects.
	 * 
	 * @return
	 */
	public List<Object> getAll();
	
	/**
	 * Returns the document cache directory
	 * 
	 * @return
	 */
	public String getDocumentCachePath();
}
