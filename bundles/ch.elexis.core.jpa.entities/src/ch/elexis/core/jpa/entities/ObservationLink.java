package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
public class ObservationLink extends AbstractEntityWithId implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
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

	public Observation getSource() {
		return source;
	}

	public void setSource(Observation source) {
		this.source = source;
	}

	public Observation getTarget() {
		return target;
	}

	public void setTarget(Observation target) {
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
