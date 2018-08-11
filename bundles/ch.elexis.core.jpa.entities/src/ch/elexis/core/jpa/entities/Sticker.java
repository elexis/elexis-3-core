package ch.elexis.core.jpa.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "ETIKETTEN")
@EntityListeners(EntityWithIdListener.class)
public class Sticker implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
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
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
	
	@Override
	public int hashCode(){
		return EntityWithId.idHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj){
		return EntityWithId.idEquals(this, obj);
	}
}
