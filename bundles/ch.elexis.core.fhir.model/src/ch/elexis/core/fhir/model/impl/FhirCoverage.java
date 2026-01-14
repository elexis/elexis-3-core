package ch.elexis.core.fhir.model.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.fhir.model.FhirModelServiceHolder;
import ch.elexis.core.fhir.model.interfaces.IFhirCoverage;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class FhirCoverage extends AbstractFhirModelAdapter<ICoverage, Coverage> implements ICoverage, IFhirCoverage {

	public FhirCoverage(Coverage fhirResource) {
		super(fhirResource);
	}

	@Override
	public Class<Coverage> getFhirType() {
		return Coverage.class;
	}

	@Override
	public Class<?> getModelType() {
		return ICoverage.class;
	}

	@Override
	public Optional<IEncounter> getLatestEncounter() {
		IQuery<IBaseBundle> query = FhirModelServiceHolder.get().getQuery("Encounter?coverage=" + getId());
		query.count(1);
		query.sort(new SortSpec("date", SortOrderEnum.DESC));
		Bundle results = query.returnBundle(Bundle.class).execute();
		if (results != null && results.hasEntry()) {
			String id = results.getEntry().get(0).getResource().getIdPart();
			System.out.println("getLatestEncounter()->" + id);
			return CoreModelServiceHolder.get().load(id, IEncounter.class);
		}
		return Optional.empty();
	}

	@Override
	public List<IEncounter> getEncounters() {
		IQuery<IBaseBundle> query = FhirModelServiceHolder.get().getQuery("Encounter?coverage=" + getId());
		System.out.println("IM HERE");
		return null;
	}

	@Override
	public boolean isDeleted() {
		return getLoaded().isDeleted();
	}

	@Override
	public void setDeleted(boolean value) {
		getLoadedMarkDirty().setDeleted(value);
	}

	@Override
	public IPatient getPatient() {
		return getLoaded().getPatient();
	}

	@Override
	public void setPatient(IPatient value) {
		if (getPatient() != null) {
			addRefresh(getPatient());
		}
		if (value != null) {
			getLoadedMarkDirty().setPatient(value);
			addRefresh(value);
		} else {
			getLoadedMarkDirty().setPatient(null);
		}
	}

	@Override
	public String getDescription() {
		return getLoaded().getDescription();
	}

	@Override
	public void setDescription(String value) {
		getLoadedMarkDirty().setDescription(value);
	}

	@Override
	public String getReason() {
		return getLoaded().getReason();
	}

	@Override
	public void setReason(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalDate getDateFrom() {
		return getLoaded().getDateFrom();
	}

	@Override
	public void setDateFrom(LocalDate value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBillingSystem getBillingSystem() {
		return getLoaded().getBillingSystem();
	}

	@Override
	public void setBillingSystem(IBillingSystem value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IContact getGuarantor() {
		return getLoaded().getGuarantor();
	}

	@Override
	public void setGuarantor(IContact value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IContact getCostBearer() {
		return getLoaded().getCostBearer();
	}

	@Override
	public void setCostBearer(IContact value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getInsuranceNumber() {
		return getLoaded().getInsuranceNumber();
	}

	@Override
	public void setInsuranceNumber(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalDate getDateTo() {
		return getLoaded().getDateTo();
	}

	@Override
	public void setDateTo(LocalDate value) {
		// TODO Auto-generated method stub

	}

	@Override
	public LocalDate getBillingProposalDate() {
		return getLoaded().getBillingProposalDate();
	}

	@Override
	public void setBillingProposalDate(LocalDate value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		return getNarrativeTags().contains("open");
	}

}
