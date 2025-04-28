package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "EIGENLEISTUNGEN")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Eigenleistung.code", query = "SELECT ei FROM Eigenleistung ei WHERE ei.deleted = false AND ei.code = :code")
public class Eigenleistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 20)
	private String code;

	@Column(length = 20)
	private String codeclass;

	@Column(length = 80, name = "BEZEICHNUNG")
	private String description;

	@Column(length = 6, name = "EK_PREIS")
	private String basePrice;

	@Column(length = 6, name = "VK_PREIS")
	private String salePrice;

	@Column(length = 4, name = "ZEIT")
	@Convert(converter = IntegerStringConverter.class)
	private int time;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(String basePrice) {
		this.basePrice = basePrice;
	}

	public String getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public String getCodeclass() {
		return codeclass;
	}

	public void setCodeclass(String codeclass) {
		this.codeclass = codeclass;
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
