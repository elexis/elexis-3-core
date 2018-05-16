package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.model.InvoiceState;

@Entity
@Table(name = "RECHNUNGEN")
public class Invoice extends AbstractDBObjectIdDeletedExtInfo {

	@Column(length = 8, name = "RnNummer")
	protected String number;

	@OneToOne
	@JoinColumn(name = "FallID")
	protected Fall fall;

	@OneToOne
	@JoinColumn(name = "MandantID")
	protected Kontakt mandator;

	@Column(name = "RnDatum", length = 8)
	protected LocalDate invoiceDate;

	@Column(name = "RnDatumVon", length = 8)
	protected LocalDate invoiceDateFrom;

	@Column(name = "RnDatumBis", length = 8)
	protected LocalDate invoiceDateTo;

	@Column(length = 20, name = "RnStatus")
	protected InvoiceState state;

	@Column(name = "StatusDatum", length = 8)
	protected LocalDate statusDate;

	@Column(length = 8, name = "Betrag")
	protected String amount;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Fall getFall() {
		return fall;
	}

	public void setFall(Fall fall) {
		this.fall = fall;
	}

	public Kontakt getMandator() {
		return mandator;
	}

	public void setMandator(Kontakt mandator) {
		this.mandator = mandator;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public LocalDate getInvoiceDateFrom() {
		return invoiceDateFrom;
	}

	public void setInvoiceDateFrom(LocalDate invoiceDateFrom) {
		this.invoiceDateFrom = invoiceDateFrom;
	}

	public LocalDate getInvoiceDateTo() {
		return invoiceDateTo;
	}

	public void setInvoiceDateTo(LocalDate invoiceDateTo) {
		this.invoiceDateTo = invoiceDateTo;
	}

	public InvoiceState getState() {
		return state;
	}

	public void setState(InvoiceState state) {
		this.state = state;
	}

	public LocalDate getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(LocalDate statusDate) {
		this.statusDate = statusDate;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	@Override
	public String getLabel() {
		return getFall() + " " + getNumber() + " " + getInvoiceDate() + getAmount();
	}

	@Override
	public String toString() {
		return super.toString() + " number=[" + number + "] fall=[" + fall + "] invoiceDate=[" + invoiceDate
				+ "] amount=[" + amount + "]";
	}

}
