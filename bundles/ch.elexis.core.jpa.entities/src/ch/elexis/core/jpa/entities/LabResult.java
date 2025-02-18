package ch.elexis.core.jpa.entities;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.converter.PathologicDescriptionConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "laborwerte")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "LabResult.patient.item", query = "SELECT lr FROM LabResult lr WHERE lr.deleted = false AND lr.patient = :patient AND lr.item = :item")
@NamedQuery(name = "LabResult.patient.itemtype.includesDeleted", query = "SELECT lr FROM LabResult lr WHERE lr.patient = :patient AND lr.item.typ = :itemtype")
public class LabResult extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Lob
	protected byte[] extInfo;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PatientID")
	private Kontakt patient;

	@Column(name = "datum", length = 8)
	private LocalDate date;

	@Column(length = 6)
	private String zeit;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ItemID")
	private LabItem item;

	@Column(length = 255, name = "resultat")
	private String result;

	@Convert(converter = IntegerStringConverter.class)
	private int flags;

	@Lob
	@Column(name = "Kommentar")
	private String comment;

	@Column(length = 255)
	private String unit;

	@Column(length = 24)
	private LocalDateTime analysetime;

	@Column(length = 24)
	private LocalDateTime observationtime;

	@Column(length = 24)
	private LocalDateTime transmissiontime;

	@Column(length = 255)
	private String refMale;

	@Column(length = 255)
	private String refFemale;

	@OneToOne
	@JoinColumn(name = "originId")
	private Kontakt origin;

	@Column(length = 128, name = "pathoDesc")
	@Convert(converter = PathologicDescriptionConverter.class)
	private PathologicDescription pathologicDescription = new PathologicDescription(Description.UNKNOWN);

	@Transient
	public boolean isFlagged(final int flag) {
		return (getFlags() & flag) != 0;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getZeit() {
		return zeit;
	}

	public void setZeit(String zeit) {
		this.zeit = zeit;
	}

	public LabItem getItem() {
		return item;
	}

	public void setItem(LabItem item) {
		this.item = item;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRefMale() {
		return refMale;
	}

	public void setRefMale(String refMale) {
		this.refMale = refMale;
	}

	public String getRefFemale() {
		return refFemale;
	}

	public void setRefFemale(String refFemale) {
		this.refFemale = refFemale;
	}

	public Kontakt getOrigin() {
		return origin;
	}

	public void setOrigin(Kontakt origin) {
		this.origin = origin;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public LocalDate getDate() {
		return this.date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDateTime getAnalysetime() {
		return analysetime;
	}

	public void setAnalysetime(LocalDateTime analysetime) {
		this.analysetime = analysetime;
	}

	public LocalDateTime getObservationtime() {
		return observationtime;
	}

	public void setObservationtime(LocalDateTime observationtime) {
		this.observationtime = observationtime;
	}

	public LocalDateTime getTransmissiontime() {
		return transmissiontime;
	}

	public void setTransmissiontime(LocalDateTime transmissiontime) {
		this.transmissiontime = transmissiontime;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public PathologicDescription getPathologicDescription() {
		return pathologicDescription;
	}

	public void setPathologicDescription(PathologicDescription pathologicDescription) {
		this.pathologicDescription = pathologicDescription;
	}

	@Override
	public byte[] getExtInfo() {
		return extInfo;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
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
