package ch.elexis.core.jpa.model.adapter;

import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

public abstract class AbstractIdDeleteModelAdapter<T extends EntityWithId>
		extends AbstractIdModelAdapter<T>
		implements Identifiable, Deleteable {
	
	public AbstractIdDeleteModelAdapter(T entity){
		super(entity);
		if (!(entity instanceof EntityWithDeleted)) {
			throw new IllegalStateException(
				"Entity " + entity + " does not implement EntityWithDeleted");
		}
	}
	
	@Override
	public boolean isDeleted(){
		return ((EntityWithDeleted) getEntity()).isDeleted();
	}
	
	@Override
	public void setDeleted(boolean value){
		((EntityWithDeleted) getEntity()).setDeleted(value);
	}
}
