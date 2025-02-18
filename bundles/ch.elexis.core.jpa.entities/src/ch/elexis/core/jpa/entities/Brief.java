package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "BRIEFE")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Brief.patient", query = "SELECT b FROM Brief b WHERE b.deleted = false AND b.patient = :patient")
public class Brief extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

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

	@Column(length = 80, name = "MimeType")
	protected String mimetype;

	@OneToOne(cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	protected Heap content;

	@Lob()
	protected String path;

	@Basic
	@Lob()
	protected String note;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean geloescht = false;

	@Column(name = "DOCUMENT_STATUS")
	protected int status;

	@Column(length = 255)
	protected String keywords;

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

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Heap getOrCreateContent() {
		if (content == null) {
			content = new Heap();
			content.setId(getId());
		}
		return content;
	}
}
