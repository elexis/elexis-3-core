package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.rgw.tools.TimeTool;

@Entity
@Table(name = "TARMED_KUMULATION")
public class TarmedKumulation extends AbstractDBObjectIdDeleted {

	public static final String TYP_EXCLUSION = "E";
	public static final String TYP_INCLUSION = "I";
	public static final String TYP_EXCLUSIVE = "X";

	@Column(length = 25)
	private String masterCode;

	@Column(length = 1)
	private String masterArt;

	@Column(length = 25)
	private String slaveCode;

	@Column(length = 1)
	private String slaveArt;

	@Column(length = 1)
	private String typ;

	@Column(length = 1)
	private String view;

	@Column(length = 1)
	private String validSide;

	@Column(length = 8)
	private LocalDate validFrom;

	@Column(length = 8)
	private LocalDate validTo;
	
	@Column(length = 3)
	private String law;

	public String getMasterCode() {
		return masterCode;
	}

	public void setMasterCode(String masterCode) {
		this.masterCode = masterCode;
	}

	public String getMasterArt() {
		return masterArt;
	}

	public void setMasterArt(String masterArt) {
		this.masterArt = masterArt;
	}

	public String getSlaveCode() {
		return slaveCode;
	}

	public void setSlaveCode(String slaveCode) {
		this.slaveCode = slaveCode;
	}

	public String getSlaveArt() {
		return slaveArt;
	}

	public void setSlaveArt(String slaveArt) {
		this.slaveArt = slaveArt;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getValidSide() {
		return validSide;
	}

	public void setValidSide(String validSide) {
		this.validSide = validSide;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public String getLaw() {
		return law;
	}
	
	public void setLaw(String law) {
		this.law = law;
	}
	
	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * Checks if the kumulation is still/already valid on the given date
	 * 
	 * @param date
	 *            on which it should be valid
	 * @return true if valid, false otherwise
	 */
	@Transient
	public boolean isValidKumulation(TimeTool date){
		TimeTool from = new TimeTool(getValidFrom());
		TimeTool to = new TimeTool(getValidTo());
		
		if (date.isAfterOrEqual(from) && date.isBeforeOrEqual(to)) {
			return true;
		}
		return false;
	}
}
