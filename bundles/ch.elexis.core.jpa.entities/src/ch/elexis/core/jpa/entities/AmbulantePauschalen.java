package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_AMBULANTEPAUSCHALEN")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "AmbulantePauschalen.typ.code", query = "SELECT tp FROM AmbulantePauschalen tp WHERE tp.deleted = false AND tp.typ = :typ AND tp.code = :code")
@NamedQuery(name = "AmbulantePauschalen.typ.code.validFrom", query = "SELECT tp FROM AmbulantePauschalen tp WHERE tp.deleted = false AND tp.typ = :typ AND tp.code = :code AND tp.validFrom = :validFrom")
public class AmbulantePauschalen extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "Ambulantepauschalen";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
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

	@Column(length = 8)
	private String tp;

	@Column(length = 255)
	private String digniQuali;

	@Column(length = 255)
	private String chapter;

	@Column(length = 255)
	private String text;

	@Column(length = 15)
	private String typ;

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

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getDigniQuali() {
		return digniQuali;
	}

	public void setDigniQuali(String digniQuali) {
		this.digniQuali = digniQuali;
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

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}
}
