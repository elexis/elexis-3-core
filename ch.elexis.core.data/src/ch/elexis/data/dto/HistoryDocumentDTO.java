package ch.elexis.data.dto;

import java.util.Date;

import ch.elexis.core.model.IHistory;
import ch.elexis.core.types.DocumentStatus;

public class HistoryDocumentDTO implements IHistory {
	private Date date;
	private DocumentStatus status;
	private String description;
	
	public HistoryDocumentDTO(Date date, DocumentStatus status, String description){
		super();
		this.date = date;
		this.status = status;
		this.description = description;
	}
	
	@Override
	public Date getDate(){
		return date;
	}
	
	@Override
	public void setDate(Date value){
		this.date = value;
	}
	
	@Override
	public DocumentStatus getStatus(){
		return status;
	}
	
	@Override
	public void setStatus(DocumentStatus value){
		this.status = value;
	}
	
	@Override
	public String getDescription(){
		return description;
	}
	
	@Override
	public void setDescription(String value){
		this.description = value;
	}
	
}
