package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "TARMED_DEFINITIONEN")
@EntityListeners(EntityWithIdListener.class)
@NamedQueries({
	@NamedQuery(name = "TarmedDefinitionen.spalte.kuerzel", query = "SELECT td FROM TarmedDefinitionen td WHERE td.spalte = :spalte AND td.kuerzel = :kuerzel"),
	@NamedQuery(name = "TarmedDefinitionen.spalte.titel", query = "SELECT td FROM TarmedDefinitionen td WHERE td.spalte = :spalte AND td.titel = :titel")
})
public class TarmedDefinitionen extends AbstractEntityWithId
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
	
	@Column(length = 20)
	private String spalte;
	
	@Column(length = 5)
	private String kuerzel;
	
	@Column(length = 255)
	private String titel;
	
	@Column(length = 3)
	private String law;
	
	public String getSpalte(){
		return spalte;
	}
	
	public void setSpalte(String spalte){
		this.spalte = spalte;
	}
	
	public String getKuerzel(){
		return kuerzel;
	}
	
	public void setKuerzel(String kuerzel){
		this.kuerzel = kuerzel;
	}
	
	public String getTitel(){
		return titel;
	}
	
	public void setTitel(String titel){
		this.titel = titel;
	}
	
	public String getLaw(){
		return law;
	}
	
	public void setLaw(String law){
		this.law = law;
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
		return lastupdate != null ? lastupdate : 0L;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
}
