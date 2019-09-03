package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_COMPLEMENTARY")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "ComplementaryLeistung.code", query = "SELECT cl FROM ComplementaryLeistung cl WHERE cl.code = :code")
public class ComplementaryLeistung extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted {
	
	public static final String CODESYSTEM_NAME = "Komplementärmedizin";
	
	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Column(length = 255)
	private String chapter;
	
	@Column(length = 16)
	private String code;
	
	@Column(length = 255)
	private String codeText;
	
	@Column
	@Lob
	private String description;
	
	@Column(length = 16)
	@Convert(converter = IntegerStringConverter.class)
	private int fixedValue;
	
	@Column(length = 8)
	private LocalDate validFrom;
	
	@Column(length = 8)
	private LocalDate validTo;
	
	public int getFixedValue(){
		return fixedValue;
	}
	
	public void setFixedValue(int fixedValue){
		this.fixedValue = fixedValue;
	}
	
	public String getChapter(){
		return chapter;
	}
	
	public void setChapter(String chapter){
		this.chapter = chapter;
	}
	
	public LocalDate getValidFrom(){
		return validFrom;
	}
	
	public void setValidFrom(LocalDate validFrom){
		this.validFrom = validFrom;
	}
	
	public LocalDate getValidTo(){
		return validTo;
	}
	
	public void setValidTo(LocalDate validTo){
		this.validTo = validTo;
	}
	
	public String getCode(){
		return code;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getCodeText(){
		return codeText;
	}
	
	public void setCodeText(String codeText){
		this.codeText = codeText;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
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
