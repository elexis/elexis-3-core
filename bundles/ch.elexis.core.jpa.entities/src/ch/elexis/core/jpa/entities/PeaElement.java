package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "ch_medelexis_pea")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Pea.openRegistrations", query = "SELECT pea FROM PeaElement pea WHERE pea.deleted = false AND pea.type = '1' AND pea.localState = '0'")
@NamedQuery(name = "Pea.publishedPublicQuestionnaire", query = "SELECT pea FROM PeaElement pea WHERE pea.deleted = false AND pea.type = '2' AND pea.localState = '5' AND pea.handler = :handlerName")
@NamedQuery(name = "Pea.publishedTargetedQuestionnaire", query = "SELECT pea FROM PeaElement pea WHERE pea.deleted = false AND pea.type = '2' AND pea.localState = '5' AND pea.handler = :handlerName AND pea.subjectId = :subjectId")
public class PeaElement extends AbstractEntityWithId implements EntityWithDeleted {

	@Id
	@Column(unique = true, nullable = false, length = 36)
	private String id;

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column
	protected int type = 0;

	@Column(length = 24)
	protected LocalDateTime creationDate;

	@Column(unique = true, nullable = true, length = 36)
	protected String referenceId;

	@Column(length = 64, nullable = false)
	protected String handler;

	@Column(length = 25)
	protected String subjectId;

	@Column
	protected int localState = 0;

	@Column
	@Basic(fetch = FetchType.LAZY)
	@Lob
	protected String data;

	@Transient
	public boolean isTargeted() {
		return subjectId != null && !subjectId.isEmpty();
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

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public int getLocalState() {
		return localState;
	}

	public void setLocalState(int localState) {
		this.localState = localState;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
}
