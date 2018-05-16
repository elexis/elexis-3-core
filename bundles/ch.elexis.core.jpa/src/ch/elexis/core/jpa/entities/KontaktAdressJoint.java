package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "kontakt_adress_joint")
@NamedQueries({
		@NamedQuery(name = KontaktAdressJoint.QUERY_findAllIDisMyKontakt, query = "SELECT e FROM KontaktAdressJoint e WHERE e.myKontakt = :id"),
		@NamedQuery(name = KontaktAdressJoint.QUERY_findAllIDisOtherKontakt, query = "SELECT e FROM KontaktAdressJoint e WHERE e.otherKontakt = :id") })
public class KontaktAdressJoint extends AbstractDBObjectIdDeleted {

	public static final String QUERY_findAllIDisMyKontakt = "QUERY_findAllIDisMyKontakt";
	public static final String QUERY_findAllIDisOtherKontakt = "QUERY_findAllIDisOtherKontakt";

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
	public String getLabel() {
		return getMyKontakt() + " (" + getBezug() + ") " + getOtherKontakt();
	}

	@Override
	public String toString() {
		return super.toString() + " bezug=[" + bezug + "] myKontakt=[" + myKontakt + "] otherKontakt=[" + otherKontakt
				+ "] myRType=[" + myRType + "] otherRType=[" + otherRType + "]";
	}
}
