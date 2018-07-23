package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.mixin.IdentifiableWithXid;

public class Config extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Config>
		implements IdentifiableWithXid, IConfig {
	
	public Config(ch.elexis.core.jpa.entities.Config entity){
		super(entity);
	}
	
	@Override
	public String getId(){
		return getEntity().getId();
	}
	
	@Override
	public String getLabel(){
		return getEntity().getParam() + " -> " + getEntity().getWert();
	}
	
	@Override
	public String getKey(){
		return getEntity().getParam();
	}
	
	@Override
	public void setKey(String value){
		getEntity().setParam(value);
	}
	
	@Override
	public String getValue(){
		return getEntity().getWert();
	}
	
	@Override
	public void setValue(String value){
		getEntity().setWert(value);
	}
}
