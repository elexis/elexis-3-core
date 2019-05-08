package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_MESSAGES")
@EntityListeners(EntityWithIdListener.class)
public class Message extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {
	
	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@JoinColumn(name = "ORIGIN")
	protected Kontakt origin;
	
	@JoinColumn(name = "DESTINATION")
	protected Kontakt destination;
	
	@Column
	protected LocalDateTime dateTime;
	
	@Column
	@Lob
	protected String msg;
	
	@Column
	@Lob
	protected String messageCodes;
	
	public Kontakt getOrigin(){
		return origin;
	}
	
	public void setOrigin(Kontakt origin){
		this.origin = origin;
	}
	
	public Kontakt getDestination(){
		return destination;
	}
	
	public void setDestination(Kontakt destination){
		this.destination = destination;
	}
	
	public LocalDateTime getDateTime(){
		return dateTime;
	}
	
	public void setDateTime(LocalDateTime dateTime){
		this.dateTime = dateTime;
	}
	
	public String getMsg(){
		return msg;
	}
	
	public void setMsg(String msg){
		this.msg = msg;
	}
	
	@Override
	public boolean isDeleted(){
		return deleted;
	}
	
	public String getMessageCodes(){
		return messageCodes;
	}
	
	public void setMessageCodes(String messageCodes){
		this.messageCodes = messageCodes;
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
	
}
