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

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.converter.PrescriptionEntryTypeConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.prescription.EntryType;

@Entity
@Table(name = "patient_artikel_joint")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class Prescription extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

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
	@Convert(converter = PrescriptionEntryTypeConverter.class)
	private EntryType entryType;

	@OneToOne
	@JoinColumn(name = "patientID")
	private Kontakt patient;

	@OneToOne
	@JoinColumn(name = "prescriptor")
	private Kontakt prescriptor;
	
	@Column(length = 25)
	private String rezeptID;

	@Column(length = 3)
	@Convert(converter = IntegerStringConverter.class)
	private int sortorder;
	
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

	public EntryType getEntryType(){
		if(entryType == EntryType.UNKNOWN) {
			String rezeptId = getRezeptID();
			if (rezeptId != null && !rezeptId.isEmpty()) {
				// this is necessary due to a past impl. where self dispensed was
				// not set as entry type
				if (rezeptId.equals("Direktabgabe")) {
					setEntryType(EntryType.SELF_DISPENSED);
					setRezeptID("");
					return EntryType.SELF_DISPENSED;
				}
				setEntryType(EntryType.RECIPE);
				return EntryType.RECIPE;
			}
			setEntryType(EntryType.FIXED_MEDICATION);
			return EntryType.FIXED_MEDICATION;
		}
		return entryType;
	}

	public void setEntryType(EntryType entryType){
		this.entryType = entryType;
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

	public Kontakt getPrescriptor(){
		return prescriptor;
	}
	
	public void setPrescriptor(Kontakt patient){
		this.prescriptor = patient;
	}
	
	public String getRezeptID(){
		return rezeptID;
	}

	public void setRezeptID(String rezeptID) {
		this.rezeptID = rezeptID;
	}

	public int getSortorder(){
		return sortorder;
	}
	
	public void setSortorder(int sortorder){
		this.sortorder = sortorder;
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
}
