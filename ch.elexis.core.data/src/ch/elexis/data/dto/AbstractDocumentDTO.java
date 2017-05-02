package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IHistory;
import ch.elexis.core.types.DocumentStatus;
import ch.rgw.tools.MimeTool;

public abstract class AbstractDocumentDTO implements IDocument {
	private String patientId;
	private String authorId;
	private String title = "";
	private String description;
	private DocumentStatus status = DocumentStatus.NEW;
	private Date created;
	private Date lastchanged;
	private String mimeType;
	private ICategory category;
	private String keywords = "";
	private List<IHistory> history = new ArrayList<>();
	private String label;
	private String id;
	private String storeId;
	private String extension;
	
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
	public void setKeywords(String value){
		this.keywords = value;
	}
	
	@Override
	public String getKeywords(){
		return keywords;
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
	
	@Override
	public void setExtension(String value){
		this.extension = value;
		
	}
	
	@Override
	public String getExtension(){
		return extension;
	}
	
	public String evaluateExtension(String input){
		String ext = MimeTool.getExtension(input);
		if (StringUtils.isEmpty(ext)) {
			ext = FilenameUtils.getExtension(input);
			if (StringUtils.isEmpty(ext)) {
				ext = input;
			}
		}
		return ext;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((patientId == null) ? 0 : patientId.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDocumentDTO other = (AbstractDocumentDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (patientId == null) {
			if (other.patientId != null)
				return false;
		} else if (!patientId.equals(other.patientId))
			return false;
		return true;
	}
}
