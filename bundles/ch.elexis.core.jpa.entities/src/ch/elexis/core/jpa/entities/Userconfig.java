package ch.elexis.core.jpa.entities;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "userconfig")
@EntityListeners(EntityWithIdListener.class)
public class Userconfig implements EntityWithId {

	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	@ManyToOne()
	@JoinColumn(name = "UserID")
	private Kontakt owner;
	
	@Id
	@Column(unique = true, nullable = false, length = 80)
	private String param;
	
	@Lob
	private String value;

	public Kontakt getOwner() {
		return owner;
	}

	public void setOwner(Kontakt owner) {
		this.owner = owner;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String getId(){
		return getParam();
	}
	
	@Override
	public void setId(String id){
		setParam(id);
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
