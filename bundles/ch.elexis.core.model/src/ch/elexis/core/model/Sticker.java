package ch.elexis.core.model;

import java.util.List;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;

public class Sticker extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Sticker>
		implements IdentifiableWithXid, ISticker {
	
	public Sticker(ch.elexis.core.jpa.entities.Sticker entity){
		super(entity);
	}
	
	@Override
	public String getBackground(){
		return getEntity().getBackground();
	}
	
	@Override
	public void setBackground(String value){
		getEntity().setBackground(value);
	}
	
	@Override
	public String getForeground(){
		return getEntity().getForeground();
	}
	
	@Override
	public void setForeground(String value){
		getEntity().setForeground(value);
	}
	
	@Override
	public boolean isVisible(){
		return true;
	}
	
	@Override
	public void setVisible(boolean value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setClassForSticker(Class<?> clazz){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeClassForSticker(Class<?> clazz){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<String> getClassesForSticker(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int compareTo(ISticker o){
		return getLabel().compareTo(o.getLabel());
	}
	
	@Override
	public String getName(){
		return getEntity().getName();
	}
	
	@Override
	public void setName(String value){
		getEntity().setName(value);
	}
	
}
