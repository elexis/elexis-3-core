package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.ModelUtil;

public class Stock extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Stock>
		implements IdentifiableWithXid, IStock {
	
	public Stock(ch.elexis.core.jpa.entities.Stock entity){
		super(entity);
	}
	
	@Override
	public String getCode(){
		return getEntity().getCode();
	}
	
	@Override
	public String getDriverUuid(){
		return getEntity().getDriverUuid();
	}
	
	@Override
	public String getDriverConfig(){
		return getEntity().getDriverConfig();
	}
	
	@Override
	public int getPriority(){
		return getEntity().getPriority();
	}
	
	@Override
	public IMandator getOwner(){
		if (getEntity().getOwner() != null) {
			return ModelUtil.getAdapter(getEntity().getOwner(), IMandator.class);
		}
		return null;
	}
}
