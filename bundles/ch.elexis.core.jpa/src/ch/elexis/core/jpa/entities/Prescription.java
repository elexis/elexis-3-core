package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.model.prescription.EntryType;

@Entity
@Table(name = "patient_artikel_joint")
public class Prescription extends AbstractDBObjectIdDeletedExtInfo {

	@Column(length = 3)
	private String anzahl;

	@Column(length = 255)
	private String artikel;

	@Column(length = 255)
	private String bemerkung;

	@Column(length = 24)
	private LocalDateTime dateFrom;

	@Column(length = 24)
	private LocalDateTime dateUntil;

	@Column(length = 255)
	private String dosis;

	@Column(length = 2, name = "prescType")
	private String prescriptionType;

	@OneToOne
	@JoinColumn(name = "patientID")
	private Kontakt patient;

	@Column(length = 25)
	private String rezeptID;

	@Transient
	public EntryType getEntryType() {
		String prescTypeString = getPrescriptionType();
		int typeNum = -1;
		if (prescTypeString != null && !prescTypeString.isEmpty()) {
			try {
				typeNum = Integer.parseInt(prescTypeString);
			} catch (NumberFormatException e) {
				// ignore and return -1
			}
		}

		if (typeNum != -1) {
			return EntryType.byNumeric(typeNum);
		}

		String rezeptId = getRezeptID();
		if (rezeptId != null && !rezeptId.isEmpty()) {
			// this is necessary due to a past impl. where self dispensed was
			// not set as entry type
			if (rezeptId.equals("Direktabgabe")) {
				setPrescriptionType(Integer.toString(EntryType.SELF_DISPENSED.numericValue()));
				setRezeptID("");
				return EntryType.SELF_DISPENSED;
			}
			return EntryType.RECIPE;
		}

		return EntryType.FIXED_MEDICATION;
	}

	public String getAnzahl() {
		return anzahl;
	}

	public void setAnzahl(String anzahl) {
		this.anzahl = anzahl;
	}

	public String getArtikel(){
		return artikel;
	}

	public void setArtikel(String artikel){
		this.artikel = artikel;
	}

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDateTime getDateUntil() {
		return dateUntil;
	}

	public void setDateUntil(LocalDateTime dateUntil) {
		this.dateUntil = dateUntil;
	}

	public String getPrescriptionType() {
		return prescriptionType;
	}

	public void setPrescriptionType(String prescriptionType) {
		this.prescriptionType = prescriptionType;
	}

	public String getDosis() {
		return dosis;
	}

	public void setDosis(String dosis) {
		this.dosis = dosis;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getRezeptID() {
		return rezeptID;
	}

	public void setRezeptID(String rezeptID) {
		this.rezeptID = rezeptID;
	}

	@Override
	public String getLabel() {
		return (getArtikel() != null ? getArtikel() : "FEHLER") + " " + getDosis();
	}
}
