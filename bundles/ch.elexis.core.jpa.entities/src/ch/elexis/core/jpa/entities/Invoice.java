package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.ac.AoboEntity;
import ch.elexis.core.ac.AoboEntityColumn;
import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.InvoiceStateConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.jpa.entities.listener.InvoiceEntityListener;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@AoboEntity
@Table(name = "RECHNUNGEN")
@EntityListeners({ InvoiceEntityListener.class, EntityWithIdListener.class })
@Cache(expiry = 15000)
@NamedQuery(name = "Invoice.number", query = "SELECT i FROM Invoice i WHERE i.deleted = false AND i.number = :number")
@NamedQuery(name = "Invoice.number.aobo", query = "SELECT i FROM Invoice i WHERE i.deleted = false AND i.number = :number AND (i.mandator.id IN :aoboids OR i.mandator is null)")
@NamedQuery(name = "Invoice.from.to.paid.notempty", query = "SELECT i FROM Invoice i WHERE i.deleted = false "
		+ "AND i.invoiceDate >= :from AND i.invoiceDate <= :to AND NOT (i.state = ch.elexis.core.model.InvoiceState.PAID AND i.amount = '0')")
@NamedQuery(name = "Invoice.from.to.mandator.paid.notempty", query = "SELECT i FROM Invoice i WHERE i.deleted = false "
		+ "AND i.mandator = :mandator AND i.invoiceDate >= :from AND i.invoiceDate <= :to AND NOT (i.state = ch.elexis.core.model.InvoiceState.PAID AND i.amount = '0')")
public class Invoice extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	public static final String REMARK = "Bemerkung";
	public static final String ATTACHMENTS = "Attachments";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Lob
	protected byte[] extInfo;

	@Column(length = 8, name = "RnNummer")
	protected String number;

	@OneToOne
	@JoinColumn(name = "FallID")
	protected Fall fall;

	@AoboEntityColumn
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
	@Convert(converter = InvoiceStateConverter.class)
	protected InvoiceState state;

	@Column(name = "StatusDatum", length = 8)
	protected LocalDate statusDate;

	@Column(length = 8, name = "Betrag")
	protected String amount;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.REFRESH)
	private List<VerrechnetCopy> invoiceBilled;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.REFRESH)
	private List<Behandlung> encounters;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.REFRESH)
	private List<Zahlung> payments;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice", cascade = CascadeType.REFRESH)
	private List<AccountTransaction> transactions;

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
	public byte[] getExtInfo() {
		return extInfo;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
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

	public List<VerrechnetCopy> getInvoiceBilled() {
		return invoiceBilled;
	}

	public List<Behandlung> getEncounters() {
		return encounters;
	}

	public List<Zahlung> getPayments() {
		return payments;
	}

	public List<AccountTransaction> getTransactions() {
		return transactions;
	}
}
