package ch.elexis.core.jpa.entities;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.rgw.tools.TimeTool;

@Entity
@Table(name = "TARMED_GROUP")
public class TarmedGroup extends AbstractDBObjectIdDeleted {
	
	public static final Object SERVICES_SEPARATOR = "|";

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

}
