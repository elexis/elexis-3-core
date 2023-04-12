package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.ERSCodeTypeConverter;
import ch.elexis.core.jpa.entities.converter.ERSRejectCodeTypeConverter;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.esr.ESRCode;
import ch.elexis.core.model.esr.ESRRejectCode;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "esrrecords")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class EsrRecord extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
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
}
