package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.converter.ArticleTypConverter;
import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.types.ArticleTyp;

@Entity
@Table(name = "artikel")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Artikel.typ.code", query = "SELECT ar FROM Artikel ar WHERE ar.deleted = false AND ar.subId = :code AND ar.typ = :typ")
@NamedQuery(name = "Artikel.typ.id", query = "SELECT ar FROM Artikel ar WHERE ar.deleted = false AND ar.id = :id AND ar.typ = :typ")
public class Artikel implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	public static final String CODESYSTEM_NAME = "Artikel";

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob
	protected byte[] extInfo;
	
	@Column(length = 15)
	private String ean;

	@Column(length = 20, name = "SubID")
	private String subId;

	@Column(length = 80)
	private String klasse;

	@Column(length = 127)
	private String name;

	@Column(length = 127, name = "Name_intern")
	private String nameIntern;

	@Column(length = 8, name = "EK_Preis")
	private String ekPreis;

	/**
	 * user-defined prices are stored as negative value, hence Math.abs should be
	 * applied for billing
	 */
	@Column(length = 8, name = "VK_Preis")
	private String vkPreis;

	@Column(length = 15)
	@Convert(converter = ArticleTypConverter.class)
	private ArticleTyp Typ;

	@Column(length = 10)
	private String codeclass;

	@Column(length = 25)
	private String extId;

	@Column(length = 8)
	private String lastImport;

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validTo;

	@Column(length = 255, name = "ATC_code")
	private String atcCode;

	@Override
	public String toString() {
		return super.toString() + "name=[" + getName() + "]";
	}

	public String getLabel() {
		String ret = getNameIntern();
		if (StringUtils.isEmpty(ret)) {
			ret = getName();
		}
		return ret;
	}

	public String getEan() {
		return ean;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public String getSubId() {
		return subId;
	}

	public void setSubId(String subId) {
		this.subId = subId;
	}

	public String getKlasse() {
		return klasse;
	}

	public void setKlasse(String klasse) {
		this.klasse = klasse;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameIntern() {
		return nameIntern;
	}

	public void setNameIntern(String nameIntern) {
		this.nameIntern = nameIntern;
	}

	public String getEkPreis() {
		return ekPreis;
	}

	public void setEkPreis(String ekPreis) {
		this.ekPreis = ekPreis;
	}

	public String getVkPreis() {
		return vkPreis;
	}

	public void setVkPreis(String vkPreis) {
		this.vkPreis = vkPreis;
	}

	public ArticleTyp getTyp(){
		return Typ;
	}

	public void setTyp(ArticleTyp typ){
		Typ = typ;
	}

	public String getCodeclass() {
		return codeclass;
	}

	public void setCodeclass(String codeclass) {
		this.codeclass = codeclass;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getLastImport() {
		return lastImport;
	}

	public void setLastImport(String lastImport) {
		this.lastImport = lastImport;
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

	public String getAtcCode() {
		return atcCode;
	}

	public void setAtcCode(String atcCode) {
		this.atcCode = atcCode;
	}
	
	@Override
	public byte[] getExtInfo(){
		return extInfo;
	}
	
	@Override
	public void setExtInfo(byte[] extInfo){
		this.extInfo = extInfo;
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
}
