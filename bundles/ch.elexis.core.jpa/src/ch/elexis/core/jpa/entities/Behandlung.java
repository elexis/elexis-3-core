package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

import ch.rgw.tools.VersionedResource;

@Entity
@Table(name = "behandlungen")
public class Behandlung extends AbstractDBObjectIdDeleted {

	@OneToOne
	@JoinColumn(name = "fallId")
	private Fall fall;

	@OneToOne
	@JoinColumn(name = "mandantId")
	private Kontakt mandant;

	@OneToOne
	@JoinColumn(name = "RechnungsID")
	private Invoice invoice;

	@Column(length = 8)
	private LocalDate datum;

	@OneToMany
	@JoinTable(name = "behdl_dg_joint", joinColumns = @JoinColumn(name = "BehandlungsID"), inverseJoinColumns = @JoinColumn(name = "DiagnoseID"))
	private Set<Diagnosis> diagnoses;

	/**
	 * Seems to be always null
	 */
	@Column(length = 25, name = "leistungen")
	private String leistungenId;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "VersionedResourceConverter")
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

	public Set<Diagnosis> getDiagnoses() {
		return diagnoses;
	}

	public void setDiagnoses(Set<Diagnosis> diagnoses) {
		this.diagnoses = diagnoses;
	}

	public String getLeistungenId() {
		return leistungenId;
	}

	public void setLeistungenId(String leistungenId) {
		this.leistungenId = leistungenId;
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
	public String getLabel() {
		return getDatum() + " " + getMandant() != null ? getMandant().getLabel() : "";
	}

	@Override
	public String toString() {
		return super.toString() + "datum=[" + getDatum() + "] mandant=[" + getMandant() + "]";
	}
}
