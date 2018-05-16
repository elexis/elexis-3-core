package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TARMED_DEFINITIONEN")
public class TarmedDefinitionen extends AbstractDBObjectIdDeleted {

	@Column(length = 20)
	private String spalte;

	@Column(length = 5)
	private String kuerzel;

	@Column(length = 255)
	private String titel;

	@Column(length = 3)
	private String law;

	public String getSpalte() {
		return spalte;
	}

	public void setSpalte(String spalte) {
		this.spalte = spalte;
	}

	public String getKuerzel() {
		return kuerzel;
	}

	public void setKuerzel(String kuerzel) {
		this.kuerzel = kuerzel;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getLaw() {
		return law;
	}

	public void setLaw(String law) {
		this.law = law;
	}
	
}
