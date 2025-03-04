package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_PANDEMIC")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "PandemieLeistung.code", query = "SELECT pl FROM PandemieLeistung pl WHERE pl.deleted = false AND pl.code = :code")
@NamedQuery(name = "PandemieLeistung.pandemic.code.validFrom", query = "SELECT pl FROM PandemieLeistung pl WHERE pl.deleted = false AND pl.pandemic = :pandemic AND pl.code = :code AND pl.validFrom = :validFrom")
public class PandemieLeistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "Pandemie";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 25)
	private String code;

	@Column(length = 25)
	private String pandemic;

	@Column(length = 255)
	private String chapter;

	@Column(length = 255)
	private String title;

	@Column(length = 255)
	private String org;

	@Column(length = 255)
	private String rules;

	@Column(length = 25)
	@Convert(converter = IntegerStringConverter.class)
	private int taxpoints;

	@Column(length = 25)
	@Convert(converter = IntegerStringConverter.class)
	private int cents;

	@Column
	@Lob
	private String description;

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validUntil;

	public int getCents() {
		return cents;
	}

	public void setCents(int cents) {
		this.cents = cents;
	}

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validUntil;
	}

	public void setValidTo(LocalDate validTo) {
		this.validUntil = validTo;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getPandemic() {
		return pandemic;
	}

	public void setPandemic(String pandemic) {
		this.pandemic = pandemic;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getRules() {
		return rules;
	}

	public void setRules(String rules) {
		this.rules = rules;
	}

	public int getTaxpoints() {
		return taxpoints;
	}

	public void setTaxpoints(int taxpoints) {
		this.taxpoints = taxpoints;
	}
}
