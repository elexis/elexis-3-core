package ch.elexis.core.model;

import java.time.LocalDateTime;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class LabOrder extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.LabOrder>
		implements IdentifiableWithXid, ILabOrder {
	
	public LabOrder(ch.elexis.core.jpa.entities.LabOrder entity){
		super(entity);
	}
	
	@Override
	public ILabResult getResult(){
		return ModelUtil.getAdapter(getEntity().getResult(), ILabResult.class);
	}
	
	@Override
	public void setResult(ILabResult value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setResult(
				(ch.elexis.core.jpa.entities.LabResult) ((AbstractIdModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntity().setResult(null);
		}
	}
	
	@Override
	public ILabItem getItem(){
		return ModelUtil.getAdapter(getEntity().getItem(), ILabItem.class);
	}
	
	@Override
	public void setItem(ILabItem value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setItem(
				(ch.elexis.core.jpa.entities.LabItem) ((AbstractIdModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntity().setItem(null);
		}
	}
	
	@Override
	public IPatient getPatient(){
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
	}
	
	@Override
	public void setPatient(IPatient value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setPatient((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntity().setPatient(null);
		}
	}
	
	@Override
	public LocalDateTime getTimeStamp(){
		return getEntity().getTime();
	}
	
	@Override
	public void setTimeStamp(LocalDateTime value){
		getEntity().setTime(value);
	}
	
	@Override
	public LocalDateTime getObservationTime(){
		return getEntity().getObservationTime();
	}
	
	@Override
	public void setObservationTime(LocalDateTime value){
		getEntity().setObservationTime(value);
	}
	
	@Override
	public IContact getUser(){
		return ModelUtil.getAdapter(getEntity().getUser(), IContact.class);
	}
	
	@Override
	public void setUser(IContact value){
		if (value instanceof AbstractIdModelAdapter<?>) {
			getEntity().setUser((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntity().setUser(null);
		}
	}
	
	@Override
	public IMandator getMandator(){
		return ModelUtil.getAdapter(getEntity().getMandator(), IMandator.class);
	}
	
	@Override
	public void setMandator(IMandator value){
		if (value instanceof AbstractIdModelAdapter<?>) {
			getEntity().setMandator((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntity().setMandator(null);
		}
	}
	
	@Override
	public String getOrderId(){
		return getEntity().getOrderid();
	}
	
	@Override
	public void setOrderId(String value){
		getEntity().setOrderid(value);
	}
	
	@Override
	public LabOrderState getState(){
		return getEntity().getState();
	}
	
	@Override
	public void setState(LabOrderState value){
		getEntity().setState(value);
	}
	
	@Override
	public String toString(){
		return getEntity().getMandator() + " -> [" + getEntity().getItem() + "] ["
			+ getEntity().getResult() + "]";
	}
}
