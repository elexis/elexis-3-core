package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "net_medshare_percentile_checklist_data")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class PercentileChecklistData extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@ManyToOne
	@JoinColumn(name = "PatientId")
	private Kontakt patient;

	@ManyToOne
	@JoinColumn(name = "QuestionId")
	private PercentileChecklistQuestion question;

	@ManyToOne
	@JoinColumn(name = "FormId")
	private PercentileChecklistForm form;

	@ManyToOne
	@JoinColumn(name = "KonsId")
	private Behandlung behandlung;

	@Column(length = 11)
	private String sortOrder;

	@Column(length = 8)
	private LocalDate konsDate;

	@Column(length = 8)
	private String answer;

	@Lob
	private String remark;

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

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public PercentileChecklistQuestion getQuestion() {
		return question;
	}

	public void setQuestion(PercentileChecklistQuestion question) {
		this.question = question;
	}

	public PercentileChecklistForm getForm() {
		return form;
	}

	public void setForm(PercentileChecklistForm form) {
		this.form = form;
	}

	public Behandlung getBehandlung() {
		return behandlung;
	}

	public void setBehandlung(Behandlung behandlung) {
		this.behandlung = behandlung;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public LocalDate getKonsDate() {
		return konsDate;
	}

	public void setKonsDate(LocalDate konsDate) {
		this.konsDate = konsDate;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
