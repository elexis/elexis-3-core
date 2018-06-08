package ch.elexis.core.jpa.model.adapter;

import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

public abstract class AbstractIdDeleteModelAdapter<T extends AbstractDBObjectIdDeleted>
		implements Identifiable, Deleteable {
	
	private T entity;
	
	public AbstractIdDeleteModelAdapter(T entity){
		this.entity = entity;
		// make sure model supports id and delete
		if (!(entity instanceof AbstractDBObjectIdDeleted)) {
			throw new IllegalStateException(
				"Model " + entity + " is no subclass of "
					+ AbstractDBObjectIdDeleted.class.getSimpleName());
		}
	}
	
	public T getEntity(){
		return entity;
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
	public boolean isDeleted(){
		return getEntity().isDeleted();
	}
	
	@Override
	public void setDeleted(boolean value){
		getEntity().setDeleted(value);
	}
	
	// TODO maybe change to Objects 
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		return result;
	}
	
	// TODO maybe change to Objects 
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractIdDeleteModelAdapter<?> other = (AbstractIdDeleteModelAdapter<?>) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		return true;
	}
}
