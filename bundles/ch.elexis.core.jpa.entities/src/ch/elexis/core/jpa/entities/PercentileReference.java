package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "net_medshare_percentile_refdata")
@EntityListeners(EntityWithIdListener.class)
public class PercentileReference extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String SEX_BOY = "m"; //$NON-NLS-1$
	public static final String SEX_GIRL = "f"; //$NON-NLS-1$

	public static final String TYPE_WEIGHT = "WEIGHT"; //$NON-NLS-1$
	public static final String TYPE_LENGTH = "LENGTH"; //$NON-NLS-1$
	public static final String TYPE_HEAD = "HEAD"; //$NON-NLS-1$
	public static final String TYPE_BMI = "BMI"; //$NON-NLS-1$
	public static final String TYPE_GROWTH = "GROWTH"; //$NON-NLS-1$

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 1)
	private String sex;

	@Column(length = 10)
	private String type;

	@Column(length = 8)
	private String age;

	@Column(length = 8)
	private String value_3;

	@Column(length = 8)
	private String value_10;

	@Column(length = 8)
	private String value_25;

	@Column(length = 8)
	private String value_50;

	@Column(length = 8)
	private String value_75;

	@Column(length = 8)
	private String value_90;

	@Column(length = 8)
	private String value_97;

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

	public String getAge() {
		return age;
	}

	public String getSex() {
		return sex;
	}

	public String getType() {
		return type;
	}

	public String getValue_3() {
		return value_3;
	}

	public String getValue_10() {
		return value_10;
	}

	public String getValue_25() {
		return value_25;
	}

	public String getValue_50() {
		return value_50;
	}

	public String getValue_75() {
		return value_75;
	}

	public String getValue_90() {
		return value_90;
	}

	public String getValue_97() {
		return value_97;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue_3(String value_3) {
		this.value_3 = value_3;
	}

	public void setValue_10(String value_10) {
		this.value_10 = value_10;
	}

	public void setValue_25(String value_25) {
		this.value_25 = value_25;
	}

	public void setValue_50(String value_50) {
		this.value_50 = value_50;
	}

	public void setValue_75(String value_75) {
		this.value_75 = value_75;
	}

	public void setValue_90(String value_90) {
		this.value_90 = value_90;
	}

	public void setValue_97(String value_97) {
		this.value_97 = value_97;
	}
}
