package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.Mutable;

import ch.elexis.core.ac.AoboEntity;
import ch.elexis.core.ac.AoboEntityColumn;
import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.VersionedResourceConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.rgw.tools.VersionedResource;

@Entity
@AoboEntity
@Table(name = "behandlungen")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "Behandlung.fall", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall = :fall")
@NamedQuery(name = "Behandlung.fall.aobo", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall = :fall AND (b.mandant IN :aoboids OR b.mandant is null)")
@NamedQuery(name = "Behandlung.patient", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall.patientKontakt = :patient ORDER BY b.datum desc")
@NamedQuery(name = "Behandlung.patient.aobo", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall.patientKontakt = :patient AND (b.mandant IN :aoboids OR b.mandant is null) ORDER BY b.datum, b.time desc")
@NamedQuery(name = "Behandlung.patient.last", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall.patientKontakt = :patient ORDER BY b.datum, b.time desc")
@NamedQuery(name = "Behandlung.patient.last.aobo", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall.patientKontakt = :patient AND (b.mandant IN :aoboids OR b.mandant is null) ORDER BY b.datum, b.time desc")
public class Behandlung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	private boolean billable = true;

	@ManyToOne
	@JoinColumn(name = "FallID")
	private Fall fall;

	@AoboEntityColumn
	@OneToOne
	@JoinColumn(name = "mandantId")
	private Kontakt mandant;

	@ManyToOne
	@JoinColumn(name = "rechnungsid")
	private Invoice invoice;

	@Column(length = 8)
	private LocalDate datum;

	@Column(length = 6, name = "Zeit")
	private String time;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "behdl_dg_joint", joinColumns = @JoinColumn(name = "BehandlungsID"), inverseJoinColumns = @JoinColumn(name = "DiagnoseID"))
	private List<Diagnosis> diagnoses;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "behandlung", cascade = CascadeType.REFRESH)
	@OrderBy("klasse ASC, leistungenCode ASC")
	private List<Verrechnet> billed;

	@Mutable
	@Convert(converter = VersionedResourceConverter.class)
	private VersionedResource eintrag;

	public Fall getFall() {
		return fall;
	}

	public void setFall(Fall fall) {
		this.fall = fall;
	}

	public Kontakt getMandant() {
		return mandant;
	}

	public void setMandant(Kontakt mandant) {
		this.mandant = mandant;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	/**
	 * @return date if value is set, else <code>null</code>
	 */
	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public List<Diagnosis> getDiagnoses() {
		if (diagnoses == null) {
			diagnoses = new ArrayList<>();
		}
		return diagnoses;
	}

	public List<Verrechnet> getBilled() {
		if (billed == null) {
			billed = new ArrayList<>();
		}
		return billed;
	}

	public VersionedResource getEintrag() {
		if (eintrag == null) {
			eintrag = VersionedResource.load(null);
		}
		return eintrag;
	}

	public void setEintrag(VersionedResource eintrag) {
		this.eintrag = eintrag;
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

	public boolean getBillable() {
		return billable;
	}

	public void setBillable(boolean value) {
		this.billable = value;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
