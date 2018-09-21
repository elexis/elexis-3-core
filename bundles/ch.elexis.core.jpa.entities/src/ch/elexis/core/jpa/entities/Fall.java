package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "faelle")
@EntityListeners(EntityWithIdListener.class)
public class Fall implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Basic(fetch = FetchType.LAZY)
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

	public Kontakt getPatient(){
		return patientKontakt;
	}

	public void setPatient(Kontakt patientKontakt){
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
	public byte[] getExtInfo(){
		return extInfo;
	}
	
	@Override
	public void setExtInfo(byte[] extInfo){
		this.extInfo = extInfo;
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
	
	@Override
	public int hashCode(){
		return EntityWithId.idHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj){
		return EntityWithId.idEquals(this, obj);
	}
}
