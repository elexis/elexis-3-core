package ch.elexis.core.jpa.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATION")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Observation.patientid", query = "SELECT ob FROM Observation ob WHERE ob.deleted = false AND ob.patientid = :patientid")
@NamedQuery(name = "Observation.encounterid", query = "SELECT ob FROM Observation ob WHERE ob.deleted = false AND ob.encounterid = :encounterid")
public class Observation extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
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
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean referenced = false;

	@Column(length = 8)
	private String type;

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String encounterid;

	@Column(length = 80)
	private String performerid;

	@Column(length = 255)
	private String originuri;

	@Column(length = 8)
	private String decimalplace;

	@Lob
	private String format;

	@Lob
	private String script;

	@Lob
	private String content;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "target")
	protected List<ObservationLink> targetLinks = new ArrayList<>();
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "source")
	protected List<ObservationLink> sourceLinks = new ArrayList<>();
	
	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	public String getEncounterId() {
		return encounterid;
	}

	public void setEncounterId(String consultationId) {
		this.encounterid = consultationId;
	}

	public String getPerformerId() {
		return performerid;
	}

	public void setPerformerId(String performerId) {
		this.performerid = performerId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOriginuri() {
		return originuri;
	}

	public void setOriginuri(String originuri) {
		this.originuri = originuri;
	}

	public String getDecimalplace() {
		return decimalplace;
	}

	public void setDecimalplace(String decimalplace) {
		this.decimalplace = decimalplace;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public boolean isReferenced() {
		return referenced;
	}

	public void setReferenced(boolean referenced) {
		this.referenced = referenced;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public List<ObservationLink> getSourceLinks(){
		return sourceLinks;
	}
	
	public List<ObservationLink> getTargetLinks(){
		return targetLinks;
	}
}
