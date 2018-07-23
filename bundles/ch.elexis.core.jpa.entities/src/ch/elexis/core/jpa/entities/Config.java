package ch.elexis.core.jpa.entities;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "config")
@EntityListeners(EntityWithIdListener.class)
public class Config implements EntityWithId {

	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	@Id
	@Column(unique = true, nullable = false, length = 80)
	private String param;

	@Lob
	private String wert;

	@Override
	public String getId(){
		return getParam();
	}
	
	@Override
	public void setId(String id){
		setParam(id);
	}
	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getWert() {
		return wert;
	}

	public void setWert(String wert) {
		this.wert = wert;
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
