package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "AT_MEDEVIT_ELEXIS_LABMAP")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "LabMapping.origin.itemname", query = "SELECT lm FROM LabMapping lm WHERE lm.deleted = false AND lm.origin = :origin AND lm.itemname = :itemname")
public class LabMapping extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 255)
	protected String itemname;

	@OneToOne
	@JoinColumn(name = "originid")
	protected Kontakt origin;

	@OneToOne
	@JoinColumn(name = "labitemid")
	protected LabItem labItem;

	@Column(length = 1)
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean charge;

	public String getItemname() {
		return itemname;
	}

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}

	public Kontakt getOrigin() {
		return origin;
	}

	public void setOrigin(Kontakt origin) {
		this.origin = origin;
	}

	public LabItem getLabItem() {
		return labItem;
	}

	public void setLabItem(LabItem labItem) {
		this.labItem = labItem;
	}

	public boolean isCharge() {
		return charge;
	}

	public void setCharge(boolean charge) {
		this.charge = charge;
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
