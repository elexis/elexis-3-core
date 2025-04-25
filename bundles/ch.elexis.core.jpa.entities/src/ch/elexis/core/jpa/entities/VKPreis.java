package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vk_preise")
@EntityListeners(EntityWithIdListener.class)
public class VKPreis extends AbstractEntityWithId implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column(length = 80)
	private String typ;

	@Column(length = 8)
	private LocalDate datum_von;

	@Column(length = 8)
	private LocalDate datum_bis;

	@Column(length = 8)
	private String multiplikator;

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDate getDatum_von() {
		return datum_von;
	}

	public void setDatum_von(LocalDate datum_von) {
		this.datum_von = datum_von;
	}

	public LocalDate getDatum_bis() {
		return datum_bis;
	}

	public void setDatum_bis(LocalDate datum_bis) {
		this.datum_bis = datum_bis;
	}

	public String getMultiplikator() {
		return multiplikator;
	}

	public void setMultiplikator(String multiplikator) {
		this.multiplikator = multiplikator;
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
