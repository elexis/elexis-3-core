package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK")
@EntityListeners(EntityWithIdListener.class)
public class ObservationLink extends AbstractEntityWithId implements EntityWithId {
	
	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
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
		return lastupdate != null ? lastupdate : 0L;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}

	@ManyToOne()
	@JoinColumn(name = "sourceid")
	private Observation source;

	@ManyToOne()
	@JoinColumn(name = "targetid")
	private Observation target;

	@Column(length = 8)
	private String type;

	@Column(length = 255)
	private String description;

	public Observation getSource(){
		return source;
	}

	public void setSource(Observation source){
		this.source = source;
	}

	public Observation getTarget(){
		return target;
	}

	public void setTarget(Observation target){
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
