package ch.elexis.core.jpa.model.adapter;

import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

public abstract class AbstractIdDeleteModelAdapter<T extends AbstractDBObjectIdDeleted>
		extends AbstractIdModelAdapter<T>
		implements Identifiable, Deleteable {
	
	public AbstractIdDeleteModelAdapter(T entity){
		super(entity);
	}
	
	@Override
	public boolean isDeleted(){
		return getEntity().isDeleted();
	}
	
	@Override
	public void setDeleted(boolean value){
		getEntity().setDeleted(value);
	}
}
