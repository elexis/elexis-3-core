package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

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
	@Column(unique = true, nullable = false, length = 64)
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
	private String unterkapitel;

	@Column(length = 512)
	private String fachaerztlicheMehrleistungBei;

	@Lob
	private String spezifikation;

	@Lob
	private String anwendungsregeln;

	@Column(length = 16)
	private String taxpunkte;

	@Column(length = 16)
	private String stufe;

	@Column(length = 512)
	private String moeglicheKombination;

	@Column(length = 16)
	private String mehrleistungstyp;

	@Column(length = 512)
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
		return unterkapitel;
	}

	public void setUnterkapitel(String unterkapitel) {
		this.unterkapitel = unterkapitel;
	}

	public String getFachaerztlicheMehrleistungBei() {
		return fachaerztlicheMehrleistungBei;
	}

	public void setFachaerztlicheMehrleistungBei(String fachaerztlicheMehrleistungBei) {
		this.fachaerztlicheMehrleistungBei = fachaerztlicheMehrleistungBei;
	}

	public String getSpezifikation() {
		return spezifikation;
	}

	public void setSpezifikation(String spezifikation) {
		this.spezifikation = spezifikation;
	}

	public String getAnwendungsregeln() {
		return anwendungsregeln;
	}

	public void setAnwendungsregeln(String anwendungsregeln) {
		this.anwendungsregeln = anwendungsregeln;
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
		return mehrleistungstyp;
	}

	public void setMehrleistungstyp(String mehrleistungstyp) {
		this.mehrleistungstyp = mehrleistungstyp;
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
