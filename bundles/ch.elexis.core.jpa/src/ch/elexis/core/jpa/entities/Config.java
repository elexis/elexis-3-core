package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

@Entity
@Table(name = "config")
@Cache(type = CacheType.NONE)
public class Config extends AbstractDBObject {

	@Id
	@Column(unique = true, nullable = false, length = 80)
	private String param;

	@Lob
	private String wert;

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getWert() {
		return wert;
	}

	public void setWert(String wert) {
		this.wert = wert;
	}

	@Override
	public String toString() {
		return super.toString() + "param=[" + getParam() + "] wert=[" + getWert() + "]";
	}
}
