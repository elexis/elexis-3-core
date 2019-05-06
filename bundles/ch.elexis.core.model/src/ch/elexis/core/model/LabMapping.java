package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class LabMapping extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.LabMapping>
		implements IdentifiableWithXid, ILabMapping {
	
	public LabMapping(ch.elexis.core.jpa.entities.LabMapping entity){
		super(entity);
	}
	
	@Override
	public String getItemName(){
		return getEntity().getItemname();
	}
	
	@Override
	public void setItemName(String value){
		getEntity().setItemname(value);
	}
	
	@Override
	public ILabItem getItem(){
		return ModelUtil.getAdapter(getEntity().getLabItem(), ILabItem.class);
	}
	
	@Override
	public void setItem(ILabItem value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setLabItem(
				(ch.elexis.core.jpa.entities.LabItem) ((AbstractIdModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntity().setLabItem(null);
		}
	}
	
	@Override
	public IContact getOrigin(){
		return ModelUtil.getAdapter(getEntity().getOrigin(), IContact.class);
	}
	
	@Override
	public void setOrigin(IContact value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity()
				.setOrigin((ch.elexis.core.jpa.entities.Kontakt) ((AbstractIdModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntity().setOrigin(null);
		}
	}
	
	@Override
	public boolean isCharge(){
		return getEntity().isCharge();
	}
	
	@Override
	public void setCharge(boolean value){
		getEntity().setCharge(value);
	}
}
