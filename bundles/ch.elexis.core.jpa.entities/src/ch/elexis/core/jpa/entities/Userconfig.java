package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "userconfig")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "Userconfig.owner.param", query = "SELECT uc FROM Userconfig uc WHERE uc.owner = :owner AND uc.param = :param")
public class Userconfig implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@ManyToOne(fetch = FetchType.LAZY)
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
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
}
