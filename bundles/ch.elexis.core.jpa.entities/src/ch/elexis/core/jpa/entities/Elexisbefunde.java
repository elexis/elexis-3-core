package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "elexisbefunde")
@EntityListeners(EntityWithIdListener.class)
public class Elexisbefunde extends AbstractEntityWithId
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
	
	@OneToOne
	@JoinColumn(name = "PatientID")
	private Kontakt patient;

	@Column(length = 80)
	private String name;

	@Column(length = 8)
	private LocalDate datum;

	@Lob
	protected byte[] befunde;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Kontakt getPatient(){
		return patient;
	}
	
	public void setPatient(Kontakt patient){
		this.patient = patient;
	}
	
	public LocalDate getDatum(){
		return datum;
	}
	
	public void setDatum(LocalDate datum){
		this.datum = datum;
	}
	
	public byte[] getBefunde(){
		return befunde;
	}
	
	public void setBefunde(byte[] befunde){
		this.befunde = befunde;
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
