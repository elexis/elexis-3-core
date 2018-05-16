package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "faelle")
public class Fall extends AbstractDBObjectIdDeletedExtInfo {

	@Column(length = 25)
	private String betriebsNummer;

	@Column(length = 80)
	private String bezeichnung;

	@Column(length = 8)
	private LocalDate datumBis;

	@Column(length = 8)
	private LocalDate datumVon;

	@Column(length = 80)
	private String diagnosen;

	@Column(length = 25)
	private String fallNummer;

	@OneToOne
	@JoinColumn(name = "garantID")
	private Kontakt garantKontakt;

	@Column(length = 20)
	private String gesetz;

	@Column(length = 80)
	private String grund;

	@OneToOne
	@JoinColumn(name = "kostentrID")
	private Kontakt kostentrKontakt;

	@ManyToOne()
	@JoinColumn(name = "patientID")
	private Kontakt patientKontakt;

	@Column(length = 80)
	private String status;

	@Column(length = 25)
	private String versNummer;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "FallID", insertable = false)
	protected List<Behandlung> consultations;

	public String getBetriebsNummer() {
		return this.betriebsNummer;
	}

	public void setBetriebsNummer(String betriebsNummer) {
		this.betriebsNummer = betriebsNummer;
	}

	public String getBezeichnung() {
		return this.bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	/**
	 * @return date if value is set, else <code>null</code>
	 */
	public LocalDate getDatumBis() {
		return datumBis;
	}

	public void setDatumBis(LocalDate datumBis) {
		this.datumBis = datumBis;
	}

	/**
	 * @return date if value is set, else <code>null</code>
	 */
	public LocalDate getDatumVon() {
		return datumVon;
	}

	public void setDatumVon(LocalDate datumVon) {
		this.datumVon = datumVon;
	}

	public Kontakt getGarantKontakt() {
		return garantKontakt;
	}

	public void setGarantKontakt(Kontakt garantKontakt) {
		this.garantKontakt = garantKontakt;
	}

	public Kontakt getKostentrKontakt() {
		return kostentrKontakt;
	}

	public void setKostentrKontakt(Kontakt kostentrKontakt) {
		this.kostentrKontakt = kostentrKontakt;
	}

	public Kontakt getPatientKontakt() {
		return patientKontakt;
	}

	public void setPatientKontakt(Kontakt patientKontakt) {
		this.patientKontakt = patientKontakt;
	}

	public String getDiagnosen() {
		return this.diagnosen;
	}

	public void setDiagnosen(String diagnosen) {
		this.diagnosen = diagnosen;
	}

	public String getFallNummer() {
		return this.fallNummer;
	}

	public void setFallNummer(String fallNummer) {
		this.fallNummer = fallNummer;
	}

	public String getGesetz() {
		return this.gesetz;
	}

	public void setGesetz(String gesetz) {
		this.gesetz = gesetz;
	}

	public String getGrund() {
		return this.grund;
	}

	public void setGrund(String grund) {
		this.grund = grund;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVersNummer() {
		return this.versNummer;
	}

	public void setVersNummer(String versNummer) {
		this.versNummer = versNummer;
	}

	public List<Behandlung> getConsultations() {
		return consultations;
	}

	public void setConsultations(List<Behandlung> consultations) {
		this.consultations = consultations;
	}
	
	@Transient
	public Kontakt getPatient() {
		return getPatientKontakt();
	}

	@Override
	public String getLabel() {
		return getGrund() + " " + getBezeichnung() + " " + getDatumVon() + " " + getDatumBis();
	}

	@Override
	public String toString() {
		return super.toString() + "bezeichnung=[" + bezeichnung + "] datumVon=[" + datumVon + "] grund=[" + grund
				+ "] patientKontakt=[" + patientKontakt + "]";
	}

}
