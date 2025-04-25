package ch.elexis.core.jpa.entities;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "USER_")
@EntityListeners(EntityWithIdListener.class)
@XmlRootElement(name = "user")
@Cache(expiry = 15000)
@NamedQuery(name = "User.kontakt", query = "SELECT u FROM User u WHERE u.deleted = false AND u.kontakt = :kontakt")
public class User extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

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

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "KONTAKT_ID")
	protected Kontakt kontakt;

	@Column(length = 64, name = "HASHED_PASSWORD")
	protected String hashedPassword;

	@Column(length = 64)
	protected String salt;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	@Column(name = "is_active")
	protected boolean active;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	@Column(name = "is_administrator")
	protected boolean administrator;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	@Column(name = "allow_external")
	protected boolean allowExternal;

	@Lob()
	protected String keystore;

	@Column(length = 16)
	protected String totp;

	@ManyToMany
	@JoinTable(name = "USER_ROLE_JOINT", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "ID"))
	protected Collection<Role> roles = new HashSet<>();

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}

	public boolean isAllowExternal() {
		return allowExternal;
	}

	public void setAllowExternal(boolean allowExternal) {
		this.allowExternal = allowExternal;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public String getTotp() {
		return totp;
	}

	public void setTotp(String totp) {
		this.totp = totp;
	}

	public Collection<Role> getRoles() {
		return roles;
	}

	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
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
