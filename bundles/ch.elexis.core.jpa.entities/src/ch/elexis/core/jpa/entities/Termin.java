package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
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
@Table(name = "AGNTERMINE")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "Termin.dayfrom.dayto", query = "SELECT te FROM Termin te WHERE te.deleted = false AND te.tag >= :dayfrom AND te.tag <= :dayto")
public class Termin extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 80)
	private String patId;

	@Column(length = 25)
	private String bereich;

	@Column(length = 8)
	private LocalDate tag;

	@Column(length = 4)
	private String beginn;

	@Column(length = 4)
	private String dauer;

	@Lob()
	private String grund;

	@Column(length = 50)
	private String terminTyp;

	@Column(length = 50)
	private String terminStatus;

	@Column(length = 25)
	private String erstelltVon;

	@Column(length = 10)
	private String angelegt;

	@Column(length = 10)
	private String lastedit;

	@Column
	private int palmId;

	@Column(length = 10)
	private String flags;

	@Lob()
	private String extension;

	@Column(length = 50)
	private String linkgroup;

	@Lob()
	private String statusHistory;

	@Convert(converter = IntegerStringConverter.class)
	private int priority = 0;

	@Convert(converter = IntegerStringConverter.class)
	private int caseType = 0;

	@Convert(converter = IntegerStringConverter.class)
	private int insuranceType = 0;

	@Convert(converter = IntegerStringConverter.class)
	private int treatmentReason = 0;

	public String getPatId() {
		return patId;
	}

	public void setPatId(String patId) {
		this.patId = patId;
	}

	public String getBereich() {
		return bereich;
	}

	public void setBereich(String bereich) {
		this.bereich = bereich;
	}

	public LocalDate getTag() {
		return tag;
	}

	public void setTag(LocalDate tag) {
		this.tag = tag;
	}

	public String getBeginn() {
		return beginn;
	}

	public void setBeginn(String beginn) {
		this.beginn = beginn;
	}

	public String getDauer() {
		return dauer;
	}

	public void setDauer(String dauer) {
		this.dauer = dauer;
	}

	public String getGrund() {
		return grund;
	}

	public void setGrund(String grund) {
		this.grund = grund;
	}

	public String getTerminTyp() {
		return terminTyp;
	}

	public void setTerminTyp(String terminTyp) {
		this.terminTyp = terminTyp;
	}

	public String getTerminStatus() {
		return terminStatus;
	}

	public void setTerminStatus(String terminStatus) {
		this.terminStatus = terminStatus;
	}

	public String getErstelltVon() {
		return erstelltVon;
	}

	public void setErstelltVon(String erstelltVon) {
		this.erstelltVon = erstelltVon;
	}

	public String getAngelegt() {
		return angelegt;
	}

	public void setAngelegt(String angelegt) {
		this.angelegt = angelegt;
	}

	public String getLastedit() {
		return lastedit;
	}

	public void setLastedit(String lastedit) {
		this.lastedit = lastedit;
	}

	public int getPalmId() {
		return palmId;
	}

	public void setPalmId(int palmId) {
		this.palmId = palmId;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getLinkgroup() {
		return linkgroup;
	}

	public void setLinkgroup(String linkgroup) {
		this.linkgroup = linkgroup;
	}

	public String getStatusHistory() {
		return statusHistory;
	}

	public void setStatusHistory(String statusHistory) {
		this.statusHistory = statusHistory;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getTreatmentReason() {
		return treatmentReason;
	}

	public void setTreatmentReason(int treatmentReason) {
		this.treatmentReason = treatmentReason;
	}

	public int getInsuranceType() {
		return insuranceType;
	}

	public void setInsuranceType(int insuranceType) {
		this.insuranceType = insuranceType;
	}

	public int getCaseType() {
		return caseType;
	}

	public void setCaseType(int caseType) {
		this.caseType = caseType;
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
