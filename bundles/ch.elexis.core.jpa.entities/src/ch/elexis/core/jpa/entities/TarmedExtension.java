package ch.elexis.core.jpa.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "TARMED_EXTENSION")
@EntityListeners(EntityWithIdListener.class)
public class TarmedExtension implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@Column(length = 14)
	private String code;

	@Basic(fetch = FetchType.LAZY)
	@Lob
	private byte[] limits;

	@Lob
	private String med_interpret;

	@Lob
	private String tech_interpret;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public byte[] getLimits(){
		return limits;
	}

	public void setLimits(byte[] limits){
		this.limits = limits;
	}

	public String getMed_interpret() {
		return med_interpret;
	}

	public void setMed_interpret(String med_interpret) {
		this.med_interpret = med_interpret;
	}

	public String getTech_interpret() {
		return tech_interpret;
	}

	public void setTech_interpret(String tech_interpret) {
		this.tech_interpret = tech_interpret;
	}
	
	@Override
	public String getId(){
		return code;
	}
	
	@Override
	public void setId(String id){
		this.code = id;
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
