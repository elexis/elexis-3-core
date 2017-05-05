package ch.elexis.core.documents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.ITag;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IDocumentStore.Capability;

@Component(service = DocumentStore.class)
public class DocumentStore {
	
	private static final String DEFAULT_STORE_ID = "ch.elexis.data.store.omnivore";
	
	final ConcurrentMap<String, IDocumentStore> services = new ConcurrentHashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addDocumentStore(IDocumentStore store){
		services.put(store.getId(), store);
	}
	
	void removeDocumentStore(IDocumentStore store){
		services.remove(store.getId());
	}
	
	/**
	 * Returns all documents from each registered store
	 * 
	 * @param patientId
	 * @param authorId
	 * @param category
	 * @param tag
	 * @return
	 */
	public List<IDocument> getDocuments(String patientId, String authorId, ICategory category,
		List<ITag> tag){
		List<IDocument> documents = new ArrayList<>();
		services.values().forEach(
			service -> documents.addAll(service.getDocuments(patientId, authorId, category, tag)));
		return documents;
	}
	
	public Map<ICategory, List<IDocument>> getDocumentsByPatientId(String patientId){
		Map<ICategory, List<IDocument>> map = new HashMap<>();
		List<IDocument> documents = getDocuments(patientId, null, null, null);
		for (IDocument iDocument : documents) {
			List<IDocument> categoryDocuments = map.get(iDocument.getCategory());
			if (categoryDocuments == null) {
				categoryDocuments = new ArrayList<>();
			}
			categoryDocuments.add(iDocument);
			map.put(iDocument.getCategory(), categoryDocuments);
		}
		return map;
	}
	
	public List<IDocument> getDocumentsByCategory(String patientId,
		ICategory iCategory){
		return getDocuments(patientId, null, iCategory, null);
	}
	
	/**
	 * Loads a document without content from the given document store
	 * 
	 * @param id
	 * @param storeId
	 * @return
	 */
	public Optional<IDocument> loadDocument(String id, String storeId){
		return getService(storeId).loadDocument(id);
	}
	
	/**
	 * Loads the document content from the specified store
	 * 
	 * @param document
	 * @return
	 */
	public Optional<InputStream> loadContent(IDocument document){
		return getService(document.getStoreId()).loadContent(document);
	}
	
	/**
	 * Saves the content to a file and returns the aboslute path of the file.
	 * 
	 * @param document
	 * @param filePath
	 * @return
	 * @throws ElexisException
	 */
	public String saveContentToFile(IDocument document, String filePath) throws ElexisException{
		Optional<InputStream> in = getService(document.getStoreId()).loadContent(document);
		if (in.isPresent()) {
			try {
				File file = new File(filePath);
				FileUtils.copyInputStreamToFile(in.get(), file);
				return file.getAbsolutePath();
			} catch (IOException e) {
				throw new ElexisException("cannot save content", e);
			}
		}
		return null;
	}
	
	/**
	 * Saves the content to a temp file and returns the absolute path of the temp file. if no
	 * tempfileprefix is defined default is 'export'. if no tempfileSuffix is defined default is
	 * 'tmp'.
	 * 
	 * @param document
	 * @param tempFilePrefix
	 * @param tempFileSuffix
	 * @param deleteOnExit
	 * @return
	 * @throws ElexisException
	 */
	public String saveContentToTempFile(IDocument document, String tempFilePrefix,
		String tempFileSuffix, boolean deleteOnExit)
		throws ElexisException{
		Optional<InputStream> in = getService(document.getStoreId()).loadContent(document);
		if (in.isPresent()) {
			try {
				if (StringUtils.isEmpty(tempFilePrefix)) {
					tempFilePrefix = "export";
				}
				if (StringUtils.isEmpty(tempFileSuffix)) {
					tempFileSuffix = "tmp";
				}
				File tmpFile =
					new File(FileUtils.getTempDirectory(), tempFilePrefix + "." + tempFileSuffix);
				FileUtils.copyInputStreamToFile(in.get(), tmpFile);
				if (deleteOnExit) {
					tmpFile.deleteOnExit();
				}
				return tmpFile.getAbsolutePath();
			} catch (IOException e) {
				throw new ElexisException("cannot save content", e);
			}
		}
		return null;
	}
	
	/**
	 * Persists or updates a document without content to the specified store
	 * 
	 * @param document
	 * @return
	 * @throws ElexisException
	 */
	public IDocument saveDocument(IDocument document) throws ElexisException{
		return getService(document.getStoreId()).saveDocument(document);
	}
	
	/**
	 * Perists or updates a document with content to the specified store
	 * 
	 * @param document
	 * @param content
	 * @return
	 * @throws ElexisException
	 */
	public IDocument saveDocument(IDocument document, InputStream content) throws ElexisException{
		return getService(document.getStoreId()).saveDocument(document, content);
	}
	
	/**
	 * Removes a document from the store
	 * 
	 * @param document
	 */
	public void removeDocument(IDocument document){
		getService(document.getStoreId()).removeDocument(document);
	}
	
	/**
	 * Creates an new document instance
	 * 
	 * @param storeId
	 * @param patientId
	 * @param path
	 * @param categoryName
	 * 
	 * @return
	 */
	public IDocument createDocument(String storeId, String patientId, String path,
		String categoryName){
		String title = FilenameUtils.getName(path);
		String mimeType = FilenameUtils.getExtension(path);
		IDocument iDocument = getService(storeId != null ? storeId : DEFAULT_STORE_ID)
			.createDocument(patientId,
			title, categoryName);
		iDocument.setMimeType(mimeType);
		iDocument.setExtension(mimeType);
		return iDocument;
	}
	
	/**
	 * Returns all categories from the given store of the document
	 * 
	 * @return
	 */
	public List<ICategory> getCategories(IDocument iDocument){
		List<ICategory> results = new ArrayList<>();
		Set<String> checkNames = new HashSet<>();
		
		IDocumentStore service = getService(iDocument.getStoreId());
		List<ICategory> categories = service.getCategories();
		for (ICategory category : categories) {
			if (checkNames.add(category.getName())) {
				results.add(category);
			}
		}
		Collections.sort(results, new Comparator<ICategory>() {
			@Override
			public int compare(ICategory o1, ICategory o2){
				if (o1 != null && o2 != null) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
				else {
					return o1 != null ? -1 : (o2 != null ? 1 : 0);
				}
			}
		});
		return results;
	}
	
	/**
	 * Returns the default category for a store.
	 * 
	 * @param iDocument
	 * @return
	 */
	public ICategory getDefaultCategory(IDocument iDocument){
		return getService(iDocument.getStoreId()).getCategoryDefault();
	}
	
	/**
	 * Creates a new category by name
	 * 
	 * @param iDocument
	 * @param name
	 * @return
	 */
	public ICategory createCategory(IDocument iDocument, String name){
		return getService(iDocument.getStoreId()).createCategory(name);
	}
	
	/**
	 * 
	 * @param iDocument
	 * @param newCategory
	 * @return
	 */
	public ICategory renameCategory(IDocument iDocument, String newCategory){
		
		ICategory oldCategory = iDocument.getCategory();
		getService(iDocument.getStoreId()).renameCategory(oldCategory, newCategory);
		return getService(iDocument.getStoreId()).createCategory(newCategory);
	}
	
	/**
	 * Removes the category for a {@link IDocument} if no other references for that category exists
	 * and creates a new {@link ICategory}
	 * 
	 * @param iDocument
	 * @param newCategory
	 * @throws ElexisException
	 *             if other category to document references exists
	 * @return
	 */
	public ICategory removeCategory(IDocument iDocument, String newCategory)
		throws ElexisException{
		
		ICategory oldCategory = iDocument.getCategory();
		try
		{
			getService(iDocument.getStoreId()).removeCategory(iDocument, newCategory);
			
		}
		catch (IllegalStateException e) {
			throw new ElexisException("category references exists", e);
		}
		return getService(iDocument.getStoreId()).createCategory(newCategory);
	}

	/**
	 * Returns all tags from each registered store
	 * 
	 * @return
	 */
	public List<ITag> getTags(){
		/*List<ITag> tags = new ArrayList<>();
		services.values().forEach(service -> tags.addAll(service.getTags()));
		return tags;*/
		return Collections.emptyList();
	}
	
	private IDocumentStore getService(String storeId){
		IDocumentStore iDocumentStore = services.get(storeId);
		if (iDocumentStore != null) {
			return iDocumentStore;
		}
		throw new IllegalAccessError("no store defined for id: " + storeId);
	}
	
	public boolean isAllowed(IDocument document, Capability restricted){
		return getService(document.getStoreId()).isAllowed(restricted);
	}
	
	public Optional<IPersistentObject> getPersistenceObject(IDocument document){
		return getService(document.getStoreId()).getPersistenceObject(document);
	}
}
