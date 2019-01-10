package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "rezepte")
@EntityListeners(EntityWithIdListener.class)
public class Rezept extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {
	
	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@OneToOne
	@JoinColumn(name = "patientid")
	private Kontakt patient;
	
	@OneToOne
	@JoinColumn(name = "mandantid")
	private Kontakt mandant;
	
	@OneToOne
	@JoinColumn(name = "briefid")
	private Brief brief;
	
	@Column(length = 8)
	private LocalDate datum;
	
	@OneToMany
	@JoinColumn(name = "rezeptID")
	private List<Prescription> prescriptions = new ArrayList<>();
	
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
	
	public Kontakt getPatient(){
		return patient;
	}
	
	public void setPatient(Kontakt patient){
		this.patient = patient;
	}
	
	public Kontakt getMandant(){
		return mandant;
	}
	
	public void setMandant(Kontakt mandant){
		this.mandant = mandant;
	}
	
	public Brief getBrief(){
		return brief;
	}
	
	public void setBrief(Brief brief){
		this.brief = brief;
	}
	
	public LocalDate getDatum(){
		return datum;
	}
	
	public void setDatum(LocalDate datum){
		this.datum = datum;
	}
	
	public List<Prescription> getPrescriptions(){
		return prescriptions;
	}
}
