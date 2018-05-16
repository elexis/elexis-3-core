package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;

import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;

@Entity
@Table(name = "laborwerte")
public class LabResult extends AbstractDBObjectIdDeletedExtInfo {

	@OneToOne
	@JoinColumn(name = "PatientID")
	private Kontakt patient;

	@Column(name = "datum", length = 8)
	private LocalDate date;

	@Column(length = 6)
	private String zeit;

	@OneToOne
	@JoinColumn(name = "ItemID")
	private LabItem item;

	@Column(length = 255, name = "resultat")
	private String result;

	@Convert(value = "IntegerStringConverter")
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
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(getItem().getLabel()).append(", ").append(getAnalysetime()).append(": ") //$NON-NLS-1$ //$NON-NLS-2$
				.append(getResult());
		return sb.toString();
	}

}
