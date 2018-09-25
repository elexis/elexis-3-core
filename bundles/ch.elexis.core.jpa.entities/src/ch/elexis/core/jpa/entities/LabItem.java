package ch.elexis.core.jpa.entities;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.converter.LabItemTypConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.StringTool;

@Entity
@Table(name = "laboritems")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "LabItem.code.name", query = "SELECT li FROM LabItem li WHERE li.deleted = false AND li.code = :code AND li.name = :name")
@NamedQuery(name = "LabItem.code.name.typ", query = "SELECT li FROM LabItem li WHERE li.deleted = false AND li.code = :code AND li.name = :name AND li.typ = :typ")
public class LabItem implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Column(name = "kuerzel", length = 80)
	private String code;

	@Column(name = "titel", length = 80)
	private String name;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "laborID")
	private Kontakt labor;

	@Column(name = "RefMann", length = 256)
	private String referenceMale;

	@Column(name = "RefFrauOrTx", length = 256)
	private String referenceFemale;

	@Column(name = "einheit", length = 20)
	private String unit;

	@Column(name = "typ", length = 1)
	@Convert(converter = LabItemTypConverter.class)
	private LabItemTyp typ;

	@Column(name = "Gruppe", length = 25)
	private String group;

	@Column(name = "prio", length = 3)
	private String priority;

	@Column(length = 128)
	private String billingCode;

	@Column(length = 100)
	private String export;

	@Column(length = 128)
	private String loinccode;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	private boolean visible = true;

	@Convert(converter = IntegerStringConverter.class)
	private int digits;

	@Column(length = 255)
	private String formula;
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "labitemid")
	protected Collection<LabMapping> mappings = new HashSet<>();

	/**
	 * @return the variable name of this LabItem as used in LabItems of type
	 *         formula for cross-reference
	 */
	@Transient
	public String getVariableName() {
		String group = getGroup();
		if (group != null && group.contains(StringTool.space)) {
			String[] g = group.split(StringTool.space, 2);
			String prio = getPriority();
			String num = (prio != null) ? prio.trim() : "9999";
			return g[0] + "_" + num;
		}

		return "ERROR";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Kontakt getLabor() {
		return labor;
	}

	public void setLabor(Kontakt labor) {
		this.labor = labor;
	}

	public String getReferenceMale() {
		return referenceMale;
	}

	public void setReferenceMale(String referenceMale) {
		this.referenceMale = referenceMale;
	}

	public String getReferenceFemale() {
		return referenceFemale;
	}

	public void setReferenceFemale(String referenceFemale) {
		this.referenceFemale = referenceFemale;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public LabItemTyp getTyp(){
		return typ;
	}

	public void setTyp(LabItemTyp type){
		this.typ = type;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getBillingCode() {
		return billingCode;
	}

	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}

	public String getExport() {
		return export;
	}

	public void setExport(String export) {
		this.export = export;
	}

	public String getLoinccode() {
		return loinccode;
	}

	public void setLoinccode(String loinccode) {
		this.loinccode = loinccode;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public int getDigits() {
		return digits;
	}

	public void setDigits(int digits) {
		this.digits = digits;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	public Collection<LabMapping> getMappings(){
		return mappings;
	}
	
	public void setMappings(Collection<LabMapping> mappings){
		this.mappings = mappings;
	}
}
