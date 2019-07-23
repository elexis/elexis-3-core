package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Right extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Right>
		implements IdentifiableWithXid, IRight {
	
	public Right(ch.elexis.core.jpa.entities.Right entity){
		super(entity);
	}
	
	@Override
	public void setId(String id){
		getEntityMarkDirty().setId(id);
	}
	
	@Override
	public String getName(){
		return getEntity().getName();
	}
	
	@Override
	public void setName(String value){
		getEntityMarkDirty().setName(value);
	}
	
	@Override
	public String getLocalizedName(){
		return getEntity().getI18nName();
	}
	
	@Override
	public void setLocalizedName(String value){
		getEntityMarkDirty().setI18nName(value);
	}
	
	@Override
	public IRight getParent(){
		ch.elexis.core.jpa.entities.Right parent = getEntity().getParent();
		if (parent != null) {
			return ModelUtil.getAdapter(parent, IRight.class);
		}
		return null;
	}
	
	@Override
	public void setParent(IRight value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setParent(
				(ch.elexis.core.jpa.entities.Right) ((AbstractIdDeleteModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setParent(null);
		}
	}
	
}
