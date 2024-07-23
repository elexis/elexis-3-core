package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "KONTO")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "AccountTransaction.invoice", query = "SELECT at FROM AccountTransaction at WHERE at.deleted = false AND at.invoice = :invoice")
@NamedQuery(name = "AccountTransaction.patient", query = "SELECT at FROM AccountTransaction at WHERE at.deleted = false AND at.patient = :patient")
@NamedQuery(name = "AccountTransaction.balance.patient", query = "SELECT SUM(at.amount) FROM AccountTransaction at WHERE at.deleted = false AND at.patient = :patient")
public class AccountTransaction extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patientid")
	private Kontakt patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rechnungsid")
	private Invoice invoice;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "zahlungsid")
	private Zahlung zahlung;

	@Column(name = "betrag")
	protected Integer amount;

	@Column(name = "datum", length = 8)
	protected LocalDate date;

	@Column(name = "bemerkung", length = 80)
	private String remark;

	@Column(name = "account", length = 255)
	private String account;

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public Zahlung getZahlung() {
		return zahlung;
	}

	public void setZahlung(Zahlung zahlung) {
		this.zahlung = zahlung;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
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
