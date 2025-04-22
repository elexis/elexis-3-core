package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.ERSCodeTypeConverter;
import ch.elexis.core.jpa.entities.converter.ERSRejectCodeTypeConverter;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.esr.ESRCode;
import ch.elexis.core.model.esr.ESRRejectCode;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "esrrecords")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class EsrRecord extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 8)
	private LocalDate datum;

	@Column(length = 8)
	private LocalDate eingelesen;

	@Column(length = 8)
	private LocalDate verarbeitet;

	@Column(length = 8)
	private LocalDate gutschrift;

	@Column(length = 8)
	private LocalDate gebucht;

	@Convert(converter = IntegerStringConverter.class)
	private int betraginrp;

	@Column(length = 3)
	@Convert(converter = ERSCodeTypeConverter.class)
	private ESRCode code;

	@Column(length = 3)
	@Convert(converter = ERSRejectCodeTypeConverter.class)
	private ESRRejectCode rejectcode;

	@Column(length = 4)
	private String kosten;

	@ManyToOne
	@JoinColumn(name = "rechnungsid")
	private Invoice rechnung;

	@ManyToOne
	@JoinColumn(name = "patientid")
	private Kontakt patient;

	@ManyToOne
	@JoinColumn(name = "mandantid")
	private Kontakt mandant;

	@Column(length = 80)
	private String file;

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

	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate value) {
		this.datum = value;
	}

	public LocalDate getEingelesen() {
		return eingelesen;
	}

	public void setEingelesen(LocalDate eingelesen) {
		this.eingelesen = eingelesen;
	}

	public LocalDate getVerarbeitet() {
		return verarbeitet;
	}

	public void setVerarbeitet(LocalDate verarbeitet) {
		this.verarbeitet = verarbeitet;
	}

	public LocalDate getGutschrift() {
		return gutschrift;
	}

	public void setGutschrift(LocalDate gutschrift) {
		this.gutschrift = gutschrift;
	}

	public LocalDate getGebucht() {
		return gebucht;
	}

	public void setGebucht(LocalDate gebucht) {
		this.gebucht = gebucht;
	}

	public Invoice getRechnung() {
		return rechnung;
	}

	public void setRechnung(Invoice rechnung) {
		this.rechnung = rechnung;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public Kontakt getMandant() {
		return mandant;
	}

	public void setMandant(Kontakt mandant) {
		this.mandant = mandant;
	}

	public int getBetraginrp() {
		return betraginrp;
	}

	public void setBetraginrp(int betraginrp) {
		this.betraginrp = betraginrp;
	}

	public ESRCode getCode() {
		return code;
	}

	public ESRRejectCode getRejectcode() {
		return rejectcode;
	}

	public String getFile() {
		return file;
	}
}
