package ch.elexis.core.model;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import ch.elexis.core.jpa.entities.DocHandle;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.types.DocumentStatus;

public class DocumentDocHandle extends AbstractIdDeleteModelAdapter<DocHandle>
		implements IDocumentHandle {
	
	public DocumentDocHandle(DocHandle entity){
		super(entity);
	}
	
	@Override
	public String getTitle(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setTitle(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getDescription(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setDescription(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public DocumentStatus getStatus(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setStatus(DocumentStatus value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Date getCreated(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setCreated(Date value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Date getLastchanged(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setLastchanged(Date value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getMimeType(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setMimeType(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ICategory getCategory(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setCategory(ICategory value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<IHistory> getHistory(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getStoreId(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setStoreId(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getExtension(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setExtension(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getKeywords(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setKeywords(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public IPatient getPatient(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setPatient(IPatient value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public IContact getAuthor(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setAuthor(IContact value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public InputStream getContent(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setContent(InputStream content){
		// TODO Auto-generated method stub
		
	}
	

}
