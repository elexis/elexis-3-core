package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@EntityListeners(EntityWithIdListener.class)
@Table(name = "TASKDESCRIPTOR")
public class TaskDescriptor extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 32)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean active = false;

	@Column(length = 64)
	protected String referenceId;

	@Column(length = 25)
	protected String owner_id;

	@Column
	protected int notificationType = 0;

	@Column(length = 64)
	protected String runnableId;

	@Column
	@Lob
	protected String runContext;

	@Column
	protected int triggerType = 0;

	@Column
	@Lob
	protected String triggerParameters;

	@Column(length = 64)
	protected String runner;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean singleton = false;

	@Column(name = "is_system")
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean system = false;

	public boolean isSingleton() {
		return singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public int getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(int triggerType) {
		this.triggerType = triggerType;
	}

	public String getRunContext() {
		return runContext;
	}

	public String getTriggerParameters() {
		return triggerParameters;
	}

	public void setTriggerParameters(String triggerParameters) {
		this.triggerParameters = triggerParameters;
	}

	public void setRunContext(String runContext) {
		this.runContext = runContext;
	}

	public String getRunner() {
		return runner;
	}

	public void setRunner(String runner) {
		this.runner = runner;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRunnableId() {
		return runnableId;
	}

	public void setRunnableId(String runnableId) {
		this.runnableId = runnableId;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getOwner() {
		return owner_id;
	}

	public void setOwner(String owner) {
		this.owner_id = owner;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

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

	public int getNotificationType() {
		return notificationType;

	}

	public void setNotificationType(int notificationType) {
		this.notificationType = notificationType;
	}

}
