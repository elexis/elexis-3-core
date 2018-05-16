package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.model.ICodeElement;

@Entity
@Table(name = "CH_ELEXIS_ARZTTARIFE_CH_PHYSIO")
public class PhysioLeistung extends AbstractDBObjectIdDeleted implements ICodeElement {
	
	public static final String CODESYSTEM_NAME = "Physiotherapie";

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validUntil;

	@Column(length = 8)
	private String tp;

	@Column(length = 6)
	private String ziffer;

	@Column(length = 255)
	private String titel;

	@Lob
	private String description;

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

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getZiffer() {
		return ziffer;
	}

	public void setZiffer(String ziffer) {
		this.ziffer = ziffer;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getLabel() {
		return getZiffer() + " " + getTitel();
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	@Override
	public String getCode() {
		return getZiffer();
	}

	@Override
	public String getText() {
		return getTitel();
	}
	
	@Override
	public String getCodeSystemCode() {
		return "311";
	}
}
