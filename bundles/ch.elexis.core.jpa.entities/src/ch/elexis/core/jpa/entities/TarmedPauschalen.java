package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_TARMEDPAUSCHALEN")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "TarmedPauschalen.code", query = "SELECT tp FROM TarmedPauschalen tp WHERE tp.deleted = false AND tp.code = :code")
@NamedQuery(name = "TarmedPauschalen.code.validFrom", query = "SELECT tp FROM TarmedPauschalen tp WHERE tp.deleted = false AND tp.code = :code AND tp.validFrom = :validFrom")
public class TarmedPauschalen extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "Tarmedpauschalen";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 25, name = "code")
	private String code;

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validTo;

	@Column(length = 255)
	private String chapter;

	@Column(length = 255)
	private String text;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
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
