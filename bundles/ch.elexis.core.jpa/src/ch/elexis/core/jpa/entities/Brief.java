package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;

@Entity
@Table(name = "BRIEFE")
public class Brief extends AbstractDBObjectIdDeleted {

	@Column(length = 255, name = "betreff")
	protected String subject;

	@Column(name = "Datum", length = 24)
	protected LocalDateTime creationDate;

	@Column(name = "modifiziert", length = 24)
	protected LocalDateTime modifiedDate;

	@Column(length = 8)
	protected LocalDate gedruckt;

	@OneToOne
	@JoinColumn(name = "absenderID")
	protected Kontakt sender;

	@OneToOne
	@JoinColumn(name = "destID")
	protected Kontakt recipient;

	@OneToOne
	@JoinColumn(name = "patientID")
	protected Kontakt patient;

	@OneToOne
	@JoinColumn(name = "behandlungsID")
	protected Behandlung consultation;

	@Column(length = 30)
	protected String typ;

	@Column(length = 80)
	protected String mimetype;

	@OneToOne(cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	protected Heap content;

	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected String path;

	@Basic
	@Lob()
	protected String note;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean geloescht = false;

	public Brief() {
		super();
		content = new Heap();
		content.setId(getId());
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(LocalDateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public LocalDate getGedruckt() {
		return gedruckt;
	}

	public void setGedruckt(LocalDate gedruckt) {
		this.gedruckt = gedruckt;
	}

	public Kontakt getSender() {
		return sender;
	}

	public void setSender(Kontakt sender) {
		this.sender = sender;
	}

	public Kontakt getRecipient() {
		return recipient;
	}

	public Behandlung getConsultation() {
		return consultation;
	}

	public void setConsultation(Behandlung consultation) {
		this.consultation = consultation;
	}

	public void setRecipient(Kontakt recipient) {
		this.recipient = recipient;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Heap getContent() {
		return content;
	}

	public void setContent(Heap content) {
		this.content = content;
	}

	public boolean isGeloescht() {
		return geloescht;
	}

	public void setGeloescht(boolean geloescht) {
		this.geloescht = geloescht;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String getLabel() {
		return getSubject() + " " + getCreationDate();
	}

	@Override
	public String toString() {
		return super.toString() + "subject=[" + getSubject() + "] date=[" + getCreationDate() + "]";
	}
}
