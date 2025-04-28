package ch.elexis.core.jpa.entities;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "default_signatures")
@EntityListeners(EntityWithIdListener.class)
public class DefaultSignature extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Lob
	protected byte[] extInfo;

	@Column(length = 10)
	private String atccode;

	@Column(length = 255)
	private String article;

	@Column(length = 255)
	private String type;

	@Column(length = 10)
	private String morning;

	@Column(length = 10)
	private String noon;

	@Column(length = 10)
	private String evening;

	@Column(length = 10)
	private String night;

	@Lob()
	private String comment;

	public String getAtccode() {
		return atccode;
	}

	public void setAtccode(String atccode) {
		this.atccode = atccode;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMorning() {
		return morning;
	}

	public void setMorning(String morning) {
		this.morning = morning;
	}

	public String getNoon() {
		return noon;
	}

	public void setNoon(String noon) {
		this.noon = noon;
	}

	public String getEvening() {
		return evening;
	}

	public void setEvening(String evening) {
		this.evening = evening;
	}

	public String getNight() {
		return night;
	}

	public void setNight(String night) {
		this.night = night;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public byte[] getExtInfo() {
		return extInfo;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
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
