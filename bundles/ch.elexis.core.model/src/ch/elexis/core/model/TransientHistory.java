package ch.elexis.core.model;

import java.util.Date;

import ch.elexis.core.types.DocumentStatus;

public class TransientHistory implements IHistory {
	
	private DocumentStatus status;
	private Date date;
	private String description;
	
	public TransientHistory(Date date, DocumentStatus status, String description){
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
