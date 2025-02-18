package ch.elexis.core.jpa.entities;

import java.beans.Transient;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.rgw.tools.TimeTool;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "TARMED_GROUP")
@EntityListeners(EntityWithIdListener.class)
public class TarmedGroup extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final Object SERVICES_SEPARATOR = "|";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 32)
	private String groupName;

	@Column(name = "services")
	@Lob
	private String rawServices;

	@Column(length = 3)
	private String law;

	@Column
	private LocalDate validFrom;

	@Column
	private LocalDate validTo;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getRawServices() {
		return rawServices;
	}

	public void setRawServices(String rawServices) {
		this.rawServices = rawServices;
	}

	public String getLaw() {
		return law;
	}

	public void setLaw(String law) {
		this.law = law;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	@Transient
	public String getCode() {
		return getGroupName();
	}

	@Transient
	public boolean validAt(TimeTool validTime) {
		TimeTool validFrom = new TimeTool(getValidFrom());
		TimeTool validTo = new TimeTool(getValidTo());
		return validTime.isAfterOrEqual(validFrom) && validTime.isBeforeOrEqual(validTo);
	}

	@Transient
	public List<String> getServices() {
		String value = getRawServices();
		if (value != null && !value.isEmpty()) {
			String[] parts = value.split("\\" + SERVICES_SEPARATOR);
			return Arrays.asList(parts);
		}
		return Collections.emptyList();
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
