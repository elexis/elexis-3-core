package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_PS25")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Ps25Leistung.code", query = "SELECT pl FROM Ps25Leistung pl WHERE pl.deleted = false AND pl.code = :code")
@NamedQuery(name = "Ps25Leistung.code.validFrom", query = "SELECT pl FROM Ps25Leistung pl WHERE pl.deleted = false AND pl.code = :code AND pl.validFrom = :validFrom")
public class Ps25Leistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "PS25";
	public static final String CODESYSTEM_CODE = "744";

	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 32)
	private String code;

	@Column(length = 255)
	private String honorarEmpfaenger;

	@Column(length = 255)
	private String fachgebietKapitel;

	@Column(length = 255)
	private String unterKapitel;

	@Column(length = 512)
	private String mehrleistungBei;

	@Lob
	private String spezifikation;

	@Lob
	private String anwendungsRegeln;

	@Column(length = 16)
	private String taxpunkte;

	@Column(length = 16)
	private String stufe;

	@Column(length = 512)
	private String moeglicheKombination;

	@Column(length = 16)
	private String mehrleistungsTyp;

	@Column(length = 255)
	private String mehrleistung;

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validUntil;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getHonorarEmpfaenger() {
		return honorarEmpfaenger;
	}

	public void setHonorarEmpfaenger(String honorarEmpfaenger) {
		this.honorarEmpfaenger = honorarEmpfaenger;
	}

	public String getFachgebietKapitel() {
		return fachgebietKapitel;
	}

	public void setFachgebietKapitel(String fachgebietKapitel) {
		this.fachgebietKapitel = fachgebietKapitel;
	}

	public String getUnterkapitel() {
		return unterKapitel;
	}

	public void setUnterkapitel(String unterkapitel) {
		this.unterKapitel = unterkapitel;
	}

	public String getMehrleistungBei() {
		return mehrleistungBei;
	}

	public void setMehrleistungBei(String mehrleistungBei) {
		this.mehrleistungBei = mehrleistungBei;
	}

	public String getSpezifikation() {
		return spezifikation;
	}

	public void setSpezifikation(String spezifikation) {
		this.spezifikation = spezifikation;
	}

	public String getAnwendungsregeln() {
		return anwendungsRegeln;
	}

	public void setAnwendungsregeln(String anwendungsregeln) {
		this.anwendungsRegeln = anwendungsregeln;
	}

	public String getTaxpunkte() {
		return taxpunkte;
	}

	public void setTaxpunkte(String taxpunkte) {
		this.taxpunkte = taxpunkte;
	}

	public String getStufe() {
		return stufe;
	}

	public void setStufe(String stufe) {
		this.stufe = stufe;
	}

	public String getMoeglicheKombination() {
		return moeglicheKombination;
	}

	public void setMoeglicheKombination(String moeglicheKombination) {
		this.moeglicheKombination = moeglicheKombination;
	}

	public String getMehrleistungstyp() {
		return mehrleistungsTyp;
	}

	public void setMehrleistungstyp(String mehrleistungstyp) {
		this.mehrleistungsTyp = mehrleistungstyp;
	}

	public String getMehrleistung() {
		return mehrleistung;
	}

	public void setMehrleistung(String mehrleistung) {
		this.mehrleistung = mehrleistung;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(LocalDate validUntil) {
		this.validUntil = validUntil;
	}

	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	public String getText() {
		return getMehrleistung();
	}

	public String getCodeSystemCode() {
		return CODESYSTEM_CODE;
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
