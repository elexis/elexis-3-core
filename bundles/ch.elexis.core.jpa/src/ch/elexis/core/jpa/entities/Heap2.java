package ch.elexis.core.jpa.entities;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "HEAP2")
public class Heap2 extends AbstractDBObjectIdDeleted {

	@Column(length = 8)
	protected Date datum;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected byte[] contents;

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}
	
	@Override
	public String getLabel() {
		return getDatum() + "";
	}

	@Override
	public String toString() {
		return super.toString() + " datum=[" + datum + "]";
	}
}
