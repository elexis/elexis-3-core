package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Diagnosis;
import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
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
		getEntity().setDatum(value.toLocalDate());
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
			getEntity().setFall(((AbstractIdModelAdapter<Fall>) value).getEntity());
		} else {
			getEntity().setFall(null);
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
			getEntity().setMandant(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntity().setMandant(null);
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
		getEntity().setDatum(value);
	}

	@Override
	public VersionedResource getVersionedEntry(){
		return getEntity().getEintrag();
	}

	@Override
	public void setVersionedEntry(VersionedResource value){
		getEntity().setEintrag(value);
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
		// is needed here, because of the behdl_dg_joint mapping table
		if (diagnosis != null) {
			@SuppressWarnings("unchecked")
			Diagnosis diag = ((AbstractIdModelAdapter<Diagnosis>) diagnosis).getEntity();
			getEntity().getDiagnoses().add(diag);
			ModelUtil.getModelService().save(Arrays.asList(diagnosis, this));
		}
	}
	
	@Override
	public void removeDiagnosis(IDiagnosis diagnosis){
		if (!(diagnosis instanceof IDiagnosisReference)) {
			LoggerFactory.getLogger(getClass()).warn("Can only remove IDiagnosisReference");
		}
		@SuppressWarnings("unchecked")
		Diagnosis diag = ((AbstractIdModelAdapter<Diagnosis>) diagnosis).getEntity();
		getEntity().getDiagnoses().remove(diag);
		ModelUtil.getModelService().save(Arrays.asList(diagnosis, this));
	}
}
