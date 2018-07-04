package ch.elexis.core.jpa.entities;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;

@MappedSuperclass
public abstract class AbstractDBObjectIdDeleted extends AbstractDBObjectId {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "object", insertable = false, updatable = false)
	@MapKey(name = "domain")
	protected Map<String, Xid> xids;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
		// TODO if true, remove all Xids
	}

	public Map<String, Xid> getXids() {
		return xids;
	}

	public void setXids(Map<String, Xid> xids) {
		this.xids = xids;
	}

	@Override
	public String toString() {
		return super.toString() + (isDeleted() ? " D " : "   ") + "id=[" + String.format("%25s", getId()) + "]";
	}
	
	public String getLabel() {
		return toString();
	}
}
