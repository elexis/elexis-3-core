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
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_PSYCHO")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "PsychoLeistung.code", query = "SELECT pl FROM PsychoLeistung pl WHERE pl.deleted = false AND pl.code = :code")
@NamedQuery(name = "PsychoLeistung.code.validFrom", query = "SELECT pl FROM PsychoLeistung pl WHERE pl.deleted = false AND pl.code = :code AND pl.validFrom = :validFrom")
public class PsychoLeistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "Psychotherapie";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
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

	@Lob
	private String limitations;

	@Lob
	private String exclusions;

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
		return "581";
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

	public void setLimitations(String limitations) {
		this.limitations = limitations;
	}

	public String getLimitations() {
		return limitations;
	}

	public void setExclusions(String exclusions) {
		this.exclusions = exclusions;
	}

	public String getExclusions() {
		return exclusions;
	}
}
