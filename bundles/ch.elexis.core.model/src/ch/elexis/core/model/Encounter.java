package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.ModelUtil;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPatient(IPatient value){
		// TODO why?
		return;
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
		// TODO Auto-generated method stub
		return null;
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
	
}
