package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;

@Entity
@Table(name = "reminders")
public class Reminder extends AbstractDBObjectIdDeletedExtInfo {
	
	public static final String ALL_RESPONSIBLE = "ALL";

	@OneToOne
	@JoinColumn(name = "IdentID")
	private Kontakt kontakt;

	@OneToOne
	@JoinColumn(name = "OriginID")
	private Kontakt creator;

	@OneToMany
	@JoinTable(name = "reminders_responsible_link", joinColumns = @JoinColumn(name = "ReminderID"), inverseJoinColumns = @JoinColumn(name = "ResponsibleID"))
	private Set<Kontakt> responsible = new HashSet<>();

	@Column(name = "Responsible", length = 25)
	private String responsibleValue;
	
	@Column(length = 8)
	protected LocalDate dateDue;

	@Column
	protected ProcessStatus status;

	@Column(name = "typ")
	protected Visibility visibility;

	@Column(length = 160)
	protected String subject;

	@Lob()
	protected String params;

	@Lob()
	protected String message;

	@Column(length = 1)
	protected Priority priority;

	@Column(length = 2)
	protected Type actionType;

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public Type getActionType() {
		return actionType;
	}

	public void setActionType(Type actionType) {
		this.actionType = actionType;
	}

	public Kontakt getCreator() {
		return creator;
	}

	public void setCreator(Kontakt creator) {
		this.creator = creator;
	}

	public LocalDate getDateDue() {
		return dateDue;
	}

	public void setDateDue(LocalDate dateDue) {
		this.dateDue = dateDue;
	}

	public ProcessStatus getStatus() {
		return status;
	}

	public void setStatus(ProcessStatus status) {
		this.status = status;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Set<Kontakt> getResponsible() {
		return responsible;
	}

	public void setResponsible(Set<Kontakt> responsible) {
		this.responsible = responsible;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getResponsibleValue() {
		return responsibleValue;
	}
	
	public void setResponsibleValue(String responsibleValue) {
		this.responsibleValue = responsibleValue;
	}
}
