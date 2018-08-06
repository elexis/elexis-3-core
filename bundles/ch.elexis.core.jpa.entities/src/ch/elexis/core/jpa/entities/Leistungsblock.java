package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.ElexisDBCompressedStringConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "leistungsblock")
@EntityListeners(EntityWithIdListener.class)
public class Leistungsblock implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "Block";

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@OneToOne
	@JoinColumn(name = "MandantId")
	private Kontakt mandator;

	@Column(length = 30)
	private String name;

	@Convert(converter = ElexisDBCompressedStringConverter.class)
	@Column(name = "leistungen", columnDefinition = "BLOB")
	private String services;

	@Column(length = 30)
	private String macro;

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

	public String getMacro() {
		return macro;
	}

	public void setMacro(String macro) {
		this.macro = macro;
	}

	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	public String getCode() {
		return getName();
	}

	public String getText() {
		return getName();
	}

	public String getServices(){
		return services;
	}
	
	public void setServices(String services){
		this.services = services;
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
