package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.ModelUtil;

public class Coverage extends AbstractIdDeleteModelAdapter<Fall>
		implements IdentifiableWithXid, ICoverage {
	
	private ExtInfoHandler extInfoHandler;
	
	public Coverage(Fall entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
	
	@Override
	public IPatient getPatient(){
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setPatient(IPatient value){
		if (value != null) {
			getEntity().setPatientKontakt(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntity().setPatientKontakt(null);
		}
	}
	
	@Override
	public String getDescription(){
		return getEntity().getBezeichnung();
	}
	
	@Override
	public void setDescription(String value){
		getEntity().setBezeichnung(value);
	}
	
	@Override
	public String getReason(){
		return getEntity().getGrund();
	}
	
	@Override
	public void setReason(String value){
		getEntity().setGrund(value);
	}
	
	@Override
	public LocalDate getDateFrom(){
		return getEntity().getDatumVon();
	}
	
	@Override
	public void setDateFrom(LocalDate value){
		getEntity().setDatumBis(value);
	}
	
	@Override
	public String getBillingSystem(){
		return getEntity().getGesetz();
	}
	
	@Override
	public void setBillingSystem(String value){
		getEntity().setGesetz(value);
	}

	@Override
	public IContact getCostBearer(){
		return ModelUtil.getAdapter(getEntity().getKostentrKontakt(), IContact.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCostBearer(IContact value){
		if (value != null) {
			getEntity().setKostentrKontakt(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntity().setKostentrKontakt(null);
		}
	}

	@Override
	public String getInsuranceNumber(){
		return getEntity().getVersNummer();
	}

	@Override
	public void setInsuranceNumber(String value){
		getEntity().setVersNummer(value);
	}
	
}
