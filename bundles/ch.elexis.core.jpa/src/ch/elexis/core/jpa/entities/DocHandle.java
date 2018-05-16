package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.constants.QueryConstants;

@Entity
@Table(name = "CH_ELEXIS_OMNIVORE_DATA")
@NamedQueries({
	@NamedQuery(name = QueryConstants.QUERY_DOCHANDLE_determineLength, query = "SELECT LENGTH(dh.doc) FROM DocHandle dh WHERE dh.id = :"
		+ QueryConstants.PARAM_ID)
})
public class DocHandle extends AbstractDBObjectIdDeleted {

	public static final String CATEGORY_CATEGORY = "text/category";

	@OneToOne
	@JoinColumn(name = "PatID")
	protected Kontakt kontakt;

	@Column(length = 8)
	protected LocalDate datum;

	@Column(length = 8)
	protected LocalDate creationDate;

	@Column(length = 80)
	protected String category;

	@Column(length = 255)
	protected String title;

	@Column(length = 255)
	protected String mimetype;

	@Column(length = 512)
	protected String keywords;

	@Column(length = 255)
	protected String path;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	protected byte[] doc;

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] getDoc() {
		return doc;
	}

	public void setDoc(byte[] doc) {
		this.doc = doc;
	}

	@Override
	public String getLabel() {
		return getDatum() + " - " + getTitle();
	}

	@Override
	public String toString() {
		return super.toString() + "title=[" + getTitle() + "] category=[" + getCategory() + "] datum=[" + getDatum()
				+ "]";
	}
}
