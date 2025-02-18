package ch.elexis.core.jpa.entities;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_ICPC_EPISODES")
@EntityListeners(EntityWithIdListener.class)
public class ICPCEpisode extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Lob
	protected byte[] extInfo;

	@ManyToOne()
	@JoinColumn(name = "PatientID")
	private Kontakt patientKontakt;

	@OneToMany(mappedBy = "Episode")
	private List<ICPCEpisodeDiagnosisLink> diagnosisLinks = new ArrayList<>();

	@Column(length = 256, name = "Title")
	private String title;

	@Column(length = 20, name = "StartDate")
	private String startDate;

	@Column(length = 10, name = "Number")
	private String number;

	@Convert(converter = IntegerStringConverter.class)
	private int status;

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

	@Override
	public byte[] getExtInfo() {
		return extInfo;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Kontakt getPatientKontakt() {
		return patientKontakt;
	}

	public void setPatientKontakt(Kontakt patientKontakt) {
		this.patientKontakt = patientKontakt;
	}

	public void addDiagnosis(String diagnosis) {
		ICPCEpisodeDiagnosisLink link = new ICPCEpisodeDiagnosisLink();
		link.setEpisode(this);
		link.setDiagnosis(diagnosis);
		diagnosisLinks.add(link);
	}

	public void removeDiagnosis(ICPCEpisodeDiagnosisLink link) {
		diagnosisLinks.remove(link);
	}

	public List<ICPCEpisodeDiagnosisLink> getLinks() {
		return diagnosisLinks;
	}
}
