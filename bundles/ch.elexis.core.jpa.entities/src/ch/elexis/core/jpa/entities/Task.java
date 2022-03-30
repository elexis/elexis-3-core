package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.TimeMillisConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@EntityListeners(EntityWithIdListener.class)
@Table(name = "TASK")
@Cache(expiry = 15000)
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

	@Column
	protected Long createdAt;

	@Column
	protected Long runAt;

	@Column
	protected Long finishedAt;

	@JoinColumn(name = "descriptor")
	@ManyToOne(cascade = CascadeType.REFRESH)
	protected TaskDescriptor taskDescriptor;

	@Column
	@Basic(fetch = FetchType.LAZY)
	@Lob
	protected String runContext;

	@Column
	@Basic(fetch = FetchType.LAZY)
	@Lob
	protected String result;

	@Column(length = 64)
	protected String runner;

	@Column(name = "is_system")
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean system = false;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
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
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return this.state;
	}

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public int getTriggerEvent() {
		return triggerEvent;
	}

	public void setTriggerEvent(int triggerEvent) {
		this.triggerEvent = triggerEvent;
	}

	public TaskDescriptor getTaskDescriptor() {
		return taskDescriptor;
	}

	public void setTaskDescriptor(TaskDescriptor taskDescriptor) {
		this.taskDescriptor = taskDescriptor;
	}

	public String getRunContext() {
		return runContext;
	}

	public void setRunContext(String runContext) {
		this.runContext = runContext;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getRunner() {
		return runner;
	}

	public void setRunner(String runner) {
		this.runner = runner;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	@Transient
	public LocalDateTime getCreatedAtLocalDateTime() {
		return TimeMillisConverter.convertOptionalMillisToLocalDateTime(getCreatedAt());
	}

	public Long getRunAt() {
		return runAt;
	}

	public void setRunAt(Long runAt) {
		this.runAt = runAt;
	}

	@Transient
	public LocalDateTime getRunAtLocalDateTime() {
		return TimeMillisConverter.convertOptionalMillisToLocalDateTime(getRunAt());
	}

	public Long getFinishedAt() {
		return finishedAt;
	}

	@Transient
	public LocalDateTime getFinishedAtLocalDateTime() {
		return TimeMillisConverter.convertOptionalMillisToLocalDateTime(getFinishedAt());
	}

	public void setFinishedAt(Long finishedAt) {
		this.finishedAt = finishedAt;
	}

}
