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
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_BERCHTOLD_PRIVATRECHNUNG")
@EntityListeners(EntityWithIdListener.class)
@NamedQueries({
		@NamedQuery(name = "BerchtoldPrivatLeistung.code", query = "SELECT pl FROM BerchtoldPrivatLeistung pl WHERE pl.deleted = false AND pl.shortName = :code"),
		@NamedQuery(name = "BerchtoldPrivatLeistung.parent", query = "SELECT pl FROM BerchtoldPrivatLeistung pl WHERE pl.deleted = false AND pl.parent = :parent ORDER BY pl.shortName ASC") })

public class BerchtoldPrivatLeistung extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 80)
	private String parent;

	@Column(length = 499)
	private String name;

	@Column(length = 80, name = "short")
	private String shortName;

	@Column(length = 25)
	private String subsystem;

	@Column(length = 8, name = "VALID_FROM")
	private LocalDate validFrom;

	@Column(length = 8, name = "VALID_UNTIL")
	private LocalDate validTo;

	@Column(length = 8)
	@Convert(converter = IntegerStringConverter.class)
	private int cost;

	@Column(length = 8)
	@Convert(converter = IntegerStringConverter.class)
	private int price;

	@Column
	@Convert(converter = IntegerStringConverter.class)
	private int time;

	@Lob
	protected byte[] extInfo;

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
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

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
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

	@Override
	public byte[] getExtInfo() {
		return extInfo;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
	}
}
