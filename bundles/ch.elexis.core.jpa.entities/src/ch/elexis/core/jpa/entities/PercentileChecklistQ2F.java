package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "net_medshare_percentile_checklist_q2f")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "PercentileChecklistQ2F.form.question", query = "SELECT pqf FROM PercentileChecklistQ2F pqf WHERE pqf.form = :form AND pqf.question = :question")
public class PercentileChecklistQ2F extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@ManyToOne
	@JoinColumn(name = "FormId")
	private PercentileChecklistForm form;

	@ManyToOne
	@JoinColumn(name = "QuestionId")
	private PercentileChecklistQuestion question;

	@Column(length = 11)
	private String sortorder;

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

	public PercentileChecklistForm getForm() {
		return form;
	}

	public void setForm(PercentileChecklistForm form) {
		this.form = form;
	}

	public PercentileChecklistQuestion getQuestion() {
		return question;
	}

	public void setQuestion(PercentileChecklistQuestion question) {
		this.question = question;
	}

	public String getSortorder() {
		return sortorder;
	}

	public void setSortOrder(String sortorder) {
		this.sortorder = sortorder;
	}
}
