package ch.elexis.core.jpa.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserconfigId implements Serializable {

	private static final long serialVersionUID = -654453520781303717L;

	private String ownerId;
	private String param;

	public UserconfigId() {
	}

	public UserconfigId(final String ownerId, final String param) {
		this.ownerId = ownerId;
		this.param = param;
	}

	public String getOwner() {
		return ownerId;
	}

	public String getParam() {
		return param;
	}

	public void setOwner(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setParam(String param) {
		this.param = param;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ownerId, param);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserconfigId other = (UserconfigId) obj;
		return Objects.equals(ownerId, other.ownerId) && Objects.equals(param, other.param);
	}

}
