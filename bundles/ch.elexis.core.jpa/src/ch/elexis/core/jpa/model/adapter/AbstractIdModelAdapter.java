package ch.elexis.core.jpa.model.adapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public abstract class AbstractIdModelAdapter<T extends EntityWithId> implements Identifiable {
	
	/**
	 * Used in json serialization
	 */
	@SuppressWarnings("unused")
	private final String entityType;
	
	private T entity;
	
	public AbstractIdModelAdapter(T entity){
		this.entity = entity;
		this.entityType = entity.getClass().getName();
		// make sure model supports id and delete
		if (!(entity instanceof EntityWithId)) {
			throw new IllegalStateException(
				"Model " + entity + " is no subclass of "
					+ EntityWithId.class.getSimpleName());
		}
	}
	
	public T getEntity(){
		return entity;
	}
	
	/**
	 * <b>IMPORTANT:</b> this method should only be used {@link IModelService} implementations to
	 * update the entity on merge. This is needed if entity listeners update values of the entity on
	 * persist, to update with the modified entity.
	 * 
	 * @param entity
	 */
	@SuppressWarnings("unchecked")
	public void setEntity(EntityWithId entity){
		this.entity = (T) entity;
	}
	
	@Override
	public String getId(){
		return getEntity().getId();
	}
	
	@Override
	public String getLabel(){
		return getEntity().toString();
	}
	
	@Override
	public Long getLastupdate(){
		return getEntity().getLastupdate();
	}
	
	protected Date toDate(LocalDateTime localDateTime){
		ZonedDateTime atZone = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(atZone.toInstant());
	}
	
	protected Date toDate(LocalDate localDate){
		ZonedDateTime atZone = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(atZone.toInstant());
	}
	
	protected LocalDateTime toLocalDate(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	@Override
	public String toString() {
		return getClass().getName() + " [getEntity()=" + getEntity() + ", getId()=" + getId() + ", getLastupdate()="
				+ getLastupdate() + "]";
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractIdModelAdapter<?> other = (AbstractIdModelAdapter<?>) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entityEqualId(entity, other.entity))
			return false;
		return true;
	}
	
	private boolean entityEqualId(EntityWithId left, EntityWithId right){
		if (left.getId() != null && right.getId() != null) {
			return left.getId().equals(right.getId());
		}
		return false;
	}
}
