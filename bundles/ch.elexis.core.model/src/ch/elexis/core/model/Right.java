package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Right extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Right>
		implements IdentifiableWithXid, IRight {
	
	public Right(ch.elexis.core.jpa.entities.Right entity){
		super(entity);
	}
	
	@Override
	public void setId(String id){
		getEntity().setId(id);
	}
	
	@Override
	public String getName(){
		return getEntity().getName();
	}
	
	@Override
	public void setName(String value){
		getEntity().setName(value);
	}
	
	@Override
	public String getLocalizedName(){
		return getEntity().getI18nName();
	}
	
	@Override
	public void setLocalizedName(String value){
		getEntity().setI18nName(value);
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
			getEntity().setParent(
				(ch.elexis.core.jpa.entities.Right) ((AbstractIdDeleteModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntity().setParent(null);
		}
	}
	
}
