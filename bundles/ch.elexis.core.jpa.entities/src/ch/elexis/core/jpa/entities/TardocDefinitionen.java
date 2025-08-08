package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "TARDOC_DEFINITIONEN")
@EntityListeners(EntityWithIdListener.class)
@NamedQueries({
		@NamedQuery(name = "TardocDefinitionen.spalte.kuerzel", query = "SELECT td FROM TardocDefinitionen td WHERE td.spalte = :spalte AND td.kuerzel = :kuerzel"),
		@NamedQuery(name = "TardocDefinitionen.spalte.titel", query = "SELECT td FROM TardocDefinitionen td WHERE td.spalte = :spalte AND td.titel = :titel") })
public class TardocDefinitionen extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

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

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
