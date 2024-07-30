package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "at_medevit_elexis_impfplan")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class Vaccination extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Lob
	protected byte[] extInfo;

	@OneToOne
	@JoinColumn(name = "Patient_ID")
	private Kontakt patient;

	@Column(length = 255, name = "Artikel_REF")
	private String article;

	@Column(length = 255, name = "BusinessName")
	private String articleName;

	@Column(length = 13, name = "ean")
	private String articleGtin;

	@Column(length = 20, name = "ATCCode")
	private String articleAtc;

	@Column(length = 255, name = "lotnr")
	private String lotNumber;

	@Column(length = 8, name = "dateOfAdministration")
	private LocalDate dateOfAdministration;

	@Column(length = 255, name = "administrator")
	private String performer;

	@Column(length = 255, name = "vaccAgainst")
	private String ingredientsAtc;

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public String getArticleName() {
		return articleName;
	}

	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}

	public String getArticleAtc() {
		return articleAtc;
	}

	public void setArticleAtc(String articleAtc) {
		this.articleAtc = articleAtc;
	}

	public String getArticleGtin() {
		return articleGtin;
	}

	public void setArticleGtin(String articleGtin) {
		this.articleGtin = articleGtin;
	}

	public String getLotNumber() {
		return lotNumber;
	}

	public void setLotNumber(String lotNumber) {
		this.lotNumber = lotNumber;
	}

	public LocalDate getDateOfAdministration() {
		return dateOfAdministration;
	}

	public void setDateOfAdministration(LocalDate dateOfAdministration) {
		this.dateOfAdministration = dateOfAdministration;
	}

	public String getPerformer() {
		return performer;
	}

	public void setPerformer(String performer) {
		this.performer = performer;
	}

	public String getIngredientsAtc() {
		return ingredientsAtc;
	}

	public void setIngredientsAtc(String ingredientsAtc) {
		this.ingredientsAtc = ingredientsAtc;
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
