package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "net_medshare_percentile_patient")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class PercentilePatient extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@ManyToOne
	@JoinColumn(name = "PATIENT_ID")
	private Kontakt patient;

	@Column(length = 8, name = "AGE_CORRECTION")
	private String ageCorrection;

	@Column(length = 8, name = "LEN_FATHER")
	private String lengthFather;

	@Column(length = 8, name = "LEN_MOTHER")
	private String lengthMother;

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

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getAgeCorrection() {
		return ageCorrection;
	}

	public void setAgeCorrection(String ageCorrection) {
		this.ageCorrection = ageCorrection;
	}

	public String getLengthFather() {
		return lengthFather;
	}

	public void setLengthFather(String lengthFather) {
		this.lengthFather = lengthFather;
	}

	public String getLengthMother() {
		return lengthMother;
	}

	public void setLengthMother(String lengthMother) {
		this.lengthMother = lengthMother;
	}
}
