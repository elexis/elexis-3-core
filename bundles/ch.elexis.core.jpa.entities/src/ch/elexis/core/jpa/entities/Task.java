package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@EntityListeners(EntityWithIdListener.class)
@Table(name = "TASK")
public class Task extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {
	
	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 32)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Column
	protected int state = 0;
	
	@Column
	protected int triggerEvent = 0;
	
	@Column(length = 32)
	protected String descriptorId;
	
	@Column
	@Lob
	protected String runContext;
	
	@Column
	@Lob
	protected String result;
	
	@Column(length = 64)
	protected String runner;
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public boolean isDeleted(){
		return deleted;
	}
	
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	public Long getLastupdate(){
		return lastupdate;
	}
	
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public int getState(){
		return this.state;
	}
	
	public int getTriggerEvent(){
		return triggerEvent;
	}
	
	public void setTriggerEvent(int triggerEvent){
		this.triggerEvent = triggerEvent;
	}
	
	public String getDescriptorId(){
		return descriptorId;
	}
	
	public void setDescriptorId(String descriptorId){
		this.descriptorId = descriptorId;
	}
	
	public String getRunContext(){
		return runContext;
	}
	
	public void setRunContext(String runContext){
		this.runContext = runContext;
	}
	
	public String getResult(){
		return result;
	}
	
	public void setResult(String result){
		this.result = result;
	}
	
	public String getRunner(){
		return runner;
	}
	
	public void setRunner(String runner){
		this.runner = runner;
	}
	
}
