package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.LabOrderStateConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.jpa.entities.listener.LabOrderEntityListener;
import ch.elexis.core.model.LabOrderState;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "laborder")
@EntityListeners({ LabOrderEntityListener.class, EntityWithIdListener.class })
@Cache(expiry = 15000)
@NamedQuery(name = "LabOrder.orderid", query = "SELECT lo FROM LabOrder lo WHERE  lo.deleted = false AND lo.orderid = :orderid")
@NamedQuery(name = "LabOrder.item.patient.state", query = "SELECT lo FROM LabOrder lo WHERE  lo.deleted = false AND lo.item = :item AND lo.patient = :patient AND lo.state = :state")
public class LabOrder extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@OneToOne
	@JoinColumn(name = "userid")
	private Kontakt user;

	@OneToOne
	@JoinColumn(name = "mandant")
	private Kontakt mandator;

	@OneToOne
	@JoinColumn(name = "patient")
	private Kontakt patient;

	@OneToOne
	@JoinColumn(name = "item")
	private LabItem item;

	@OneToOne
	@JoinColumn(name = "result")
	private LabResult result;

	@Column(length = 128)
	private String orderid;

	@Column(length = 255)
	private String groupname;

	@Column(length = 24)
	private LocalDateTime time;

	@Column(length = 24)
	private LocalDateTime observationTime;

	@Column(length = 1)
	@Convert(converter = LabOrderStateConverter.class)
	private LabOrderState state;

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	private boolean userResolved;

	public Kontakt getUser() {
		return user;
	}

	public void setUser(Kontakt user) {
		this.user = user;
	}

	public Kontakt getMandator() {
		return mandator;
	}

	public void setMandator(Kontakt mandator) {
		this.mandator = mandator;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public LabItem getItem() {
		return item;
	}

	public void setItem(LabItem item) {
		this.item = item;
	}

	public LabResult getResult() {
		return result;
	}

	public void setResult(LabResult result) {
		this.result = result;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public LocalDateTime getObservationTime() {
		return observationTime;
	}

	public void setObservationTime(LocalDateTime observationTime) {
		this.observationTime = observationTime;
	}

	public LabOrderState getState() {
		return state;
	}

	public void setState(LabOrderState state) {
		this.state = state;
	}

	public boolean isUserResolved() {
		return userResolved;
	}

	public void setUserResolved(boolean userResolved) {
		this.userResolved = userResolved;
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
