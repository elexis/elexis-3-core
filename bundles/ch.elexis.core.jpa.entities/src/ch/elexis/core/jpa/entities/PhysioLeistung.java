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
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_PHYSIO")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "PhysioLeistung.ziffer", query = "SELECT pl FROM PhysioLeistung pl WHERE pl.ziffer = :ziffer")
public class PhysioLeistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String FIXEDPRICE = "\n[FIXPRICE]";
	public static final String CODESYSTEM_NAME = "Physiotherapie";

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

	@Column(length = 6)
	private String ziffer;

	@Column(length = 255)
	private String titel;

	@Column(length = 3)
	private String law;

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

	public String getZiffer() {
		return ziffer;
	}

	public void setZiffer(String ziffer) {
		this.ziffer = ziffer;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
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

	public String getCode() {
		return getZiffer();
	}

	public String getText() {
		return getTitel();
	}

	public String getCodeSystemCode() {
		return "311";
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

	public String getLaw() {
		return law;
	}

	public void setLaw(String law) {
		this.law = law;
	}
}
