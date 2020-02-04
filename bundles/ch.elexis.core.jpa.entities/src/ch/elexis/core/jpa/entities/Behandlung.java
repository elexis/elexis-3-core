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
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Mutable;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.VersionedResourceConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.rgw.tools.VersionedResource;

@Entity
@Table(name = "behandlungen")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Behandlung.fall", query = "SELECT b FROM Behandlung b WHERE b.deleted = false AND b.fall = :fall")
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

	@OneToOne
	@JoinColumn(name = "mandantId")
	private Kontakt mandant;

	@ManyToOne
	@JoinColumn(name = "rechnungsid")
	private Invoice invoice;

	@Column(length = 8)
	private LocalDate datum;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "behdl_dg_joint", joinColumns = @JoinColumn(name = "BehandlungsID"), inverseJoinColumns = @JoinColumn(name = "DiagnoseID"))
	private List<Diagnosis> diagnoses = new ArrayList<>();
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "behandlung")
	private List<Verrechnet> billed = new ArrayList<>();

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

	public List<Diagnosis> getDiagnoses(){
		return diagnoses;
	}
	
	public List<Verrechnet> getBilled(){
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
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
	
	public boolean getBillable(){
		return billable;
	}
	
	public void setBillable(boolean value){
		this.billable = value;
	}
}
