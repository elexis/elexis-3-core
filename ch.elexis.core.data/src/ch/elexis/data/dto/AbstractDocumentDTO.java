package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.model.ITag;
import ch.elexis.core.types.DocumentStatus;

public abstract class AbstractDocumentDTO implements IDocument {
	private String patientId;
	private String authorId;
	private String title;
	private String description;
	private DocumentStatus status = DocumentStatus.NEW;
	private Date created;
	private Date lastchanged;
	private String mimeType;
	private ICategory category;
	private List<ITag> tags = new ArrayList<>();
	private List<IHistory> history = new ArrayList<>();
	private String label;
	private String id;
	private String storeId;
	
	@Override
	public String getPatientId(){
		return patientId;
	}
	
	@Override
	public void setPatientId(String patientId){
		this.patientId = patientId;
	}
	
	@Override
	public String getAuthorId(){
		return authorId;
	}
	
	@Override
	public void setAuthorId(String authorId){
		this.authorId = authorId;
	}
	
	@Override
	public String getTitle(){
		return title;
	}
	
	@Override
	public void setTitle(String title){
		this.title = title;
	}
	
	@Override
	public String getDescription(){
		return description;
	}
	
	@Override
	public void setDescription(String description){
		this.description = description;
	}
	
	@Override
	public DocumentStatus getStatus(){
		return status;
	}
	
	@Override
	public void setStatus(DocumentStatus status){
		this.status = status;
	}
	
	@Override
	public Date getCreated(){
		return created;
	}
	
	@Override
	public void setCreated(Date created){
		this.created = created;
	}
	
	@Override
	public Date getLastchanged(){
		return lastchanged;
	}
	
	@Override
	public void setLastchanged(Date lastchanged){
		this.lastchanged = lastchanged;
	}
	
	@Override
	public String getMimeType(){
		return mimeType;
	}
	
	@Override
	public void setMimeType(String mimeType){
		this.mimeType = mimeType;
	}
	
	@Override
	public ICategory getCategory(){
		return category;
	}
	
	@Override
	public void setCategory(ICategory category){
		this.category = category;
	}
	
	@Override
	public List<ITag> getTags(){
		return tags;
	}
	
	public void setTags(List<ITag> tags){
		this.tags = tags;
	}
	
	@Override
	public List<IHistory> getHistory(){
		return history;
	}
	
	public void setHistory(List<IHistory> history){
		this.history = history;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	@Override
	public String getLabel(){
		return label;
	}
	
	@Override
	public String getStoreId(){
		return storeId;
	}
	
	@Override
	public void setStoreId(String value){
		this.storeId = value;
	}
}
