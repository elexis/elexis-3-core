package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "AT_MEDEVIT_ELEXIS_LABMAP")
public class LabMapping extends AbstractDBObjectIdDeleted {

	@Column(length = 255)
	protected String itemname;

	@OneToOne
	@JoinColumn(name = "originid")
	protected Kontakt origin;

	@OneToOne
	@JoinColumn(name = "labitemid")
	protected LabItem labItem;

	@Column(length = 1)
	@Convert("booleanStringConverter")
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

}
