package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.TextTemplateCategoryConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.elexis.core.types.TextTemplateCategory;

@Entity
@Table(name = "CH_ELEXIS_CORE_TEXTTEMPLATE")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
public class TextTemplate extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 80)
	@Convert(converter = TextTemplateCategoryConverter.class)
	protected TextTemplateCategory category;

	@ManyToOne
	@JoinColumn(name = "mandatorId")
	protected Kontakt mandator;

	@Column(length = 255)
	protected String name;

	@Column
	@Lob
	protected String template;

	@Lob
	protected byte[] extInfo;

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

	public TextTemplateCategory getCategory() {
		return category;
	}

	public void setCategory(TextTemplateCategory value) {
		this.category = value;
	}

	public Kontakt getMandator() {
		return mandator;
	}

	public void setMandator(Kontakt mandator) {
		this.mandator = mandator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
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
