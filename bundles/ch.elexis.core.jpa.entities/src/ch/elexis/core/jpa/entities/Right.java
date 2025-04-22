package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "RIGHT_")
@EntityListeners(EntityWithIdListener.class)
public class Right extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	@Column(name = "LOG_EXECUTION")
	protected boolean logExecution;

	@Column(length = 255)
	protected String name;

	@OneToOne
	@JoinColumn(name = "PARENTID")
	protected Right parent;

	@Column(length = 255, name = "I18N_NAME")
	protected String i18nName;

	public boolean isLogExecution() {
		return logExecution;
	}

	public void setLogExecution(boolean logExecution) {
		this.logExecution = logExecution;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Right getParent() {
		return parent;
	}

	public void setParent(Right parent) {
		this.parent = parent;
	}

	public String getI18nName() {
		return i18nName;
	}

	public void setI18nName(String i18nName) {
		this.i18nName = i18nName;
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
}
