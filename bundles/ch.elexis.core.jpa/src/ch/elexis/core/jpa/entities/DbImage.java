package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "DBIMAGE")
public class DbImage extends AbstractDBObjectIdDeleted {

	@Column(name = "datum")
	private LocalDate date = LocalDate.now();

	@Column(length = 80)
	private String prefix;

	@Column(length = 80)
	private String title;

	@Lob
	@Column(name = "bild")
	private byte[] image;

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	@Override
	public String getLabel() {
		return getDate() + " - " + getTitle() + " (" + getPrefix() + ")";
	}

	@Override
	public String toString() {
		return super.toString() + "date=[" + getDate() + "] title=[" + getTitle() + "] prefix=[" + getPrefix() + "]";
	}
}
