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
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "net_medshare_percentile_data")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class PercentileData extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Column(length = 8)
	private LocalDate date;
	
	@ManyToOne
	@JoinColumn(name = "PATIENT_ID")
	private Kontakt patient;
	
	@Column(length = 8)
	private String length;

	@Column(length = 8)
	private String weight;
	
	@Column(length = 8)
	private String head;
	
	@Column(length = 8)
	private String bmi;

	@Column(length = 8)
	private String growthrate;
	
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
	
	public LocalDate getDate(){
		return date;
	}
	
	public void setDate(LocalDate date){
		this.date = date;
	}
	
	public Kontakt getPatient(){
		return patient;
	}
	
	public void setPatient(Kontakt patient){
		this.patient = patient;
	}
	
	public String getLength(){
		return length;
	}
	
	public void setLength(String length){
		this.length = length;
	}
	
	public String getWeight(){
		return weight;
	}
	
	public void setWeight(String weight){
		this.weight = weight;
	}
	
	public String getHead(){
		return head;
	}
	
	public void setHead(String head){
		this.head = head;
	}
	
	public String getBmi(){
		return bmi;
	}
	
	public void setBmi(String bmi){
		this.bmi = bmi;
	}
	
	public String getGrowthrate(){
		return growthrate;
	}
	
	public void setGrowthrate(String growthrate){
		this.growthrate = growthrate;
	}
}
