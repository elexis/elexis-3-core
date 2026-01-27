package ch.elexis.core.fhir.model.dto;

import java.time.LocalDate;
import java.util.List;

import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;

public class ICoverageDto extends IdentifiableDeletableDto implements ICoverage {

	String insuranceNumber;
	IBillingSystem billingSystem;
	IContact guarantor;
	LocalDate dateTo;
	LocalDate dateFrom;
	String reason;
	LocalDate billingProposalDate;
	String description;
	IContact costBearer;
	IPatient patient;
	boolean open;

	@Override
	public List<IEncounter> getEncounters() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInsuranceNumber() {
		return insuranceNumber;
	}

	public void setInsuranceNumber(String insuranceNumber) {
		this.insuranceNumber = insuranceNumber;
	}

	public IBillingSystem getBillingSystem() {
		return billingSystem;
	}

	public void setBillingSystem(IBillingSystem billingSystem) {
		this.billingSystem = billingSystem;
	}

	public IContact getGuarantor() {
		return guarantor;
	}

	public void setGuarantor(IContact guarantor) {
		this.guarantor = guarantor;
	}

	public LocalDate getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo) {
		this.dateTo = dateTo;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDate getBillingProposalDate() {
		return billingProposalDate;
	}

	public void setBillingProposalDate(LocalDate billingProposalDate) {
		this.billingProposalDate = billingProposalDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IContact getCostBearer() {
		return costBearer;
	}

	public void setCostBearer(IContact costBearer) {
		this.costBearer = costBearer;
	}

	public IPatient getPatient() {
		return patient;
	}

	public void setPatient(IPatient patient) {
		this.patient = patient;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

}
