package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "faelle")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Fall.patient", query = "SELECT f FROM Fall f WHERE f.deleted = false AND f.patientKontakt = :patient")
public class Fall extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "fall", cascade = CascadeType.REFRESH)
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

	public Kontakt getPatient() {
		return patientKontakt;
	}

	public void setPatient(Kontakt patientKontakt) {
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
}
