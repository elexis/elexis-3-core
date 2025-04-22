package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "net_medshare_percentile_checklist_form")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class PercentileChecklistForm extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 255)
	private LocalDate formTitle;

	@Column(length = 11)
	private String fromAge;

	@Column(length = 11)
	private String toAge;

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

	public LocalDate getFormTitle() {
		return formTitle;
	}

	public void setFormTitle(LocalDate formTitle) {
		this.formTitle = formTitle;
	}

	public String getFromAge() {
		return fromAge;
	}

	public void setFromAge(String fromAge) {
		this.fromAge = fromAge;
	}

	public String getToAge() {
		return toAge;
	}

	public void setToAge(String toAge) {
		this.toAge = toAge;
	}
}
