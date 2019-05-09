package ch.elexis.core.importer.div.tasks.test.mock;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ITag;
import ch.elexis.core.model.IXid;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.types.DocumentStatus;

@Component(immediate = true, property = "storeid=ch.elexis.data.store.omnivore")
public class MockOmnivoreDocumentStore implements IDocumentStore {
	
	@Override
	public String getId(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<IDocument> getDocuments(String patientId, String authorId, ICategory category,
		List<ITag> tag){
		return Collections.emptyList();
	}
	
	@Override
	public Optional<IDocument> loadDocument(String id){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<InputStream> loadContent(IDocument document){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IDocument createDocument(String patientId, String title, String categoryName){
		return new IDocument() {
			
			@Override
			public void setDeleted(boolean value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isDeleted(){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public IXid getXid(String domain){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Long getLastupdate(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getLabel(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getId(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean addXid(String domain, String id, boolean updateIfExists){
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void setTitle(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setStoreId(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setStatus(DocumentStatus value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setPatient(IPatient value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setMimeType(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setLastchanged(Date value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setKeywords(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setExtension(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDescription(String value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCreated(Date value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setContent(InputStream content){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCategory(ICategory value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAuthor(IContact value){
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getTitle(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getStoreId(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public DocumentStatus getStatus(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IPatient getPatient(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getMimeType(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Date getLastchanged(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getKeywords(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<IHistory> getHistory(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getExtension(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getDescription(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Date getCreated(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public InputStream getContent(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ICategory getCategory(){
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IContact getAuthor(){
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
	
	@Override
	public IDocument saveDocument(IDocument document) throws ElexisException{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IDocument saveDocument(IDocument document, InputStream content) throws ElexisException{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void removeDocument(IDocument document){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<ICategory> getCategories(){
		return Collections.emptyList();
	}
	
	@Override
	public ICategory getCategoryDefault(){
		return new ICategory() {
			
			@Override
			public void setName(String value){}
			
			@Override
			public String getName(){
				return "mockCategory";
			}
		};
	}
	
	@Override
	public ICategory createCategory(String name){
		return new ICategory() {
			
			@Override
			public void setName(String value){}
			
			@Override
			public String getName(){
				return name;
			}
		};
	}
	
	@Override
	public void removeCategory(IDocument iDocument, String newCategory)
		throws IllegalStateException{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void renameCategory(ICategory category, String newCategory){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Optional<Object> getPersistenceObject(IDocument iDocument){
		// TODO Auto-generated method stub
		return null;
	}
	
}
