package ch.elexis.core.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Brief;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.types.DocumentStatus;

import ch.rgw.tools.StringTool;
public class DocumentLetter extends AbstractIdDeleteModelAdapter<Brief>
		implements IdentifiableWithXid, IDocumentLetter {
	
	private ICategory category;
	private DocumentStatus status;
	private String storeId = StringTool.leer;
	private List<IHistory> history;
	private String keywords;
	
	public DocumentLetter(Brief entity){
		super(entity);
	}
	
	@Override
	public String getTitle(){
		return getEntity().getSubject();
	}
	
	@Override
	public void setTitle(String value){
		getEntityMarkDirty().setSubject(value);
	}
	
	@Override
	public String getDescription(){
		return getEntity().getNote();
	}
	
	@Override
	public void setDescription(String value){
		getEntityMarkDirty().setNote(value);
	}
	
	@Override
	public DocumentStatus getStatus(){
		if (status == null) {
			if (getEntity().getRecipient() != null) {
				status = DocumentStatus.SENT;
			} else {
				status = DocumentStatus.NEW;
			}
		}
		return status;
	}
	
	@Override
	public void setStatus(DocumentStatus value){
		this.status = value;
	}
	
	@Override
	public Date getCreated(){
		return toDate(getEntity().getCreationDate());
	}
	
	@Override
	public void setCreated(Date value){
		getEntityMarkDirty().setCreationDate(toLocalDate(value));
	}
	
	@Override
	public Date getLastchanged(){
		return new Date(getEntity().getLastupdate().longValue());
	}
	
	@Override
	public void setLastchanged(Date value){
		getEntityMarkDirty().setLastupdate(value.getTime());
	}
	
	@Override
	public String getMimeType(){
		return getEntity().getMimetype();
	}
	
	@Override
	public void setMimeType(String value){
		getEntityMarkDirty().setMimetype(value);
	}
	
	@Override
	public ICategory getCategory(){
		if (this.category == null && getEntity().getTyp() != null) {
			this.category = new TransientCategory(getEntity().getTyp());
		}
		return this.category;
	}
	
	@Override
	public void setCategory(ICategory value){
		this.category = value;
		getEntityMarkDirty().setTyp(value.getName());
	}
	
	@Override
	public List<IHistory> getHistory(){
		if (history == null) {
			history = new ArrayList<>();
			if (getEntity().getRecipient() != null) {
				history.add(new TransientHistory(getCreated(), DocumentStatus.SENT,
					ModelUtil.getPersonalia(getEntity().getRecipient())));
			}
		}
		return history;
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
	public String getExtension(){
		return ModelUtil.evaluateFileExtension(getEntity().getMimetype());
	}
	

	@Override
	public void setExtension(String value){
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getKeywords(){
		return this.keywords;
	}
	
	@Override
	public void setKeywords(String value){
		this.keywords = value;
	}
	
	@Override
	public IPatient getPatient(){
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
	}
	
	@Override
	public void setPatient(IPatient value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setPatient((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setPatient(null);
		}
	}
	
	@Override
	public IContact getAuthor(){
		return ModelUtil.getAdapter(getEntity().getSender(), IContact.class);
	}
	
	@Override
	public void setAuthor(IContact value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setSender((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setSender(null);
		}
	}
	
	@Override
	public InputStream getContent(){
		// test for file content first
		if (ModelUtil.isExternFile() && getPatient() != null) {
			Optional<File> file = ModelUtil.getExternFile(this);
			if (file.isPresent()) {
				try {
					return new FileInputStream(file.get());
				} catch (FileNotFoundException e) {
					LoggerFactory.getLogger(getClass()).error("Error getting document content", e);
				}
			}
		}
		// fallback to Heap content
		if (getEntity().getContent() != null) {
			return new ByteArrayInputStream(getEntity().getContent().getInhalt());
		}
		return null;
	}
	
	@Override
	public void setContent(InputStream content){
		// set file content if configured 
		if (ModelUtil.isExternFile() && getPatient() != null) {
			Optional<File> file = ModelUtil.getExternFile(this);
			if (!file.isPresent()) {
				file = ModelUtil.createExternFile(this);
			}
			if (file.isPresent()) {
				try (FileOutputStream fileOutput = new FileOutputStream(file.get())) {
					IOUtils.copy(content, fileOutput);
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Error setting document content", e);
				}
			}
		}
		
		if (getEntity().getContent() != null) {
			try {
				getEntity().getContent().setInhalt(IOUtils.toByteArray(content));
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Error setting document content", e);
			} finally {
				IOUtils.closeQuietly(content);
			}
		}
	}
}
