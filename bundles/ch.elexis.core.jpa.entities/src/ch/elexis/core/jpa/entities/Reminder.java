package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.ReminderPriorityConverter;
import ch.elexis.core.jpa.entities.converter.ReminderProcessStatusConverter;
import ch.elexis.core.jpa.entities.converter.ReminderTypeConverter;
import ch.elexis.core.jpa.entities.converter.ReminderVisibilityConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reminders")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class Reminder extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	public static final String ALL_RESPONSIBLE = "ALL";

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

	@OneToOne
	@JoinColumn(name = "IdentID")
	private Kontakt kontakt;

	@OneToOne
	@JoinColumn(name = "OriginID")
	private Kontakt creator;

	@OneToOne
	@JoinColumn(name = "GroupID")
	private UserGroup userGroup;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "reminders_responsible_link", joinColumns = @JoinColumn(name = "ReminderID"), inverseJoinColumns = @JoinColumn(name = "ResponsibleID"))
	private List<Kontakt> responsible;

	@Column(name = "Responsible", length = 25)
	private String responsibleValue;

	@Column(length = 8)
	protected LocalDate dateDue;

	@Column
	@Convert(converter = ReminderProcessStatusConverter.class)
	protected ProcessStatus status;

	@Column(name = "typ")
	@Convert(converter = ReminderVisibilityConverter.class)
	protected Visibility visibility;

	@Column(length = 160)
	protected String subject;

	@Lob()
	protected String params;

	@Lob()
	protected String message;

	@Column(length = 1)
	@Convert(converter = ReminderPriorityConverter.class)
	protected Priority priority;

	@Column(length = 2)
	@Convert(converter = ReminderTypeConverter.class)
	protected Type actionType;

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
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

	public List<Kontakt> getResponsible() {
		if (responsible == null) {
			responsible = new ArrayList<>();
		}
		return responsible;
	}

	public void setResponsible(List<Kontakt> responsible) {
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
