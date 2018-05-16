package ch.elexis.core.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "laborder")
public class LabOrder extends AbstractDBObjectIdDeleted {

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
	private String state;

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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + getOrderid() + "] - ");
		sb.append(getState());
		sb.append(" - "); //$NON-NLS-1$
		sb.append(getItem().getLabel());
		if (getResult() != null) {
			sb.append(" - "); //$NON-NLS-1$
			sb.append(getResult().getResult());
		}
		return sb.toString();
	}

}
