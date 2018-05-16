package ch.elexis.core.jpa.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "ETIKETTEN")
public class Sticker extends AbstractDBObjectIdDeleted {

	@Column(length = 25, name = "Image")
	private String image;

	@Column(length = 25)
	private String importance;

	@Column(length = 40, name = "Name")
	private String name;

	@Column(columnDefinition = "CHAR(6)")
	private String foreground;

	@Column(columnDefinition = "CHAR(6)")
	private String background;

	@Column(length = 255)
	private String classes;

	@ElementCollection
	@CollectionTable(name = "ETIKETTEN_OBJCLASS_LINK", joinColumns = @JoinColumn(name = "sticker"))
	private Set<StickerClassLink> stickerClassLinks = new HashSet<>();

	@ElementCollection
	@CollectionTable(name = "ETIKETTEN_OBJECT_LINK", joinColumns = @JoinColumn(name = "etikette"))
	private Set<StickerObjectLink> stickerObjectLinks = new HashSet<>();

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getForeground() {
		return foreground;
	}

	public void setForeground(String foreground) {
		this.foreground = foreground;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getClasses() {
		return classes;
	}

	public void setClasses(String classes) {
		this.classes = classes;
	}

	public Set<StickerClassLink> getStickerClassLinks() {
		return stickerClassLinks;
	}

	public Set<StickerObjectLink> getStickerObjectLinks() {
		return stickerObjectLinks;
	}

	public void setStickerClassLinks(Set<StickerClassLink> stickerClassLinks) {
		this.stickerClassLinks = stickerClassLinks;
	}

	public void setStickerObjectLinks(Set<StickerObjectLink> stickerObjectLinks) {
		this.stickerObjectLinks = stickerObjectLinks;
	}

	@Override
	public String toString() {
		return super.toString() + " name=[" + name + "] foreground=[" + foreground + "] background=[" + background
				+ "] stickerClassLinks=[" + getStickerClassLinks() + "]";
	}

	@Override
	public String getLabel() {
		return getName();
	}
}
