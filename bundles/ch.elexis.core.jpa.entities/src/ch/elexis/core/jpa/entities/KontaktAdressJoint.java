package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "kontakt_adress_joint")
@EntityListeners(EntityWithIdListener.class)
@NamedQueries({
		@NamedQuery(name = KontaktAdressJoint.QUERY_findAllIDisMyKontakt, query = "SELECT e FROM KontaktAdressJoint e WHERE e.myKontakt = :id"),
		@NamedQuery(name = KontaktAdressJoint.QUERY_findAllIDisOtherKontakt, query = "SELECT e FROM KontaktAdressJoint e WHERE e.otherKontakt = :id") })
public class KontaktAdressJoint extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted {

	public static final String QUERY_findAllIDisMyKontakt = "QUERY_findAllIDisMyKontakt";
	public static final String QUERY_findAllIDisOtherKontakt = "QUERY_findAllIDisOtherKontakt";

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Column(length = 80)
	private String bezug;

	@JoinColumn(name = "myID")
	private Kontakt myKontakt;

	@JoinColumn(name = "otherID")
	private Kontakt otherKontakt;

	@Column(length = 4)
	private Integer myRType;

	@Column(length = 4)
	private Integer otherRType;

	public Kontakt getMyKontakt() {
		return myKontakt;
	}

	public void setMyKontakt(Kontakt myKontakt) {
		this.myKontakt = myKontakt;
	}

	public Kontakt getOtherKontakt() {
		return otherKontakt;
	}

	public void setOtherKontakt(Kontakt otherKontakt) {
		this.otherKontakt = otherKontakt;
	}

	public String getBezug() {
		return this.bezug;
	}

	public void setBezug(String bezug) {
		this.bezug = bezug;
	}

	public Integer getMyRType() {
		return myRType;
	}

	public void setMyRType(Integer myRType) {
		this.myRType = myRType;
	}

	public Integer getOtherRType() {
		return otherRType;
	}

	public void setOtherRType(Integer otherRType) {
		this.otherRType = otherRType;
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
