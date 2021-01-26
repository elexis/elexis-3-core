package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "AUF")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "AUF.patient", query = "SELECT a FROM AUF a WHERE a.deleted = false AND a.patient = :patient ORDER BY a.dateFrom DESC")
public class AUF extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@ManyToOne
	@JoinColumn(name = "PatientID")
	protected Kontakt patient;

	@ManyToOne
	@JoinColumn(name = "FallID")
	protected Fall fall;

	@OneToOne
	@JoinColumn(name = "BriefID")
	protected Brief brief;
	
	@Convert(converter = IntegerStringConverter.class)
	protected int prozent;

	@Column(name = "DatumAUZ")
	protected LocalDate date;
	
	@Column(name = "DatumVon")
	protected LocalDate dateFrom;

	@Column(name = "DatumBis")
	protected LocalDate dateUntil;

	@Column(name = "Grund", length = 50)
	protected String reason;

	@Column(name = "AUFZusatz", length = 254)
	protected String note;
	
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
	public boolean isDeleted(){
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}
	
	public Fall getFall(){
		return fall;
	}
	
	public void setFall(Fall fall){
		this.fall = fall;
	}
	
	public Brief getBrief(){
		return brief;
	}
	
	public void setBrief(Brief brief){
		this.brief = brief;
	}
	
	public int getProzent(){
		return prozent;
	}
	
	public void setProzent(int prozent){
		this.prozent = prozent;
	}
	
	public LocalDate getDate(){
		return date;
	}
	
	public void setDate(LocalDate date){
		this.date = date;
	}
	
	public LocalDate getDateFrom(){
		return dateFrom;
	}
	
	public void setDateFrom(LocalDate dateFrom){
		this.dateFrom = dateFrom;
	}
	
	public LocalDate getDateUntil(){
		return dateUntil;
	}
	
	public void setDateUntil(LocalDate dateUntil){
		this.dateUntil = dateUntil;
	}
	
	public String getReason(){
		return reason;
	}
	
	public void setReason(String reason){
		this.reason = reason;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
