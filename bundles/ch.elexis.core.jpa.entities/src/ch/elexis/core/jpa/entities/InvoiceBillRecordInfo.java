package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "INVOICE_BILL_RECORD_INFO")
@EntityListeners({ EntityWithIdListener.class })
public class InvoiceBillRecordInfo extends AbstractEntityWithId implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@ManyToOne
	@JoinColumn(name = "invoice")
	protected Invoice invoice;

	@Column(length = 255)
	private String billid;

	@Column(length = 255)
	private String billrecordid;

	@OneToOne
	@JoinColumn(name = "billed")
	protected Verrechnet billed;

	@Column(length = 25)
	private String infocode;

	@Lob
	private String info;

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

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public Verrechnet getBilled() {
		return billed;
	}

	public void setBilled(Verrechnet billed) {
		this.billed = billed;
	}

	public String getBillid() {
		return billid;
	}

	public void setBillid(String billid) {
		this.billid = billid;
	}

	public String getBillrecordid() {
		return billrecordid;
	}

	public void setBillrecordid(String billrecordid) {
		this.billrecordid = billrecordid;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getInfocode() {
		return infocode;
	}

	public void setInfocode(String infocode) {
		this.infocode = infocode;
	}
}
