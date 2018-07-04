package ch.elexis.core.model;

import java.util.Optional;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.service.CoreModelAdapterFactory;

public class UserConfig extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Userconfig>
		implements IUserConfig {
	
	public UserConfig(ch.elexis.core.jpa.entities.Userconfig entity){
		super(entity);
	}
	
	@Override
	public String getId(){
		return getEntity().getId();
	}
	
	@Override
	public String getLabel(){
		return getEntity().getLabel();
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
		return getEntity().getValue();
	}
	
	@Override
	public void setValue(String value){
		getEntity().setValue(value);
	}
	
	@Override
	public IContact getOwner(){
		if (getEntity().getOwner() != null) {
			Optional<Identifiable> owner = CoreModelAdapterFactory.getInstance()
				.getModelAdapter(getEntity().getOwner(), IContact.class, true);
			return (IContact) owner.orElse(null);
		}
		return null;
	}
	
	@Override
	public void setOwner(IContact value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntity().setOwner((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntity().setOwner(null);
		}
	}
}
