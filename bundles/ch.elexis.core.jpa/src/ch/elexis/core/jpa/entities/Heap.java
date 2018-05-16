package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "HEAP")
public class Heap extends AbstractDBObjectIdDeleted {

	@Column(length = 8)
	protected LocalDate datum;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected byte[] inhalt;

	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public byte[] getInhalt() {
		return inhalt;
	}

	public void setInhalt(byte[] inhalt) {
		this.inhalt = inhalt;
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
