package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "AT_MEDEVIT_ELEXIS_INBOX")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
public class InboxElement extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {
	
	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Override
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
	
	@OneToOne
	@JoinColumn(name = "patient")
	private Kontakt patient;
	
	@OneToOne
	@JoinColumn(name = "mandant")
	private Kontakt mandant;
	
	@Column(length = 1)
	private String state;
	
	@Column(length = 128)
	private String object;
	
	public Kontakt getPatient(){
		return patient;
	}
	
	public void setPatient(Kontakt patient){
		this.patient = patient;
	}
	
	public Kontakt getMandant(){
		return mandant;
	}
	
	public void setMandant(Kontakt mandant){
		this.mandant = mandant;
	}
	
	public String getState(){
		return state;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public String getObject(){
		return object;
	}
	
	public void setObject(String storeToString){
		this.object = storeToString;
	}
}
