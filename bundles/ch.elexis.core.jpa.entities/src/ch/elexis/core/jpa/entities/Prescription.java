package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.prescription.EntryType;

@Entity
@Table(name = "patient_artikel_joint")
@EntityListeners(EntityWithIdListener.class)
public class Prescription implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob
	protected byte[] extInfo;
	
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
	public byte[] getExtInfo(){
		return extInfo;
	}
	
	@Override
	public void setExtInfo(byte[] extInfo){
		this.extInfo = extInfo;
	}
	
	@Override
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
	
	@Override
	public int hashCode(){
		return EntityWithId.idHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj){
		return EntityWithId.idEquals(this, obj);
	}
}
