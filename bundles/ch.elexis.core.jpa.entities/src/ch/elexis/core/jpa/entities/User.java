package ch.elexis.core.jpa.entities;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "USER_")
@EntityListeners(EntityWithIdListener.class)
@XmlRootElement(name = "user")
public class User implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Basic(fetch = FetchType.LAZY)
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
	
	@Basic(fetch = FetchType.LAZY)
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
	public byte[] getExtInfo(){
		return extInfo;
	}
	
	@Override
	public void setExtInfo(byte[] extInfo){
		this.extInfo = extInfo;
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
}
