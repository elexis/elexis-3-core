package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "TARDOC_EXTENSION")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "TardocExtension.code", query = "SELECT te FROM TardocExtension te WHERE te.deleted = false AND te.code = :code")
public class TardocExtension extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	public static final String EXT_FLD_F_AL_R = "F_AL_R";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 32)
	private String code;

	@Lob
	private byte[] limits;

	@Lob
	private String med_interpret;

	@Lob
	private String tech_interpret;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public byte[] getLimits() {
		return limits;
	}

	public void setLimits(byte[] limits) {
		this.limits = limits;
	}

	public String getMed_interpret() {
		return med_interpret;
	}

	public void setMed_interpret(String med_interpret) {
		this.med_interpret = med_interpret;
	}

	public String getTech_interpret() {
		return tech_interpret;
	}

	public void setTech_interpret(String tech_interpret) {
		this.tech_interpret = tech_interpret;
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
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public byte[] getExtInfo() {
		return getLimits();
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		setLimits(extInfo);
	}
}
