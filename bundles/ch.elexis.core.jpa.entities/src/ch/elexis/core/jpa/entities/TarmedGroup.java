package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.rgw.tools.TimeTool;

@Entity
@Table(name = "TARMED_GROUP")
@EntityListeners(EntityWithIdListener.class)
public class TarmedGroup implements EntityWithId, EntityWithDeleted {
	
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

	@Column(name="services")
	@Lob
	private String rawServices;

	@Column(length = 3)
	private String law;
	
	@Column
	private LocalDate validFrom;
	
	@Column
	private LocalDate validTo;

	@OneToOne
	@JoinColumn(name = "id", insertable = false, updatable = false)
	private TarmedExtension extension;
	
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
	
	public TarmedExtension getExtension() {
		return extension;
	}
	
	public void setExtension(TarmedExtension extension) {
		this.extension = extension;
	}
	
	@Transient
	public String getCode(){
		return getGroupName();
	}
	
	@Transient
	public boolean validAt(TimeTool validTime){
		TimeTool validFrom = new TimeTool(getValidFrom());
		TimeTool validTo = new TimeTool(getValidTo());
		return validTime.isAfterOrEqual(validFrom) && validTime.isBeforeOrEqual(validTo);
	}
	
	@Transient
	public List<String> getServices(){
		String value = getRawServices();
		if (value != null && !value.isEmpty()) {
			String[] parts = value.split("\\" + SERVICES_SEPARATOR);
			return Arrays.asList(parts);
		}
		return Collections.emptyList();
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
