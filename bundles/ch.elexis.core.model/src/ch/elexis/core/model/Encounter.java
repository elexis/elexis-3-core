package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Diagnosis;
import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.service.holder.ContextServiceHolder;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.rgw.tools.VersionedResource;

public class Encounter extends AbstractIdDeleteModelAdapter<Behandlung>
		implements IdentifiableWithXid, IEncounter {

	public Encounter(Behandlung entity){
		super(entity);
	}

	@Override
	public LocalDateTime getTimeStamp(){
		// TODO looses information
		return getEntity().getDatum().atStartOfDay();
	}

	@Override
	public void setTimeStamp(LocalDateTime value){
		// TODO looses information
		getEntityMarkDirty().setDatum(value.toLocalDate());
	}

	@Override
	public IPatient getPatient(){
		if (getEntity().getFall() != null) {
			return ModelUtil.getAdapter(getEntity().getFall().getPatient(), IPatient.class);
		}
		return null;
	}

	@Override
	public ICoverage getCoverage(){
		return ModelUtil.getAdapter(getEntity().getFall(), ICoverage.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCoverage(ICoverage value){
		if (value != null) {
			// remove from existing
			if (getCoverage() != null) {
				Fall oldEntity =
					((AbstractIdModelAdapter<Fall>) getCoverage()).getEntityMarkDirty();
				oldEntity.getConsultations().remove(getEntity());
				addChanged(getCoverage());
			}
			Fall valueEntity = ((AbstractIdModelAdapter<Fall>) value).getEntityMarkDirty();
			// set both sides
			getEntityMarkDirty().setFall(valueEntity);
			valueEntity.getConsultations().add(getEntity());
			addChanged(value);
		}
	}

	@Override
	public IMandator getMandator(){
		return ModelUtil.getAdapter(getEntity().getMandant(), IMandator.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setMandator(IMandator value){
		if (value != null) {
			getEntityMarkDirty().setMandant(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setMandant(null);
		}
	}

	@Override
	public List<IBilled> getBilled(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getBilled().parallelStream().filter(b -> !b.isDeleted())
			.map(b -> ModelUtil.getAdapter(b, IBilled.class, true)).collect(Collectors.toList());
	}
	
	@Override
	public void removeBilled(IBilled billed){
		CoreModelServiceHolder.get().delete(billed);
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getDatum();
	}

	@Override
	public void setDate(LocalDate value){
		getEntityMarkDirty().setDatum(value);
	}

	@Override
	public VersionedResource getVersionedEntry(){
		return getEntity().getEintrag();
	}

	@Override
	public void setVersionedEntry(VersionedResource value){
		getEntityMarkDirty().setEintrag(value);
	}
	
	@Override
	public List<IDiagnosisReference> getDiagnoses(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getDiagnoses().parallelStream().filter(d -> !d.isDeleted())
			.map(d -> ModelUtil.getAdapter(d, IDiagnosisReference.class, true))
			.collect(Collectors.toList());
	}
	
	@Override
	public void addDiagnosis(IDiagnosis diagnosis){
		if (!(diagnosis instanceof IDiagnosisReference)) {
			diagnosis = ModelUtil.getOrCreateDiagnosisReference(diagnosis);
		}
		addChanged(diagnosis);
		@SuppressWarnings("unchecked")
		Diagnosis diag = ((AbstractIdModelAdapter<Diagnosis>) diagnosis).getEntity();
		if (!getEntity().getDiagnoses().contains(diag)) {
			getEntityMarkDirty().getDiagnoses().add(diag);
		}
	}
	
	@Override
	public void removeDiagnosis(IDiagnosis diagnosis){
		if (!(diagnosis instanceof IDiagnosisReference)) {
			LoggerFactory.getLogger(getClass()).warn("Can only remove IDiagnosisReference");
			return;
		}
		@SuppressWarnings("unchecked")
		Diagnosis diag = ((AbstractIdModelAdapter<Diagnosis>) diagnosis).getEntity();
		getEntityMarkDirty().getDiagnoses().remove(diag);
	}
	
	@Override
	public IInvoice getInvoice(){
		return ModelUtil.getAdapter(getEntity().getInvoice(), IInvoice.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setInvoice(IInvoice value){
		if (value != null) {
			getEntityMarkDirty().setInvoice(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Invoice>) value).getEntity());
		} else {
			getEntityMarkDirty().setInvoice(null);
		}
	}
	
	@Override
	public boolean isBillable(){
		return getEntity().getBillable();
	}
	
	@Override
	public void setBillable(boolean value){
		getEntityMarkDirty().setBillable(value);
	}
	
	@Override
	public InvoiceState getInvoiceState(){
		IInvoice invoice = getInvoice();
		if (invoice != null) {
			return invoice.getState();
		}
		IMandator mandator = getMandator();
		IMandator activeMandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		if ((mandator != null && activeMandator != null) && (mandator.equals(activeMandator))) {
			if (getDate().isEqual(LocalDate.now())) {
				return InvoiceState.FROM_TODAY;
			} else {
				return InvoiceState.NOT_FROM_TODAY;
			}
		} else {
			return InvoiceState.NOT_FROM_YOU;
		}
	}
	
	@Override
	public String getLabel(){
		StringBuffer ret = new StringBuffer();
		IMandator m = getMandator();
		
		ret.append(getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append(" (").append(getInvoiceStateText()).append(") - ")
			.append((m == null) ? "?" : m.getLabel());
		return ret.toString();
	}
	
	private String getInvoiceStateText(){
		String statusText = "";
		
		IInvoice rechnung = getInvoice();
		if (rechnung != null) {
			statusText += "RG " + rechnung.getNumber() + ": ";
		}
		statusText += getInvoiceState().getLocaleText();
		return statusText;
	}
}
