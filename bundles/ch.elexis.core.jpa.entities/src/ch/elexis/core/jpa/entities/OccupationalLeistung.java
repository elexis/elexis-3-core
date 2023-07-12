package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_OCCUPATIONAL")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "OccupationalLeistung.code", query = "SELECT ol FROM OccupationalLeistung ol WHERE ol.code = :code")
@NamedQuery(name = "OccupationalLeistung.code.validFrom", query = "SELECT ol FROM OccupationalLeistung ol WHERE ol.deleted = false AND ol.code = :code AND ol.validFrom = :validFrom")
public class OccupationalLeistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String FIXEDPRICE = "\n[FIXPRICE]";
	public static final String CODESYSTEM_NAME = "Arbeitsmedizinische Vorsorgeuntersuchungen";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validUntil;

	@Column(length = 8)
	private String tp;

	@Column(length = 16)
	private String code;

	@Column(length = 255)
	private String codeText;

	@Lob
	private String description;

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(LocalDate validUntil) {
		this.validUntil = validUntil;
	}

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCodeText() {
		return codeText;
	}

	public void setCodeText(String codeText) {
		this.codeText = codeText;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	public String getText() {
		return getCodeText();
	}

	public String getCodeSystemCode() {
		return "050";
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
