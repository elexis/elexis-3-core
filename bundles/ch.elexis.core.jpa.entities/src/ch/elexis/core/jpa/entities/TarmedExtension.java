package ch.elexis.core.jpa.entities;

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.ElexisExtInfoMapConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "TARMED_EXTENSION")
@EntityListeners(EntityWithIdListener.class)
public class TarmedExtension implements EntityWithId {

	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	@Id
	@Column(length = 14)
	private String code;

	@Basic(fetch = FetchType.LAZY)
	@Convert(converter = ElexisExtInfoMapConverter.class)
	private Map<String, String> limits = new Hashtable<String, String>();

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

	public Map<String, String> getLimits() {
		return limits;
	}

	public void setLimits(Map<String, String> limits) {
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
	public BigInteger getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(BigInteger lastupdate){
		this.lastupdate = lastupdate;
	}
}
