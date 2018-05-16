package ch.elexis.core.jpa.entities;

import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;

@MappedSuperclass
public abstract class AbstractDBObjectIdDeletedExtInfo extends AbstractDBObjectIdDeleted {

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisExtInfoMapConverter")
	@Column(columnDefinition = "BLOB")
	protected Map<Object, Object> extInfo = new Hashtable<Object, Object>();

	protected Map<Object, Object> getExtInfo() {
		return extInfo;
	}

	private void setExtInfo(Map<Object, Object> extInfo) {
		this.extInfo = extInfo;
	}

	/**
	 * 
	 * @param key
	 *            a non-null key value
	 * @param value
	 *            the value to store, or <code>null</code> to remove the
	 *            respective key
	 */
	@Transient
	public void setExtInfoValue(Object key, Object value) {
		if (key == null) {
			return;
		}
		// we have to create a new object on change
		// otherwise JPA won't pick-up the change
		Hashtable<Object, Object> ht = new Hashtable<Object, Object>(getExtInfo());
		if (value != null) {
			ht.put(key, value);
		} else {
			ht.remove(key);
		}
		setExtInfo(ht);
	}

	@Transient
	public String getExtInfoAsString(Object key) {
		return (String) getExtInfo().get(key);
	}
}
